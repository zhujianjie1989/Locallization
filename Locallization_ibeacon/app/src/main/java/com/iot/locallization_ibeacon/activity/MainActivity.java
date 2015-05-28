package com.iot.locallization_ibeacon.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.iot.locallization_ibeacon.R;
import com.iot.locallization_ibeacon.service.ScanBluetoothService;
import com.iot.locallization_ibeacon.tools.Tools;

import java.io.File;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_Button();

       File file = new File(Tools.path);
        if(file.exists()){
            Tools.ReadConfigFile();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent service = new Intent(MainActivity.this,ScanBluetoothService.class);
                bindService(service,new ScanServiceConnection(),BIND_AUTO_CREATE);
            }
        }).start();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(new ScanServiceConnection());
        Log.e("localliziton","onDestroy");
    }

    private void  init_Button() {
        Button Get_Lanlng = (Button) findViewById(R.id.BT_Get_Lanlng);
        Button Demo = (Button) findViewById(R.id.BT_Demo);


        Get_Lanlng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Get_LanLng_Activity.class);
                startActivity(intent);
            }
        });

        Demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DemoActivity.class);
                startActivity(intent);
            }
        });


    }
    private class ScanServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
  ;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    }


}

