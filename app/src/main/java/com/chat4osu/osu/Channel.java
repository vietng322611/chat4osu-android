package com.chat4osu.osu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Channel {
    public String name = null;
    private Set<String> userList = new HashSet<String>();
    private List<String> messages = new ArrayList<String>();

    private Pattern join = Pattern.compile("BanchoBot : (.*?) joined in slot \\d");
    private Pattern leave = Pattern.compile("BanchoBot : (.*?) left the game");

    public Channel(String name) { this.name = name; }

    public Set<String> getUser() { return userList; }

    public List<String> getMessage() {
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
    }

    public void addUser(List<String> names) { userList.addAll(names); }
    public void removeUser(String name) { userList.remove(name); }
}
