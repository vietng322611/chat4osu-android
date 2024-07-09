package com.chat4osu.osu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Manager {
    private Map<String, Channel> channelList = new HashMap<String, Channel>();
    private String nick = null;

    public String activeChat = null;

    public Manager(String nick) { this.nick = nick; }

    public void add(String name) {
        if (channelList.containsKey(name)) return;
        channelList.put(name, new Channel(name));

        setActiveChat(name);
    }

    public void remove(String name) { channelList.remove(name); }

    public void setActiveChat(String name) { activeChat = name; }

    public void update(List<String> data) {
        if (data == null) return;

        String name = data.get(1);
        switch (data.get(0)) {
            case "0":
                if (data.get(2).equals(nick)) add(name);

                channelList.get(name).addUser(data.subList(2, data.size()));
                break;

            case "1":
                channelList.get(name).updateMessage(data);
                break;

            case "2":
                if (data.get(2).equals(nick)) remove(name);
                else {
                    channelList.get(name).removeUser(data.get(2));
                }
                break;

            default:
                break;
        }
    }

    public Channel getChannel(String name) {
        if (name == null) name = activeChat;
        if (name == null) return null;

        return channelList.get(name);
    }
}