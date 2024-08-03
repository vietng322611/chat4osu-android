package com.chat4osu.osu;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public List<String> stackTrace = new ArrayList<>(); // Where's my logger huuhuhuhuhuhuhuhuhuhu

    public OsuSocket() {
    }

    public int connect(String nick, String pass) throws InterruptedException {
        retryCount++;
        try {
            socket = new Socket("irc.ppy.sh", 6667);
            socket.setKeepAlive(true);
        } catch (SocketTimeoutException e) {
            Thread.sleep(1000);
            if (retryCount > 5) {
                stackTrace.add("Connection timed out");
                Log.e("OsuSocket", "connect: Connection timed out");
                return 2;
            }
            return connect(nick, pass);
        } catch (IOException e) {
            stackTrace.add(e.getMessage());
            Log.e("OsuSocket", "connect: " + e.getMessage());
        }
        retryCount = 0;

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

            manager = new Manager(nick);

            new Thread(this::recv).start();
            keepAlive();

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

    public void recv() {
        String msg;
        while (!socket.isInputShutdown()) {
            try {
                msg = IStream.readLine();
                if (msg != null) {
                    if (!msg.contains("QUIT")) Log.d("OsuSocket","recv: " + msg);

                    if (msg.equals("PING cho.ppy.sh")) { send(msg); continue; }

                    List<String> parsedMessage = StringUtils.parse(msg);
                    manager.update(parsedMessage);
                } else {
                    Thread.sleep(100);
                }
            } catch (IOException | InterruptedException | NoSuchChannel e) {
                stackTrace.add(e.getMessage());
                Log.e("OsuSocket", "recv: " + e.getMessage());
            }
        }
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
            }
        };
        executor.submit(task);
    }

    private void keepAlive() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (socket != null && !socket.isClosed())
                send("KEEP_ALIVE");
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void join(String name) {
        if (manager.getChannel(name) == null)
            send("JOIN " + name);

        manager.setActiveChat(name);
    }

    public void part(String name) {
        if (manager.getChannel(name) != null)
            send("PART " + name);
    }
}
