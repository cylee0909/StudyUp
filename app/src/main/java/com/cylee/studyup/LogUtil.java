package com.cylee.studyup;

import android.util.Log;

public class LogUtil {
    public static final String TAG = "STUDY_DEBUG";
    public static final boolean LOGENABLE = true;

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (LOGENABLE) {
            Log.d(tag, msg);
        }
    }
}
