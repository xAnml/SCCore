package net.anmlmc.SCCore.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anml on 1/7/16.
 */
public class Utils {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public String longToTimeStamp(long time) {
        return DATE_FORMAT.format(new Date(time));
    }

    public String longToString(long time) {
        int days = (int) time / 86400;
        int hours = (int) (time - (days * 86400)) / 24;
        int minutes = (int) (time - (days * 86400) - (hours * 24)) / 60;
        int seconds = (int) (time - (days * 86400) - (hours * 24) - (minutes * 60)) + 1;

        String length = "";
        if (days != 0) {
            length += days + " day";
            if (days > 1) length += "s";
        }
        if (hours != 0) {
            length += hours + " hour";
            if (days > 1) length += "s";
        }
        if (minutes != 0) {
            length += minutes + " minute";
            if (days > 1) length += "s";
        }
        if (seconds != 0) {
            length += seconds + " second";
            if (days > 1) length += "s";
        }

        return length;
    }

    public long stringToSeconds(String string) {
        if (string.equals("0") || string.equals("")) return 0;
        String[] lifeMatch = new String[]{"d", "h", "m", "s"};
        int[] lifeInterval = new int[]{86400, 3600, 60, 1};
        long seconds = 0L;

        for (int i = 0; i < lifeMatch.length; i++) {
            Matcher matcher = Pattern.compile("([0-9]*)" + lifeMatch[i]).matcher(string);
            while (matcher.find()) {
                try {
                    seconds += Integer.parseInt(matcher.group(1)) * lifeInterval[i];
                } catch (NumberFormatException e) {
                    return 0;
                }
            }

        }
        return seconds;
    }

    public long stringToMilliSeconds(String string) {
        return stringToSeconds(string) * 1000;
    }


}