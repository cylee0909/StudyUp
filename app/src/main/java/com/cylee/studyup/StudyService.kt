package com.cylee.studyup

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import android.widget.Toast
import androidx.core.app.NotificationCompat

class StudyService : AccessibilityService() {
    companion object {
        lateinit var INSTANCE: StudyService
        val UDP_PREFIX = "MOVE_ACTION"
        @JvmField
        var alive = false
    }

    var NOTIFICATION_ID = 0x123
    var sessionManager : MediaSessionManager? = null
    var mNotificationManager : NotificationManager? = null
    var udpServer : UDPServer? = null
    var mainHandler = Handler(Looper.getMainLooper())

    fun moveAction(action: Int) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (INSTANCE != null) {
                GestureHelper.moveDirection(INSTANCE, action, null)
            } else {
                Toast.makeText(applicationContext, "服务没启动额~", Toast.LENGTH_SHORT).show()
            }
        } else {
            mainHandler.post {
                moveAction(action)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName.equals(application.packageName) ||
                event.packageName.contains("inputmethod") ||
                event.packageName.contains("com.android")) return;
        }
    }

    fun exit() {
        disableSelf()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        alive = false
        udpServer?.stop()
    }

    /**
     * 初始化通知栏
     */
    fun initNotify() {
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val mNotificationBuilder = NotificationCompat.Builder(this, initChannelId())
            .setSmallIcon(R.drawable.ic_music)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle("抖音助手")
            .setContentText("刷抖音不用手")
        if (isJellyBeanMR1()) {
            mNotificationBuilder.setShowWhen(false)
        }
        val mNotification = mNotificationBuilder.build()
        startForeground(NOTIFICATION_ID, mNotification)
    }


    //判断是否是android 6.0
    fun isJellyBeanMR1(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
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

    override fun onInterrupt() {
        onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prepareSession()
        initNotify()
        updatePlaybackState()

        udpServer?.stop()
        udpServer = UDPServer()
        udpServer?.start {
            if (it.startsWith(UDP_PREFIX)) {
                var direction = it.subSequence(IntRange(UDP_PREFIX.length + 1, it.length - 1))
                if (direction == "UP") {
                    moveAction(1)
                } else if (direction == "DOWN") {
                    moveAction(0)
                }
            }
        }
        alive = true
        INSTANCE = this
        LogUtil.d("onServiceConnected")
    }
}