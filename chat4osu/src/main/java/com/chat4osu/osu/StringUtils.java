package com.chat4osu.osu;

import android.annotation.SuppressLint;

import com.chat4osu.osu.Exception.NoSuchChannel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
 * :cho.ppy.sh 353 [nick] = [channel_name] :[name_list]
 * :[name]!cho@ppy.sh PRIVMSG [channel_name] :[message]
 */

public class StringUtils {
    public static List<String> parse(String message) throws NoSuchChannel {
        if (message.startsWith("PING")) return null;

        List<String> data = Arrays.asList(message.split(":"));
        String[] mData = data.get(1).split(" ");

        if (mData[1].equals("QUIT")) return null;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        List<String> retData = new ArrayList<>();
        switch (mData[1]) {
            case "403":
                throw new NoSuchChannel(data.get(2));
            case "323":
                retData.add("0");
                retData.add(mData[4]);
                retData.addAll(Arrays.asList(data.get(2).split(" ")));
                return retData;
            case "JOIN":
                retData.add("0");
                retData.add(data.get(2));
                retData.add(mData[0].split("!")[0]);
                return retData;
            case "PRIVMSG":
                retData.add("1");
                retData.add(mData[2]);
                retData.add(mData[0].split("!")[0]);
                retData.add(formatter.format(new Date()));
                retData.add(String.join(
                        ":",
                        data.subList(2, data.size())
                ));
                return retData;
            case "PART":
                retData.add("2");
                retData.add(data.get(2));
                retData.add(mData[0].split("!")[0]);
                return retData;
            default:
                return null;
        }
    }
}
