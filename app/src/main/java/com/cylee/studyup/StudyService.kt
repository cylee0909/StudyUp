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
        var studyAppLaunched = false
        @JvmField
        var alive = false
        var videoCompleteRunnable : Runnable? = null
    }

    override fun onInterrupt() {
        stopHelpService()
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

    fun stopHelpService() {
        if (HelperService.serviceAlive) {
            stopService(Intent(applicationContext, HelperService::class.java))
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
                }
                if (!HelperService.serviceAlive) {
                    startForegroundService(Intent(applicationContext, HelperService::class.java))
                }
            } else {
                if (studyAppLaunched) {
                    studyAppLaunched = false;
                    stopHelpService()
                }
            }
        }
    }

    fun exit() {
        disableSelf()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHelpService()
        alive = false;
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        alive = true;
        INSTANCE = this;
        LogUtil.d("onServiceConnected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        LogUtil.d("onUnbind")
        stopHelpService()
    }
}