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
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class HelperService extends Service {
    private static final String CHANNEL_ID = "HELP_SERVICE";
    View floatView;
    static boolean serviceAlive;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"HELP_SERVICE_NAME",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            notification = new Notification.Builder(getApplicationContext(),CHANNEL_ID).build();
        } else {
            notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("Study")
                    .setContentText("StudyUp!")
                    .build();
        }

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
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format= PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height =WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.alpha = 0.5f;
        params.y = ScreenUtil.dp2px(100);
        windowManager.addView(floatView, params);

        floatView.findViewById(R.id.fv_up).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                moveAction(1);
            }
        });
        floatView.findViewById(R.id.fv_down).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                moveAction(0);
            }
        });
        floatView.findViewById(R.id.fv_left).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                moveAction(2);
            }
        });
        floatView.findViewById(R.id.fv_right).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                moveAction(3);
            }
        });


        floatView.findViewById(R.id.fv_play).setOnClickListener(new View.OnClickListener(){
            int i = 0;
            @Override
            public void onClick(View v) {
                if (StudyService.INSTANCE != null) {
//                    StudyService.INSTANCE.startStudy();
//                    ReadHelper.startLearning(StudyService.INSTANCE);
//                    ReadHelper.selTab(StudyService.INSTANCE, (i++) % 4, null);
                    ReadHelper.navNextPageAndCheck(StudyService.INSTANCE, null, null);
                } else {
                    Toast.makeText(getApplicationContext(), "学习服务没启动额~", Toast.LENGTH_SHORT).show();
                }
            }
        });

        floatView.findViewById(R.id.fv_exit).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (StudyService.INSTANCE != null) {
                    StudyService.INSTANCE.exit();
                } else {
                    Toast.makeText(getApplicationContext(), "学习服务没启动额~", Toast.LENGTH_SHORT).show();
                }
            }
        });


        serviceAlive = true;
    }

    void moveAction(int action) {
        if (StudyService.INSTANCE != null) {
            GestureHelper.moveDirection(StudyService.INSTANCE, action, null);
        } else {
            Toast.makeText(getApplicationContext(), "学习服务没启动额~", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceAlive = false;
        stopForeground(true);
        LogUtil.d("HelperService onDestroy");
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.removeView(floatView);
    }
}
