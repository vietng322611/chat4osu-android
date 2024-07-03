package com.chat4osu;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class Channel {
    public String name = null;
    private Set<String> userList = new HashSet<String>();
    private List<String> messages = new ArrayList<String>();

    private Pattern join = Pattern.compile("BanchoBot : (.*?) joined in slot \\d");
    private Pattern leave = Pattern.compile("BanchoBot : (.*?) left the game");

    protected Set<String> getUser() { return userList; }

    protected List<String> getMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        List<String> ret = new ArrayList<>(messages);
        messages.clear();
        return ret;
    }

    public void updateMessage(List<String> data) {
        String message = data.get(2) + " : " + data.get(3);
        messages.add(message);

        Matcher matcher = join.matcher(message);
        if (matcher.matches()) userList.add(matcher.group(1));

        matcher = leave.matcher(message);
        if (matcher.matches()) userList.remove(matcher.group(1));

        return;
    }

    protected void addUser(List<String> names) { userList.addAll(names); }
    protected void removeUser(String name) { userList.remove(name); }
}

class Chat extends Channel {
    public Chat(String name) { this.name = name; }
}

class Lobby extends Channel {
    public Lobby(String name) { this.name = name; }
}

public class Manager {
    private Map<String, Chat> chatList = new HashMap<String, Chat>();
    private Map<String, Lobby> lobbyList = new HashMap<String, Lobby>();
    private String nick = null;

    public String activeChat = null;
    
    public Manager(String nick) { this.nick = nick; }

    public void add(String name) {
        if (name.startsWith("#mp_")) {
            if (lobbyList.containsKey(name)) return;
            lobbyList.put(name, new Lobby(name));
        } else {
            if (chatList.containsKey(name)) return;
            chatList.put(name, new Chat(name));
        }
        setActiveChat(name);
    }

    public void remove(String name) {
        if (name.startsWith("#mp_")) {
            lobbyList.remove(name);
        } else {
            chatList.remove(name);
        }
    }

    public void setActiveChat(String name) { activeChat = name; }

    public void update(List<String> data) {
        if (data == null) return;

        String name = data.get(1);
        switch (data.get(0)) {
            case "0":
                if (data.get(2).equals(nick)) add(name);

                if (name.startsWith("#mp_"))
                    lobbyList.get(name).addUser(data.subList(2, data.size()));
                else
                    chatList.get(name).addUser(data.subList(2, data.size()));
                break;

            case "1":
                if (name.startsWith("#mp_"))
                    lobbyList.get(name).updateMessage(data);
                else {
                    chatList.get(name).updateMessage(data);
                }
                break;

            case "2":
                if (data.get(2).equals(nick)) remove(name);
                else {
                    if (name.startsWith("#mp_"))
                        lobbyList.get(name).removeUser(data.get(2));
                    else {
                        chatList.get(name).removeUser(data.get(2));
                    }
                }
                break;

            default:
                break;
        }
    }

    public Object getChannel(String name) {
        if (name == null) name = activeChat;
        if (name == null) return null;

        if (name.startsWith("#mp_"))
            return lobbyList.get(name);
        else
            return chatList.get(name);
    }
}