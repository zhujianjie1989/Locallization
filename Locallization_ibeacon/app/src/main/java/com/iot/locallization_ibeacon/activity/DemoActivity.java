package com.iot.locallization_ibeacon.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iot.locallization_ibeacon.R;
import com.iot.locallization_ibeacon.algorithm.WPL_Limit_BlutoothLocationAlgorithm;
import com.iot.locallization_ibeacon.pojo.GlobalData;
import com.iot.locallization_ibeacon.tools.Tools;

import java.io.File;


public class DemoActivity extends Activity {
    private GoogleMap map;
    private Marker currmark=null;
    private boolean scan_flag= false;
    private GroundOverlay buildingMapImage =null;
    private WPL_Limit_BlutoothLocationAlgorithm location =new WPL_Limit_BlutoothLocationAlgorithm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initMap();
        init_Button();

    }

    private void changeBuildingMap()
    {
        BitmapDescriptor img =null;
        switch(GlobalData.curr_floor)
        {
            case 1:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k11);
                break;
            case 2:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k22);
                break;
            case 3:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k33);
                break;
            case 4:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k44);
                break;
            default:
                return;
        }
        buildingMapImage.remove();
        buildingMapImage = map.addGroundOverlay(new GroundOverlayOptions()
                .image(img).anchor(0, 0).bearing(-45f)
                .position(GlobalData.ancer, GlobalData.hw[0], GlobalData.hw[1]));
    }

    private void updateMap()
    {
        changeBuildingMap();
        location.setHandler(updatelog);
        location.DoLocalization();

        if (currmark!= null)
        {
            currmark.remove();
        }
        currmark=map.addMarker(new MarkerOptions().position(GlobalData.currentPosition));
    }

    private void  initMap()
    {

        map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setIndoorEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        buildingMapImage = map.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.k44)).anchor(0,0).bearing(-45f)
                .position(GlobalData.ancer,GlobalData.hw[0],GlobalData.hw[1]));

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(GlobalData.ancer, 23);
        map.moveCamera(update);

        File file = new File(Tools.path);
        if(file.exists())
        {
            Tools.ReadConfigFile();
        }

        updateHandler.postDelayed(updateMap, 1000);
    }

    private void  init_Button()
    {
        final Button scan = (Button)findViewById(R.id.BT_scan);
        Button stop = (Button)findViewById(R.id.BT_stop);

        scan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (!scan_flag){

                }
                scan_flag=true;
            }
        });

        stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (scan_flag){

                }
                scan_flag=false;

            }
        });

    }
    private Runnable updateMap = new Runnable()
    {
        @Override
        public void run()
        {
            updateMap();
            updateHandler.postDelayed(updateMap, 1500);
        }
    };

    Handler updateHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            updateMap();
        }
    };


    public static  String logstring ="";

    Handler updatelog = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            if (msg.arg1==2)
            {
                changeBuildingMap();
            }

            super.handleMessage(msg);
            TextView tx = (TextView)findViewById(R.id.textView5);
            tx.setText(logstring);
        }
    };

}
