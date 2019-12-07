package com.cylee.studyup

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SELECTED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.*

public class StudyService : AccessibilityService() {
    companion object {
        lateinit var INSTANCE: StudyService
        var studyAppLaunched = false;
        var videoCompleteRunnable : Runnable? = null
    }

    var handler = Handler(Looper.getMainLooper());

    var homeID = arrayListOf<String>(
        "百灵",
        "home_bottom_tab_icon_large",
        "电视台",
        "电台"
    );

    override fun onInterrupt() {
    }

    fun registerVideoComplete(runnable: Runnable) {
        videoCompleteRunnable = runnable;
    }

    fun handCompleteVideo() {
        videoCompleteRunnable?.run()
        videoCompleteRunnable = null
    }

    fun checkVideoComplete(event: AccessibilityEvent?) {
        if (event?.eventType == TYPE_VIEW_SELECTED && event?.className == "android.widget.SeekBar") {
            if (event?.currentItemIndex >= event.itemCount) {
                videoCompleteRunnable?.run()
                videoCompleteRunnable = null
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        LogUtil.d("AccessibilityEventDEBUG" , event.toString())
        if (event?.packageName?.equals("cn.xuexi.android") ?: false) {
            checkVideoComplete(event)
        }

        if (event?.eventType == TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName.equals(application.packageName) ||
                event.packageName.contains("inputmethod") ||
                event.packageName.contains("com.android")) return;

            if (event.packageName.equals("cn.xuexi.android")) {
                if (!studyAppLaunched) {
                    studyAppLaunched = true;
                    if (!HelperService.serviceAlive) {
                        startForegroundService(Intent(applicationContext, HelperService::class.java))
                    }
                }
            } else {
                if (studyAppLaunched) {
                    studyAppLaunched = false;
                    if (HelperService.serviceAlive) {
                        stopService(Intent(applicationContext, HelperService::class.java))
                    }
                }
            }
        }
    }

    fun play(count: Int, complete: Runnable?) {
        LogUtil.d("play " + count)
        playTab(object : Runnable {
            override fun run() {
                if (count > 0 && studyAppLaunched) {
                    play(count - 1, complete)
                } else {
                    complete?.run()
                }
            }
        })
    }


    fun playTab(complete: Runnable?) {
        LogUtil.d("playTab ")
        var random = homeID[Random().nextInt(homeID.size)];
        var node = rootInActiveWindow.findAccessibilityNodeInfosByText(random)?.firstOrNull()
        if (node == null) {
            node = rootInActiveWindow.findAccessibilityNodeInfosByViewId(random)?.firstOrNull()
        }
        node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        LogUtil.d("playTab click "+random)
        handler.postDelayed(object : Runnable {
            override fun run() {
                LogUtil.d("playTab monkey move")
                GestureHelper.monkeyMove(this@StudyService, GestureHelper.random.nextInt(10) + 1, {
                    complete?.run()
                })
            }
        }, 1000)
    }


    public fun startStudy() {
        play(GestureHelper.random.nextInt(3) + 2, object : Runnable {
            override fun run() {
                LogUtil.d("play complete")
                rootInActiveWindow.findAccessibilityNodeInfosByText("我的")?.firstOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        })
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        INSTANCE = this;
        LogUtil.d("onServiceConnected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        LogUtil.d("onUnbind")
    }
}