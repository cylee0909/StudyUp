package com.cylee.studyup;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer {
    public volatile boolean running = false;
    private volatile DatagramSocket datagramSocket;

    public void start(final IDataCallBack callBack) {
        running = true;
        new Thread(){
            @Override
            public void run() {
                // UDP服务器监听的端口
                Integer port = 8894;
                // 接收的字节大小，客户端发送的数据不能超过这个大小
                byte[] message = new byte[1024];
                try {
                    // 建立Socket连接
                    datagramSocket = new DatagramSocket(port);
                    DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                        while (running) {
                            try {
                                // 准备接收数据
                                datagramSocket.receive(datagramPacket);
                                String data = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                                Log.d("cylee", "receive msg "+data);

                                if (callBack != null) {
                                    callBack.onDataReceived(data);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void stop() {
        running = false;
        if (datagramSocket != null) {
            datagramSocket.disconnect();
        }
    }

    public interface IDataCallBack {
        void onDataReceived(String data);
    }
}
