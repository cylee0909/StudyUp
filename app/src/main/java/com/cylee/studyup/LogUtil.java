package com.cylee.studyup;

import android.util.Log;

public class LogUtil {
    public static final String TAG = "cylee : StudyService";
    public static final boolean LOGENABLE = true;

    public static void d(String msg) {
        if (LOGENABLE) {
            Log.d(TAG, msg);
        }
    }
}
