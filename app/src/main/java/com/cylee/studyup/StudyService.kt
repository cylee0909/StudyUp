package com.cylee.studyup

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver

public class StudyService : AccessibilityService() {
    companion object {
        lateinit var INSTANCE: StudyService
        var studyAppLaunched = false
        @JvmField
        var alive = false
        var videoCompleteRunnable : Runnable? = null

        const val ACTION_NEXT = "com.cyl.music_lake.notify.next" // 下一首广播标志

        const val ACTION_PREV = "com.cyl.music_lake.notify.prev" // 上一首广播标志

        const val ACTION_PLAY_PAUSE = "com.cyl.music_lake.notify.play_state" // 播放暂停广播

    }

    var NOTIFICATION_ID = 0x123
    var windowStateChanged = false
    var sessionManager : MediaSessionManager? = null
    var mNotificationManager : NotificationManager? = null

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
        if (event?.eventType == TYPE_VIEW_FOCUSED) {
            if (!windowStateChanged) {
                windowStateChanged = true
            }
        }
    }

    fun stopHelpService() {
        if (HelperService.serviceAlive) {
            stopService(Intent(applicationContext, HelperService::class.java))
        }
    }

    fun resetWindowStateChange() {
        windowStateChanged = false
    }

    fun hasWindowStateChanged() : Boolean {
        return windowStateChanged
    }


    fun moveAction(action: Int) {
        if (INSTANCE != null) {
            GestureHelper.moveDirection(INSTANCE, action, null)
        } else {
            Toast.makeText(applicationContext, "服务没启动额~", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        LogUtil.d("AccessibilityEventDEBUG" , event.toString())

        if (event?.eventType == TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName.equals(application.packageName) ||
                event.packageName.contains("inputmethod") ||
                event.packageName.contains("com.android")) return;

            if (event.packageName.equals("com.ss.android.ugc.aweme")) {
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

    /**
     * 初始化通知栏
     */
    fun initNotify() {
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        val nowPlayingIntent = Intent(this, MainActivity::class.java)
        val mNotificationBuilder = NotificationCompat.Builder(this, initChannelId())
            .setSmallIcon(R.drawable.ic_music)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setContentIntent(clickIntent)
            .setContentTitle("抖音助手")
            .setContentText("刷抖音不用手")
//            .addAction(R.drawable.ic_skip_previous,
//                "上一首",
//                retrievePlaybackAction(ACTION_PREV))
//            .addAction(playButtonResId, title,
//                retrievePlaybackAction(ACTION_PLAY_PAUSE))
//            .addAction(R.drawable.ic_skip_next,
//                "下一首",
//                retrievePlaybackAction(ACTION_NEXT))
//            .setDeleteIntent(
//                MediaButtonReceiver.buildMediaButtonPendingIntent(
//                    this, PlaybackStateCompat.ACTION_STOP
//                )
//            )
        if (isJellyBeanMR1()) {
            mNotificationBuilder.setShowWhen(false)
        }
//        if (isLollipop()) {
//            //线控
//            mNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            val style = androidx.media.app.NotificationCompat.MediaStyle()
//                .setMediaSession(sessionManager?.getMediaSession())
//                .setShowActionsInCompactView(1, 0, 2, 3, 4)
//            mNotificationBuilder.setStyle(style)
//        }
        val mNotification = mNotificationBuilder.build()
//        mNotificationManager?.notify(NOTIFICATION_ID, mNotification)
        startForeground(NOTIFICATION_ID, mNotification)
    }

    private fun retrievePlaybackAction(action: String): PendingIntent? {
        val intent = Intent(action)
        intent.component =
            ComponentName(this, StudyService::class.java)
        return PendingIntent.getService(this, 0, intent, 0)
    }

    //判断是否是android 6.0
    fun isJellyBeanMR1(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
    }

    //判断是否是android 8.0
    fun isO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }


    //判断是否是android 6.0
    fun isMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    //判断是否是android 5.0
    fun isLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }


    private fun initChannelId(): String {
        // 通知渠道的id
        val id = "music_lake_01"
        // 用户可以看到的通知渠道的名字.
        val name: CharSequence = "音乐湖"
        // 用户可以看到的通知渠道的描述
        val description = "通知栏播放控制"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel: NotificationChannel
            mChannel = NotificationChannel(id, name, importance)
            mChannel.description = description
            mChannel.enableLights(false)
            mChannel.enableVibration(false)
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager?.createNotificationChannel(mChannel)
        }
        return id
    }

    fun prepareSession() {
        val musicService = object : IMusicServiceStub {
            override fun getCurrentPosition(): Long {
                return 1;
            }

            override fun isPlaying(): Boolean {
                return true;
            }

            override fun getListCount() : Long {
                return 1000;
            }

            override fun playPause() {
            }

            override fun next() {
                moveAction(1)
            }

            override fun prev() {
                moveAction(0)
            }

            override fun seekTo(pos: Int) {
            }
        }
        sessionManager = MediaSessionManager(musicService, applicationContext, Handler(Looper.getMainLooper()))
    }

    fun updatePlaybackState() {
        sessionManager?.updateMetaData(Music())
        sessionManager?.updatePlaybackState()
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        prepareSession()
        initNotify()
        updatePlaybackState()
        alive = true
        INSTANCE = this
        LogUtil.d("onServiceConnected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        LogUtil.d("onUnbind")
        stopHelpService()
    }
}