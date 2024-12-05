package osuIRC.Backend;

import android.util.Log;

import osuIRC.Exception.NoSuchChannel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OsuSocket {
    public Manager manager;

    private Socket socket;
    private BufferedReader IStream;
    private BufferedWriter OStream;
    private int retryCount = 0;
    private boolean pipeBroken = false;
    private boolean reconnected = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public List<String> stackTrace = new ArrayList<>();

    public OsuSocket() {}

    public int connect(String nick, String pass) throws InterruptedException {
        retryCount++;
        try {
            socket = new Socket("irc.ppy.sh", 6667);
            socket.setKeepAlive(true);
            retryCount = 0;
        } catch (SocketTimeoutException e) {
            Thread.sleep(1000);
            if (retryCount > 10) {
                stackTrace.add("Connection timed out");
                Log.e("OsuSocket", "connect: Connection timed out");
                return 2;
            }
            return connect(nick, pass);
        } catch (IOException e) {
            stackTrace.add(e.getMessage());
            Log.e("OsuSocket", "connect: " + e.getMessage());
        }

        try {
            IStream = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            OStream = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            send("PASS " + pass);
            send("NICK " + nick);

            while (true) {
                String[] response = IStream.readLine().split(" ");

                boolean status = false;
                switch (response[1]) {
                    case "464":
                        stackTrace.add("Wrong password or username");
                        Log.e("OsuSocket", "connect: Wrong password or username");
                        return 1;
                    case "376":
                        status = true;
                        break;
                }
                if (status) break;
            }

            if (manager == null) manager = new Manager(nick, pass);

            if (!reconnected) monitorConnection(); recv(); keepAlive();

            return 0;
        } catch (IOException e) {
            if (e.getMessage() != null) {
                stackTrace.add(e.getMessage());
                Log.e("OsuSocket", "conect: " + e.getMessage());
            } else {
                stackTrace.add("Unknown error");
                Log.e("OsuSocket", "connect: Unknown error");
            }
            return 3;
        }
    }

    private void monitorConnection() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (!socket.isConnected() | socket.isClosed() | pipeBroken) {
                try {
                    connect(manager.nick, manager.pass);
                    reconnected = true;
                    pipeBroken = false;
                } catch (InterruptedException e) {
                    stackTrace.add(e.getMessage());
                    Log.e("OsuSocket", "monitorConnection: " + e.getMessage());
                    scheduler.shutdown();
                }
            } else if (reconnected) {
                List<String> chatList = manager.getAllChat();
                String activeChat = manager.activeChat;
                manager.clear();
                chatList.forEach(this::join);
                manager.setActiveChat(activeChat);
                reconnected = false;
            }
        },  0, 10, TimeUnit.SECONDS);
    }

    private void keepAlive() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (socket != null && !socket.isClosed())
                send("KEEP_ALIVE");
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void recv() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                String msg = IStream.readLine();
                if (msg != null) {
//                    if (!msg.contains("QUIT")) Log.d("OsuSocket", "recv: " + msg);

                    if (msg.equals("PING cho.ppy.sh")) send(msg);

                    List<String> parsedMessage = StringUtils.parse(msg);
                    manager.update(parsedMessage);
                }
            } catch (IOException e) {
                stackTrace.add(e.getMessage());
                Log.e("OsuSocket", "recv: " + e.getMessage());
                if (Objects.equals(e.getMessage(), "Broken pipe"))
                    pipeBroken = true;
            } catch (NoSuchChannel e) {
                manager.removeChat("");
                stackTrace.add(e.getMessage());
                Log.e("OsuSocket", "recv: " + e.getMessage());
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void send(String message) {
        Runnable task = () -> {
            try {
                OStream.write(message + "\n");
                OStream.flush();
                Log.d("OsuSocket", "send: " + message);
            } catch (IOException e) {
                stackTrace.add(e.getMessage());
                Log.e("OsuSocket", "send: " + e.getMessage());
                if (Objects.equals(e.getMessage(), "Broken pipe"))
                    pipeBroken = true;
            }
        };
        executor.submit(task);
    }

//    public void kill() throws IOException {
//        executor.shutdownNow();
//        scheduler.shutdownNow();
//        manager.clear();
//        socket.close();
//    }

    public void join(String name) {
        if (manager.getChannel(name) == null)
            send("JOIN " + name);

        manager.setActiveChat(name);
    }

    public void part(String name) {
        if (manager.getChannel(name) != null) {
            send("PART " + name);
            manager.removeChat(name);
        }
    }
}
