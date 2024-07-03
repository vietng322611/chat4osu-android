package com.chat4osu;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.*;
import java.io.*;

public class OsuSocket {
    private String host = "irc.ppy.sh";
    private int port = 6667;

    private Socket socket;
    private BufferedReader IStream;
    private BufferedWriter OStream;

    public Manager manager;

    private ExecutorService executor = Executors.newCachedThreadPool();

    public String errorLog = null; // Where's my logger huuhuhuhuhuhuhuhuhuhu

    public OsuSocket() {}

    public int Connect(String nick, String passw) throws Exception {
        nick = nick.replace(" ", "_");
        try {
            socket = new Socket(host, port);
        } catch (SocketTimeoutException e) {
            Thread.sleep(1000);
            return Connect(nick, passw);
        }

        socket.setKeepAlive(true);
        IStream = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        OStream = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream()));

        send("PASS " + passw);
        send("NICK " + nick);

        String response = IStream.readLine();
        if (response.contains("Bad authentication token")) {
            errorLog = "Wrong password or username";
            return 1;
        }

        manager = new Manager(nick);
        executor.submit(this::recv);

        return 0;
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
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (NoSuchChannel e) {
                errorLog = e.getMessage();
                continue;
            }
        }
    }

    public void send(String message) {
        try {
            OStream.write(message + "\n");
            OStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void join(String name) {
        send("JOIN " + name);
    }

    public void part(String name) {
        send("PART " + name);
    }
}
