package com.cylee.controllclient

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.absoluteValue


class MainActivity : AppCompatActivity() {
    val dataQueue = LinkedBlockingQueue<String>()
    var running = false

    var downX = 0f
    var downY = 0f

    companion object {
        val UDP_PORT = 8894
        val BROADCAST_IP = "255.255.255.255"
    }

    var socket : DatagramSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = findViewById<View>(R.id.root_view)
        view.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    downX = motionEvent.getX(0)
                    downY = motionEvent.getY(0)
                }

                MotionEvent.ACTION_UP -> {
                    val dx = motionEvent.getX(0) - downX
                    val dy = motionEvent.getY(0) - downY
                    val distance = if (dx.absoluteValue > dy.absoluteValue) dx else dy
                    var message =
                        if (distance > 0) {
                            "MOVE_ACTION DOWN"
                        } else {
                            "MOVE_ACTION UP"
                        }
                    dataQueue.add(message)
                }
            }
            true
        }

        setWindowBrightness(0)

        start()
    }

    private fun setWindowBrightness(brightness: Int) {
        val window: Window = window
        val lp: WindowManager.LayoutParams = window.getAttributes()
        lp.screenBrightness = brightness / 255.0f
        window.setAttributes(lp)
    }


    fun start() {
        running = true
        Thread {
            socket = DatagramSocket()
            var local: InetAddress? = null
            local = InetAddress.getByName(BROADCAST_IP)
            while (running) {
                try {
                    val message = dataQueue.take()
                    val bytes = message.toByteArray()
                    val p = DatagramPacket(bytes, bytes.size, local, UDP_PORT)
                    socket?.broadcast = true
                    socket?.send(p)
                    Log.d("cylee", "send msg "+message)
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    fun stop() {
        running = false
        socket?.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }
}