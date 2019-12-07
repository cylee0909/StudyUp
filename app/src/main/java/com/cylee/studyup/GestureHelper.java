package com.cylee.studyup;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class GestureHelper {
    static Handler handler = ReadHelper.handler;
    static Random random = new Random(System.currentTimeMillis());

    interface ActionFilter {
        boolean canMoveAction(int acition);
    }

    public static void monkeyMove(final AccessibilityService service, final int cnt, final Runnable complete) {
        monkeyMove(service, cnt, null, complete);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void monkeyMove(final AccessibilityService service, final int cnt, final ActionFilter actionFilter, final Runnable complete) {
        monkeyMove(service,  actionFilter, new Runnable() {
            @Override
            public void run() {
                if (cnt > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            monkeyMove(service, cnt -1, actionFilter, complete);
                        }
                    }, 2000 + (int)(random.nextFloat() * 2000));
                } else {
                    if (complete != null) {
                        complete.run();
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void monkeyMove(AccessibilityService service, ActionFilter actionFilter, Runnable runnable) {
        int c = random.nextInt(4);
        LogUtil.d("movePath c = "+c);
        if (actionFilter == null || actionFilter.canMoveAction(c)) {
            moveDirection(service, c, runnable);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void moveDirection(final AccessibilityService service, final int action, final int cnt, final Runnable runnable) {
        LogUtil.d("moveDirection action = "+action +" cnt = "+cnt);
        moveDirection(service, action, new Runnable() {
            @Override
            public void run() {
                if (cnt > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            moveDirection(service, action, cnt - 1, runnable);
                        }
                    }, 2000 + (int)(2000 * random.nextFloat()));
                } else {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void moveDirection(AccessibilityService service, int action, Runnable runnable) {
        Path path;
        switch (action) {
            case 0 :
                path = downPath();
                break;
            case 1:
                path = upPath();
                break;
            case 2:
                path = leftPath();
                break;
            default:
                path = rightPath();
        }
        movePath(service, path , runnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void tabHome(AccessibilityService service, final Runnable complete) {
        LogUtil.d("tabHome");
        float x = 0.5f;
        float y = 1f;
        Path path = makePath(x, y, x, y, false);
        movePath(service, path, complete);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void tabCenter(AccessibilityService service, final Runnable complete) {
        LogUtil.d("tabCenter");
        float x = centerArea();
        float y = centerArea();
        float diff = random.nextFloat() * 0.005f;
        Path path = makePath(x, y, x + diff, y + diff);
        movePath(service, path, complete);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void clipMoveUp(final AccessibilityService service, final int cnt, final Runnable runnable) {
        LogUtil.d("clipMoveUp cnt = "+cnt);
        clipMoveUp(service, new Runnable() {
            @Override
            public void run() {
                if (cnt > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clipMoveUp(service, cnt - 1, runnable);
                        }
                    }, 3000 + (int)(2000 * random.nextFloat()));
                } else {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void clipMoveUp(AccessibilityService service, Runnable complete) {
        LogUtil.d("clipMoveUp");
        float x = centerArea();
        float y = centerArea();
        float diff = random.nextFloat() * 0.005f;
        Path path = makePath(x, y, x + diff, clamp(y - random.nextFloat() * 0.2f + 0.1f));
        movePath(service, path, 1000 + random.nextInt(200), complete);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void movePath(AccessibilityService service, Path path, int duration, final Runnable complete) {
        if (!StudyService.INSTANCE.alive) return;
        LogUtil.d("movePath d = "+duration +" path = "+path);
        final AtomicBoolean comFlag = new AtomicBoolean(false);
        final GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(path, 0, duration, false);
        boolean result = service.dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if (!comFlag.get()) {
                    LogUtil.d("movePath onCompleted");
                    comFlag.set(true);
                    if (complete != null) {
                        complete.run();
                    }
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                LogUtil.d("movePath onCancelled");
            }
        }, handler);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!comFlag.get()) {
                    LogUtil.d("movePath onCompleted by hand");
                    comFlag.set(true);
                    if (complete != null) {
                        complete.run();
                    }
                }
            }
        }, duration + 100);

        LogUtil.d("movePath result = "+result);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void movePath(AccessibilityService service, Path path, final Runnable complete) {
        int d = random.nextInt(200) +  100;
        movePath(service, path, d, complete);
    }

    public static Path downPath() {
        float diff = randomDiff();
        float x = centerArea();
        float y = centerArea() - diff;
        return makePath(centerArea(), y, x + randomClip(), y + diff * 1.5f);
    }

    public static Path upPath() {
        float diff = randomDiff();
        float x = centerArea();
        float y = centerArea() + diff;
        return makePath(x, y, x + randomClip(), y - diff * 1.5f);
    }

    public static Path leftPath() {
        float diff = randomDiff();
        float x = centerArea() + diff;
        float y = centerArea();
        return makePath(x, y, x - diff * 1.5f, y + randomClip());
    }

    public static Path rightPath() {
        float diff = randomDiff();
        float x = centerArea() - diff;
        float y = centerArea();
        return makePath(x, y, x + diff * 2f, y + randomClip());
    }

    static float randomDiff() {
        return random.nextFloat() * 0.3f + 0.2f;
    }

    static float centerArea() {
        return random.nextFloat() * 0.2f + 0.4f;
    }

    static float randomClip() {
        return random.nextFloat() * 0.05f;
    }

    static float clamp(float value) {
        float min = random.nextFloat() * 0.1f + 0.1f;
        float max = 1 - min;

        return Math.min(Math.max(value, min), max);
    }

    public static Path makePath(float x, float y, float dstX, float dstY) {
        return makePath(x, y, dstX, dstY, true);
    }

    public static Path makePath(float x, float y, float dstX, float dstY, boolean clamp) {
        if (clamp) {
            x = clamp(x);
            y = clamp(y);
            dstX = clamp(dstX);
            dstY = clamp(dstY);
        }
        LogUtil.d("makePath d = "+x+" " +y + " "+dstX +" "+dstY);
        int dx = (int) (x * ScreenUtil.getScreenWidth());
        int dy = (int) (y * ScreenUtil.getScreenHeight());
        int dw = (int) ((dstX - x) * ScreenUtil.getScreenWidth());
        int dh = (int) ((dstY - y) * ScreenUtil.getScreenHeight());

        Path path = new Path();
        path.moveTo(dx, dy);
//        int cnt = random.nextInt(5) + 4;
        int cnt = 1;
        float diff = (random.nextFloat() - 0.5f) * 0.1f;
        LogUtil.d("makePath d = "+dx+" " +dy + " "+dw +" "+dh +" cnt = "+cnt);
        for (int i = 1; i < cnt + 1; i++) {
            path.lineTo(dx + dw * (i/(float)cnt + diff), dy + dh *(i/(float)cnt + diff));
        }
        return path;
    }
}
