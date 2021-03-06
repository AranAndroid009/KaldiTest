/**
 * Copyright 2020 JiaDeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dengjia.lib_share_asr;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dengjia.lib_share_asr.grammer.Action;
import com.dengjia.lib_share_asr.grammer.Device;
import com.dengjia.lib_share_asr.grammer.Place;
import com.dengjia.lib_share_asr.grammer.Wakeup;

import org.json.JSONException;
import org.json.JSONObject;
import org.kaldi.Assets;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechRecognizer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * ShareASR功能开启基本流程：
 *
 * 1. 加载.so资源
 * 2. 在线程中加载models
 * 3. 加载唤醒词回复资源
 * 4. 加载Skills资源
 * 5. 运行拾音器
 * 6. 得到识别结果
 * 7. 对识别结果进行处理
 *
 */
public class ShareAsrService extends Service implements RecognitionListener {

    private static final String TAG = "MyService";

    private static HashMap<Integer, Integer> rawHash = new HashMap<>();

    private Model model;
    private SpeechRecognizer recognizer;

    static {
        System.loadLibrary("kaldi_jni");
    }

    public ShareAsrService() {
    }

    @Override
    public void onPartialResult(String hypothesis) {
        // Log.e(TAG, hypothesis + "\n");
    }

    @Override
    public void onResult(String hypothesis) {

        // 这里是获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());

        if (hypothesis != null) {
            JSONObject object = null;
            try {
                object = new JSONObject(hypothesis);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String result = null;
            try {
                result = object.getString("text");
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                // 这里是拿到识别结果后的处理逻辑
                if (result != null) {
                    if (result.contains(Wakeup.WAKEUP.getWakeUpWord())) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(this, rawHash.get((int)(Math.random()*9+1)));
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mediaPlayer.release();
                                }
                            });
                        }
                        Log.e(TAG, "识别结果\n" + simpleDateFormat.format(date) + "\n onResult：\n" + hypothesis);
                        asrResultListener.onGetAsrResult(result);
                    }else {
                        Boolean flag_1 = false;
                        Boolean flag_2 = false;
                        Boolean flag_3 = false;

                        // 依次检测三个语槽
                        for (Action action : Action.values()) {
                            if (result.contains(action.getAction())) {
                                flag_1 = true;
                            }
                        }
                        for (Place place : Place.values()) {
                            if (result.contains(place.getPlace())) {
                                flag_2 = true;
                            }
                        }
                        for (Device device : Device.values()) {
                            if (result.contains(device.getDevice())) {
                                flag_3 = true;
                            }
                        }

                        if (flag_1 && flag_2 && flag_3) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.action_done);
                            if (mediaPlayer != null) {
                                mediaPlayer.start();
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        mediaPlayer.release();
                                    }
                                });
                            }
                            Log.e(TAG, "识别结果\n" + simpleDateFormat.format(date) + "\n onResult：\n" + hypothesis);
                            asrResultListener.onGetAsrResult(result);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onError(Exception error) {
        Log.d(TAG, "onError: " + error);
    }

    @Override
    public void onTimeout() {
        recognizer.cancel();
        recognizer = null;
    }

    private class LoadModels extends Thread{
        WeakReference<ShareAsrService> serviceReference;
        LoadModels(ShareAsrService service){
            serviceReference = new WeakReference<>(service);
        }

        @Override
        public void run() {
            try {
                Assets assets = new Assets(serviceReference.get());
                File assetDir = assets.syncAssets();
                Log.e(TAG, "查看是否获取到了Models" + assetDir.toString());
                serviceReference.get().model = new Model(assetDir.toString() + "/model-android");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 普通识别模式函数
    public void recognizeMicrophone() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer = null;
        } else {
            try {
                recognizer = new SpeechRecognizer(model);
                recognizer.addListener(this);
                recognizer.startListening();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AsrResultBinder();
    }

    public class AsrResultBinder extends Binder {
        public ShareAsrService getService(){
            return ShareAsrService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "我被执行了！onCreate");

        LoadModels loadModels = new LoadModels(this);
        loadModels.run();

        // 添加音频资源
        rawHash.put(1, R.raw.tone_wake_1);
        rawHash.put(2, R.raw.tone_wake_2);
        rawHash.put(3, R.raw.tone_wake_3);
        rawHash.put(4, R.raw.tone_wake_4);
        rawHash.put(5, R.raw.tone_wake_5);
        rawHash.put(6, R.raw.tone_wake_6);
        rawHash.put(7, R.raw.tone_wake_7);
        rawHash.put(8, R.raw.tone_wake_8);
        rawHash.put(9, R.raw.tone_wake_9);

        recognizeMicrophone();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "我被执行了！onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Service!销毁");
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "内存不足！！！");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }




    private AsrResultListener asrResultListener;


    public void addAsrResultListener(AsrResultListener asrResultListener){
        this.asrResultListener = asrResultListener;
    }

    public interface AsrResultListener{
        void onGetAsrResult(String result);
    }

}
