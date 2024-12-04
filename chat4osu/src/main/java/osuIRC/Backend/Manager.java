package osuIRC.Backend;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Manager {

    private final Map<String, Channel> channelList = new HashMap<>();

    public String nick;
    public String pass;
    public String activeChat = "";

    public Manager(String nick, String pass) {
        this.nick = nick;
        this.pass = pass;
    }

    public void clear() {
        channelList.clear();
        nick = activeChat = "";
    }

    public void addChat(String name) {
        if (!channelList.containsKey(name)) {
            channelList.put(name, new Channel(name));

            String chatType;
            if (name.startsWith("#mp_")) {
                chatType = "lobby";
            } else if (name.startsWith("#")) {
                chatType = "chat";
            } else { chatType = "DM"; }
            Objects.requireNonNull(channelList.get(name)).type = chatType;
        }
    }

    public void removeChat(String name) { channelList.remove(name); }

    public void setActiveChat(String name) {
        activeChat = name;
        Log.d("Manager", "setActiveChat: " + activeChat);
    }

    public List<String> getAllChat() {
        return new ArrayList<>(channelList.keySet());
    }

    public void update(List<String> data) {
        if (data.isEmpty()) return;

        String name = data.get(1);
        switch (data.get(0)) {
            case "0":
                if (data.get(2).equals(nick))
                    addChat(name);
                Objects.requireNonNull(channelList.get(name)).addUser(data.subList(2, data.size()));
                break;

            case "1":
                if (name.equals(nick)) name = data.get(2);

                if (!channelList.containsKey(name))
                    addChat(name);
                Objects.requireNonNull(channelList.get(name)).updateMessage(data);

                break;

            case "2":
                if (data.get(2).equals(nick))
                    removeChat(name);
                else
                    Objects.requireNonNull(channelList.get(name)).removeUser(data.get(2));
                break;
        }
    }

    public Channel getChannel(String name) {
        if (name.isEmpty()) {
            if (activeChat.isEmpty()) return null;
            name = activeChat;
        }

        return channelList.getOrDefault(name, null);
    }
}