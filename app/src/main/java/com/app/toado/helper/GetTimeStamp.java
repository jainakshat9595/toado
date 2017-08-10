package com.app.toado.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ghanendra on 28/06/2017.
 */

public class GetTimeStamp {
    static SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm aa");
    static SimpleDateFormat formatterdate = new SimpleDateFormat("MM-dd-yyyy");
    static SimpleDateFormat formattertime = new SimpleDateFormat("hh:mm aa");

    public static String timeStamp(){
        final String timestamp = formatter.format(Calendar.getInstance().getTime());
        return timestamp;
    }
    public static String timeStampDate(){
        final String timestamp = formatterdate.format(Calendar.getInstance().getTime());
        return timestamp;
    }
    public static String timeStampTime(){
        final String timestamp = formattertime.format(Calendar.getInstance().getTime());
        return timestamp;
    }


//    public static String timeStamp2(){
//        final String timestamp = formatter.format(Calendar.getInstance().getTime());
//        return timestamp;
//    }

    public static Long Id(){
        final long curTime = Calendar.getInstance().getTimeInMillis();
        return curTime;
    }
}
