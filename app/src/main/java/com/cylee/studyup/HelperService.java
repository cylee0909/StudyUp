package com.cylee.studyup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class HelperService extends Service {
    private static final String CHANNEL_ID = "HELP_SERVICE";
    View floatView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"HELP_SERVICE_NAME",
                NotificationManager.IMPORTANCE_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(getApplicationContext(),CHANNEL_ID).build();
        startForeground(1, notification);

        LogUtil.d("HelperService onCreate");
        floatView = View.inflate(getApplicationContext(), R.layout.float_view, null);
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            params.type =WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type =WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format= PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height =WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.alpha = 0.5f;
        windowManager.addView(floatView, params);

        floatView.findViewById(R.id.fv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StudyService.INSTANCE != null) {
                    GestureHelper.movePath(StudyService.INSTANCE, GestureHelper.randomYPath(), null);
                } else {
                    Toast.makeText(getApplicationContext(), "学习服务没启动额~", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d("HelperService onDestroy");
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.removeView(floatView);
    }
}
