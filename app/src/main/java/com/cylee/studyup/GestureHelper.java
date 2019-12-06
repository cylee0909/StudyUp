package com.cylee.studyup;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class GestureHelper {
    static Handler handler = new Handler(Looper.getMainLooper());
    static Random random = new Random(System.currentTimeMillis());

    public static void monkeyMove(final AccessibilityService service, final int cnt, final Runnable complete) {
        movePath(service, random.nextBoolean() ? randomXPath() : randomYPath(), new Runnable() {
            @Override
            public void run() {
                if (cnt > 0) {
                    monkeyMove(service, cnt -1, complete);
                } else {
                    if (complete != null) {
                        complete.run();
                    }
                }
            }
        });
    }

    public static void monkeyMove(AccessibilityService service, Runnable runnable) {
        LogUtil.d("movePath");
       movePath(service, random.nextBoolean() ? randomXPath() : randomYPath(), runnable);
    }

    public static void movePath(AccessibilityService service, Path path, final Runnable complete) {
//        if (!StudyService.Companion.getStudyLaunched()) return;

        path = new Path();
        path.moveTo(300, 200);
        path.lineTo(300, 800);

        int d = random.nextInt(600) +  100;
        LogUtil.d("movePath d = "+d +" path = "+path);
        final GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(path, 0, d, false);
        service.dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                LogUtil.d("movePath onCompleted");
                if (complete != null) {
                    complete.run();
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                LogUtil.d("movePath onCancelled");
            }
        }, handler);
    }

    public static Path randomXPath() {
        return makePath(random.nextFloat() * 0.5f + 0.2f, random.nextFloat() * 0.3f + 0.1f, random.nextFloat() * 0.2f + 0.3f, random.nextFloat() * 0.1f);
    }

    public static Path randomYPath() {
        return makePath(random.nextFloat() * 0.5f + 0.2f, random.nextFloat() * 0.3f + 0.1f, random.nextFloat() * 0.1f, random.nextFloat() * 0.1f + 0.3f);
    }

    public static Path makePath(float x, float y, float w, float h) {
        LogUtil.d("makePath d = "+x+" " +y + " "+w +" "+h);
        int dx = (int) (x * ScreenUtil.getScreenWidth());
        int dy = (int) (y * ScreenUtil.getScreenHeight());
        int dw = (int) (w * ScreenUtil.getScreenWidth());
        int dh = (int) (h * ScreenUtil.getScreenHeight());

        Path path = new Path();
        path.moveTo(dx, dy);
//        int cnt = random.nextInt(5) + 4;
        int cnt = 2;
        float diff = (random.nextFloat() - 0.5f) * 0.1f;
        LogUtil.d("makePath d = "+dx+" " +dy + " "+dw +" "+dh +" cnt = "+cnt);
        for (int i = 0; i < cnt; i++) {
            path.lineTo(dx + dw * (i/(float)cnt + diff), dy + dh *(i/(float)cnt + diff));
        }
        return path;
    }
}
