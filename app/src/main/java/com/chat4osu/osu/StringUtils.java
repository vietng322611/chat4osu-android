package com.chat4osu.osu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * :cho.ppy.sh 353 [nick] = [channel_name] :[name_list]
 * :[name]!cho@ppy.sh PRIVMSG [channel_name] :[message]
 */

class NoSuchChannel extends Exception {
    public NoSuchChannel() {}

    public NoSuchChannel(String message) {
        super(message);
    }
}

public class StringUtils {
    public static List<String> parse(String message) throws NoSuchChannel {
        if (message.startsWith("PING")) return null;

        String[] data = message.split(":");
        String[] mData = data[1].split(" ");

        if (mData[1] == "QUIT") return null;

        List<String> retData = new ArrayList<String>();
        switch (mData[1]) {
            case "403":
                throw new NoSuchChannel(data[2]);
            case "353":
                retData.add("0");
                retData.add(mData[4]);
                retData.addAll(Arrays.asList(data[2].split(" ")));
                return retData;
            case "JOIN":
                retData.add("0");
                retData.add(data[2]);
                retData.add(mData[0].split("!")[0]);
                return retData;
            case "PRIVMSG":
                retData.add("1");
                retData.add(mData[2]);
                retData.add(mData[0].split("!")[0]);
                retData.add(data[2]);
                return retData;
            case "PART":
                retData.add("2");
                retData.add(data[2]);
                retData.add(mData[0].split("!")[0]);
                return retData;
            default:
                return null;
        }
    }
}
