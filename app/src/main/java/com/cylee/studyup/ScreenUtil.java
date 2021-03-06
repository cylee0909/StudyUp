package com.cylee.studyup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import java.util.concurrent.atomic.AtomicInteger;

public class ScreenUtil {
    public static int dp2px(float dp) {
        final float scale = App.getApplication().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dp(float px) {
        final float scale = App.getApplication().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static void toast(String msg) {
        Toast.makeText(App.getApplication(), msg, Toast.LENGTH_SHORT).show();
    }


    public static Rect getViewRect(View view, Rect rect) {
        if (rect == null) {
            rect = new Rect();
        }
        ((Activity) view.getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int decorViewTop = rect.top;
        view.getGlobalVisibleRect(rect);
        rect.top = rect.top - decorViewTop;
        rect.bottom = rect.bottom - decorViewTop;
        return rect;
    }

    /**
     * 或者View实际的位置（主要是减去了标题栏高度）
     *
     * @param view
     * @return
     */
    public static Rect getViewRect(View view) {
        return getViewRect(view, null);
    }

    public static DisplayMetrics getMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) App.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    public static Point getRealSize(Point in) {
        if (in == null) {
            in = new Point();
        }
        ((WindowManager)App.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(in);
        return in;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth() {
        return getMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight() {
        return getMetrics().heightPixels;
    }

    /**
     * 获取屏幕密度
     */
    public static float getScreenDensity() {
        return getMetrics().density;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int getGenerateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int i = generateViewId();
            return i;
        } else {
            int i = View.generateViewId();
            return i;
        }
    }

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /**
     * 获取屏幕最小宽度
     * @return float
     */
    public static float getSmallestWidth() {
        DisplayMetrics dm = getMetrics();
        int heightPixels = getScreenHeight();
        int widthPixels = getScreenWidth();
        float density = dm.density;
        float heightDP = heightPixels / density;
        float widthDP = widthPixels / density;
        float smallestWidthDP;
        if (widthDP < heightDP) {
            smallestWidthDP = widthDP;
        } else {
            smallestWidthDP = heightDP;
        }
        return smallestWidthDP;
    }
}
