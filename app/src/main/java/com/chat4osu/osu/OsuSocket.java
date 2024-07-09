package com.chat4osu.osu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OsuSocket {
    public Manager manager;
    public List<String> stackTrace = new ArrayList<String>(); // Where's my logger huuhuhuhuhuhuhuhuhuhu
    private final String host = "irc.ppy.sh";
    private final int port = 6667;
    private Socket socket;
    private BufferedReader IStream;
    private BufferedWriter OStream;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private int retryCount = 0;

    public OsuSocket() {
    }

    public void connect(String nick, String pass) throws Exception {
        retryCount++;
        try {
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
        } catch (SocketTimeoutException e) {
            Thread.sleep(1000);
            if (retryCount > 10) {
                stackTrace.add("Connection timed out");
            }
            connect(nick, pass);
            return;
        } catch (IOException e) {
            stackTrace.add(e.getMessage());
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
                        return;
                    case "376":
                        status = true;
                        break;
                }
                if (status) break;
            }

            manager = new Manager(nick);
            executor.submit(this::recv);
        } catch (IOException e) {
            stackTrace.add(e.getMessage());
        }
    }

    public void recv() {
        String inputLine;
        while (!socket.isInputShutdown()) {
            try {
                inputLine = IStream.readLine();
                if (inputLine != null) {
                    List<String> parsedMessage = StringUtils.parse(inputLine);
                    manager.update(parsedMessage);
                } else {
                    Thread.sleep(100);
                }
            } catch (IOException | InterruptedException e) {
                stackTrace.add(e.getMessage());
                break;
            } catch (NoSuchChannel e) {
                stackTrace.add(e.getMessage());
            }
        }
    }

    public void send(String message) {
        try {
            OStream.write(message + "\n");
            OStream.flush();
        } catch (Exception e) {
            stackTrace.add(e.getMessage());
        }
    }

    public void join(String name) {
        send("JOIN " + name);
    }

    public void part(String name) {
        send("PART " + name);
    }
}
