package com.iot.locallization_ibeacon.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.iot.locallization_ibeacon.R;
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



}

