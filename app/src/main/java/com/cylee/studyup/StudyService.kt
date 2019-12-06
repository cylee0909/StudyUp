package com.cylee.studyup

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.*

public class StudyService : AccessibilityService() {
    companion object {
        lateinit var INSTANCE: AccessibilityService
        var studyLaunched = false;
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        LogUtil.d("event = " + event)
        if (event?.eventType == TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName.equals("cn.xuexi.android")) {
                if (!studyLaunched) {
                    studyLaunched = true;
                    Toast.makeText(applicationContext, "要开始学习了吗？", Toast.LENGTH_SHORT).show();
                    handler.postDelayed({
                        startStudy()
                    }, 4000);
                }
            } else {
                studyLaunched = false;
            }
        }
    }

    fun play(count: Int, complete: Runnable?) {
        LogUtil.d("play " + count)
        playTab(object : Runnable {
            override fun run() {
                if (count > 0 && studyLaunched) {
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
        LogUtil.d("playTab click")
        Thread.sleep((Random().nextFloat() * 2000 + 1000).toLong());
        LogUtil.d("playTab monkey move")
        GestureHelper.monkeyMove(this, GestureHelper.random.nextInt(10) + 1, {
            complete?.run()
        })
    }


    fun startStudy() {
        Toast.makeText(applicationContext, "开始学习", Toast.LENGTH_SHORT).show();
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
        startForegroundService(Intent(applicationContext, HelperService::class.java))
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        LogUtil.d("onUnbind")
    }
}