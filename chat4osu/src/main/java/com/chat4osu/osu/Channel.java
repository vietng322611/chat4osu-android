package com.chat4osu.osu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Channel {
    private final Lock lock = new ReentrantLock();

    public String name;
    private final Set<String> userList = new HashSet<>();
    private List<String> messageList = new ArrayList<>();

    private final Pattern join = Pattern.compile("BanchoBot : (.*) joined in slot \\d");
    private final Pattern leave = Pattern.compile("BanchoBot : (.*) left the game");

    public Channel(String name) { this.name = name; }

    public Set<String> getUser() { return userList; }

    public List<String> getMessage() {
        if (!messageList.isEmpty()) {
            List<String> ret = new ArrayList<>(messageList);
            messageList.clear();
            return ret;
        } else { return messageList; }
    }

    public void updateMessage(List<String> data) {
        String message = String.format("[%s] %s: %s", data.get(3), data.get(2), data.get(4));
        messageList.add(message);

        Matcher matcher = join.matcher(message);
        if (matcher.matches()) userList.add(matcher.group(1));

        matcher = leave.matcher(message);
        if (matcher.matches()) userList.remove(matcher.group(1));
    }

    public void saveMsg(List<String> data) {
        lock.lock();
        try {
            messageList = new ArrayList<>(data);
        } finally { lock.unlock(); }
    }

    public void addUser(List<String> names) { userList.addAll(names); }
    public void removeUser(String name) { userList.remove(name); }
}
