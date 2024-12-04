package osuIRC.Backend;

import osuIRC.Exception.NoSuchChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * :cho.ppy.sh 353 [nick] = [channel_name] :[name_list]
 * :[name]!cho@ppy.sh PRIVMSG [channel_name] :[message]
 * :cho.ppy.sh 403 [nick] [channel_name] :No such channel
 */

public class StringUtils {
    public static List<String> parse(String message) throws NoSuchChannel {
        List<String> data = Arrays.asList(message.split(":")),
                     retData = new ArrayList<>();

        String[] mData = data.get(1).split(" ");

        if (mData[1].equals("QUIT")) return retData;

        switch (mData[1]) {
            case "401":
            case "403":
                throw new NoSuchChannel(data.get(3));
            case "323":
            case "353":
                retData.add("0");
                retData.add(mData[4]);
                retData.addAll(Arrays.asList(data.get(2).split(" ")));
                break;
            case "JOIN":
                retData.add("0");
                retData.add(data.get(2));
                retData.add(mData[0].split("!")[0]);
                break;
            case "PRIVMSG":
                retData.add("1");
                retData.add(mData[2]);
                retData.add(mData[0].split("!")[0]);
                retData.add(String.join(
                        ":",
                        data.subList(2, data.size())
                ));
                break;
            case "PART":
                retData.add("2");
                retData.add(data.get(2));
                retData.add(mData[0].split("!")[0]);
                break;
        }
        return retData;
    }
}
