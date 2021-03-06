package com.winspread.kalditest;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;


import com.dengjia.lib_share_asr.ShareAsrService;


import org.w3c.dom.Text;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

//    private TextView tv_eventRouter;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Need permissions for camera & microphone", 0, perms);
        }

        Intent intent = new Intent(this, ShareAsrService.class);
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        ShareAsrService.AsrResultBinder asrResultBinder = (ShareAsrService.AsrResultBinder) iBinder;
        ShareAsrService shareAsrService = asrResultBinder.getService();
        shareAsrService.addAsrResultListener(new ShareAsrService.AsrResultListener() {
            @Override
            public void onGetAsrResult(String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CharSequence text = textView.getText();
                        textView.setText(text+result);
                    }
                });
                Log.d(">>>>", "onGetAsrResult: " + result);
            }
        });

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


}