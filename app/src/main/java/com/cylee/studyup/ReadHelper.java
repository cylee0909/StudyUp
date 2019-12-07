package com.cylee.studyup;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadHelper {
    static Handler handler = new Handler(Looper.getMainLooper());
    static final int TAB_BAILING = 0;
    static final int TAB_HOME = 1;
    static final int TAB_DIAN_SHI_TAI = 2;
    static final int TAB_DIAN_TAI = 3;
    static Random random = new Random(System.currentTimeMillis());
    static String[] homeID = {
            "百灵",
            "学习",
            "电视台",
            "电台"
    };

    public static int randomWaitTime() {
        return 2000 + (int) (2000 * random.nextFloat());
    }

    public static void startLearning(final StudyService service) {
        handler.removeCallbacksAndMessages(null);
        LogUtil.d("startLearning");
        playTab(service, random.nextInt(2) + 2, new Runnable() {
            @Override
            public void run() {
                LogUtil.d("playTab complete");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.d("readArticle start");
                        readArticle(service, new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.d("readArticle complete");
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.d("watchVideo start");
                                        watchVideo(service, new Runnable() {
                                            @Override
                                            public void run() {
                                                LogUtil.d("watchVideo end");
                                                service.exit();
                                            }
                                        });
                                    }
                                }, randomWaitTime());
                            }
                        });
                    }
                }, randomWaitTime());
            }
        });
    }


    static void playTab(final StudyService service, final int cnt, final Runnable runnable) {
        playTab(service, new Runnable() {
            @Override
            public void run() {
                if (cnt > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playTab(service, cnt - 1, runnable);
                        }
                    }, randomWaitTime());
                } else {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
        });
    }

    static void playTab(final StudyService service, final Runnable runnable) {
        LogUtil.d("playTab ");
        final int index = random.nextInt(homeID.length);
        selTab(service, index, new Runnable() {
            @Override
            public void run() {
                GestureHelper.monkeyMove(service, GestureHelper.random.nextInt(10) + 1,
                        index == TAB_HOME ? new GestureHelper.ActionFilter() {
                            @Override
                            public boolean canMoveAction(int acition) {
                                if (acition >= 2) {
                                    return false;
                                }
                                return true;
                            }
                        } : null,
                        new Runnable() {
                            @Override
                            public void run() {
                                if (runnable != null) {
                                    runnable.run();
                                }
                            }
                        });
            }
        });
    }


    public static void readArticle(final StudyService service, final Runnable complete) {
        handler.removeCallbacksAndMessages(null);
        selTab(service, TAB_HOME, new Runnable() {
            @Override
            public void run() {
                // 随便翻翻
                GestureHelper.monkeyMove(service, random.nextInt(2), new Runnable() {
                    @Override
                    public void run() {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 再左划一次
                                GestureHelper.moveDirection(service, 2, random.nextInt(2) + 1, new Runnable() {
                                    @Override
                                    public void run() {
                                        readArticleByCount(service, 6 + random.nextInt(2), complete);
                                    }
                                });
                            }
                        }, randomWaitTime());
                    }
                });
            }
        });
    }

    public static void readArticleByCount(final StudyService service, final int cnt, final Runnable runnable) {
        LogUtil.d("read article cnt = " + cnt);
        readSigleArticle(service, new Runnable() {
            @Override
            public void run() {
                if (cnt > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            readArticleByCount(service, cnt - 1, runnable);
                        }
                    }, randomWaitTime());
                } else {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
        });
    }

    private static void readSigleArticle(final StudyService service, final Runnable runnable) {
        // 向下翻翻
        GestureHelper.moveDirection(service, 1, random.nextInt(1), new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navNextPageAndCheck(service, new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.d("tabCenter complete");
                                GestureHelper.clipMoveUp(service, 3 + random.nextInt(3), new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.d("clipMoveUp complete");
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                clickBackKey(service, runnable);
                                            }
                                        }, randomWaitTime());
                                    }
                                });
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                if (runnable != null) {
                                    runnable.run();
                                }
                            }
                        });
                    }
                }, randomWaitTime());
            }
        });
    }

    public static void navNextPageAndCheck(final StudyService service, final Runnable success, final Runnable fail) {
        service.resetWindowStateChange();
        GestureHelper.tabCenter(service, new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (service.hasWindowStateChanged()) {
                            if (success != null) {
                                success.run();
                            }
                        } else {
                            if (fail != null) {
                                fail.run();
                            }
                        }
                    }
                }, randomWaitTime());
            }
        });
    }

    public static void watchVideo(final StudyService service, final Runnable complete) {
        handler.removeCallbacksAndMessages(null);
        selTab(service, TAB_DIAN_SHI_TAI, new Runnable() {
            @Override
            public void run() {
                // 随便翻翻
                GestureHelper.monkeyMove(service, random.nextInt(3), new Runnable() {
                    @Override
                    public void run() {
                        watchVideosByCount(service, 6 + random.nextInt(2), complete);
                    }
                });
            }
        });
    }

    public static void watchVideosByCount(final StudyService service, final int cnt, final Runnable runnable) {
        LogUtil.d("watch video cnt = " + cnt);
        watchSigleVideo(service, new Runnable() {
            @Override
            public void run() {
                if (cnt > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            watchVideosByCount(service, cnt - 1, runnable);
                        }
                    }, randomWaitTime());
                } else {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
        });
    }

    public static void watchSigleVideo(final StudyService service, final Runnable runnable) {
        // 向下翻翻
        GestureHelper.moveDirection(service, random.nextBoolean() ? 1 : 2, random.nextInt(2), new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navNextPageAndCheck(service, new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.d("tabCenter complete");
                                final AtomicBoolean completeFlag = new AtomicBoolean(false);
                                if (random.nextFloat() < 0.7f) {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!completeFlag.get()) {
                                                service.handCompleteVideo();
                                            }
                                        }
                                    }, (int) (1.5 * 60 * 1000 + randomWaitTime()));
                                }

                                service.registerVideoComplete(new Runnable() {
                                    @Override
                                    public void run() {
                                        completeFlag.set(true);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                clickBackKey(service, runnable);
                                            }
                                        }, randomWaitTime());
                                    }
                                });
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                if (runnable != null) {
                                    runnable.run();
                                }
                            }
                        });
                    }
                }, randomWaitTime());
            }
        });
    }

    public static void clickBackKey(AccessibilityService service, Runnable runnable) {
        LogUtil.d("clickBackKey complete");
        if (service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)) {
            if (runnable != null) {
                handler.postDelayed(runnable, 500);
            }
        }
    }


    public static void selTab(AccessibilityService service, int tabIndex, final Runnable runnable) {
        LogUtil.d("selTab tabIndex = " + tabIndex);
        Rect rect = new Rect();
        AccessibilityNodeInfo node = service.getRootInActiveWindow();
        if (tabIndex < homeID.length) {
            String id = homeID[tabIndex];
            List<AccessibilityNodeInfo> nodeInfos = null;
//            if (tabIndex == TAB_HOME) {
//                nodeInfos = node.findAccessibilityNodeInfosByViewId(id);
//            } else {
            nodeInfos = node.findAccessibilityNodeInfosByText(id);
//            }
            if (tabIndex == TAB_HOME) {
                GestureHelper.tabHome(service, new Runnable() {
                    @Override
                    public void run() {
                        if (runnable != null) {
                            handler.postDelayed(runnable, 500);
                        }
                    }
                });
                return;
            }

            if (nodeInfos != null && nodeInfos.size() > 0) {
                for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
                    if (nodeInfo.isClickable()) {
                        nodeInfo.getBoundsInScreen(rect);
                        if (rect.bottom < ScreenUtil.getScreenHeight() / 2) continue;
                        if (rect.left > rect.right || rect.left < 0 || rect.right > ScreenUtil.getScreenWidth())
                            continue;
                        if (nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                            if (runnable != null) {
                                handler.postDelayed(runnable, 500);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
