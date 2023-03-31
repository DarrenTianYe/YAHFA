package com.android.flinger.yafya;

import android.util.Log;

/**
 * Created by yugq on 2019/4/29.
 */

public class yafyaLog {
//    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(LogUtils.class);

    static boolean debug = true;
    static String TAG = "Darren-yafyaLog";

    public static void log(String info) {
        if (debug) {
            Log.d(TAG, info);
        }
    }

    //error 信息默认全部输出
    public static void error(String info) {
        Log.e(TAG, info);
    }

    public static void error(String ssss, String info) {
        Log.e(TAG+":"+ssss, info);
    }

    public static void error(Throwable throwable) {
        Log.e(TAG, throwable.toString());
    }

    public static void warning(String info) {
        if (debug) {
            Log.w(TAG, info);
        }
    }

    public static void warning(String TAG, String info) {
        if (debug) {
            Log.w(TAG, info);
        }
    }

    public static void warning(Throwable throwable) {
        if (debug) {
            Log.w(TAG, throwable.toString());
        }
    }

}