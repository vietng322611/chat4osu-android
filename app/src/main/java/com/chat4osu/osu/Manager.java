package com.chat4osu.osu;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Manager {
    private final Map<String, Channel> channelList = new HashMap<>();

    public String nick;
    public String activeChat = "";

    public Manager(String nick) { this.nick = nick; }

    public void addChat(String name) {
        if (!channelList.containsKey(name))
            channelList.put(name, new Channel(name));
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
        if (data == null) return;

        String name = data.get(1);
        switch (data.get(0)) {
            case "0":
                if (data.get(2).equals(nick)) addChat(name);
                if (channelList.containsKey(name))
                    channelList.get(name).addUser(data.subList(2, data.size()));
                break;

            case "1":
                if (name.equals(nick)) name = data.get(2);

                if (!channelList.containsKey(name)) addChat(name);
                channelList.get(name).updateMessage(data);
                break;

            case "2":
                if (data.get(2).equals(nick)) removeChat(name);
                else {
                    if (channelList.containsKey(name))
                        channelList.get(name).removeUser(data.get(2));
                }
                break;

            default:
                break;
        }
    }

    public Channel getChannel(String name) {
        if (name.isEmpty()) name = activeChat;
        if (name.isEmpty()) return null;

        if (channelList.containsKey(name)) {
            return channelList.get(name);
        } else { return null; }
    }
}