package com.cylee.studyup;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_PERMISSION = 101;
    private static final int REQUEST_CODE_SERVICE = 102;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.am_overlay_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQUEST_CODE_PERMISSION);
            }
        });

        findViewById(R.id.am_service).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQUEST_CODE_SERVICE);
            }
        });

        findViewById(R.id.am_test).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                StudyService.INSTANCE.initNotify();
                StudyService.INSTANCE.updatePlaybackState();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkEnv();
    }

    void checkEnv() {
//        boolean checkPermission = PermissionUtil.getAppOps(this);
        boolean checkService = checkAccessibilityOn(this);
//        if (!checkPermission) {
//            ScreenUtil.toast("请开启权Overlay限");
//            return;
//        }

        if (!checkService) {
            ScreenUtil.toast("请开启服务");
            return;
        }

        ScreenUtil.toast("可以开始学习了，请打开抖音！");
    }

    public static boolean checkAccessibilityOn(Context context) {
        return isAccessibilitySettingsOn(context, StudyService.class);
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + clazz.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
