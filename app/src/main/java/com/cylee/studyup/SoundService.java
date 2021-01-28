package com.cylee.studyup;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.baidu.aip.asrwakeup3.core.inputstream.InFileStream;
import com.baidu.aip.asrwakeup3.core.recog.IStatus;
import com.baidu.aip.asrwakeup3.core.wakeup.MyWakeup;
import com.baidu.aip.asrwakeup3.core.wakeup.listener.IWakeupListener;
import com.baidu.aip.asrwakeup3.core.wakeup.listener.RecogWakeupListener;
import com.baidu.speech.asr.SpeechConstant;

import java.util.HashMap;
import java.util.Map;

public class SoundService  implements IStatus {
    protected MyWakeup myWakeup;
    private int status = STATUS_NONE;
    private Context mContext;
    protected Handler handler = new Handler(Looper.getMainLooper()) {
        /*
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null && msg.obj != null) {
                Log.d("cylee", msg.obj.toString());
            }
        }
    };

    public void init(Context context) {
        mContext = context;
        InFileStream.setContext(context);

        IWakeupListener listener = new RecogWakeupListener(handler);
        myWakeup = new MyWakeup(context, listener);
    }

    public void start() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下

        // params.put(SpeechConstant.ACCEPT_AUDIO_DATA,true);
        // params.put(SpeechConstant.IN_FILE,"res:///com/baidu/android/voicedemo/wakeup.pcm");
        // params里 "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        myWakeup.start(params);
    }

    protected void stop() {
        myWakeup.stop();
    }

    /**
     * 销毁时需要释放识别资源。
     */
    protected void onDestroy() {
        myWakeup.release();
    }
}
