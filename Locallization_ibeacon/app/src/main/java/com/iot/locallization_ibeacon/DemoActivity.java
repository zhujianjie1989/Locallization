package com.iot.locallization_ibeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.Date;


public class DemoActivity extends Activity implements BluetoothAdapter.LeScanCallback{
    private GoogleMap map;
    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;
    private Marker currmark=null;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static LatLng position ;
    private double latitude=1.342518999;
    private double longitude =103.679474999;

    private float[] hw={188,23f};
    private boolean scan_flag= false;
    private GroundOverlay newarkMap;


    private WPL_Limit_BlutoothLocationAlgorithm location =new WPL_Limit_BlutoothLocationAlgorithm();
    LatLng NEWARK = new LatLng(1.342518999,103.679474999);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initMap();
        initBlueTooth();
        init_Button();
         updateHandler.postDelayed(update, 1000);
    }

    int count=0;
    public void onLeScan(final BluetoothDevice device,final int rssi, final byte[] scanRecord){
      new Thread(new Runnable() {
          @Override
          public void run() {
              BluetoothSensor beacon =  Tools.dealScan(device, rssi, scanRecord);
              if (GlabalData.blutoothSensorList.containsKey("major:"+beacon.major+" minor:"+beacon.minor)){
                  BluetoothSensor sensor = GlabalData.blutoothSensorList.get("major:"+beacon.major+" minor:"+beacon.minor);
                  //sensor.setRssi(rssi);
                  sensor.rssi=rssi;
                  sensor.updateTime = new Date().getTime();
                  Log.e("lescon",beacon.major + "  " + beacon.minor+"  "+rssi + "");
            /*if (count%6==0){
                updateHandler.postDelayed(update, 1000);*//**/
              }

          }
      }).start();


            count++;
    }


    private void updateMap()
    {
        location.setHandler(updatelog);
        location.DoLocalization();

        if (currmark!= null){
            currmark.remove();
        }
        currmark=map.addMarker(new MarkerOptions().position(GlabalData.currentPosition));
    }

    private Runnable update = new Runnable(){

        @Override
        public void run() {
            updateMap();
            updateHandler.postDelayed(update, 1500);
        }
    };

    private void  initMap(){

        map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setIndoorEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        newarkMap  = map.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.k44)).anchor(0,0).bearing(-45f)
                .position(NEWARK,hw[0], hw[1]));



        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(NEWARK, 23);
        map.moveCamera(update);

        File file = new File(Tools.path);
        if(file.exists()){
            Tools.ReadConfigFile();
        }


    }

    private void  initBlueTooth() {
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                /*position = new LatLng(location.getLatitude(),location.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(position, 18);
                map.animateCamera(update);*/
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,2000,0,locationListener);
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("zdafdaf", "ddfdfdsafdsafsafdsaf");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        };


    }


    private void  init_Button(){
        Button up = (Button)findViewById(R.id.BT_UP);
        Button down = (Button)findViewById(R.id.BT_DOWN);
        Button left = (Button)findViewById(R.id.BT_LEFT);
        Button right = (Button)findViewById(R.id.BT_RIGHT);

        final Button scan = (Button)findViewById(R.id.BT_scan);
        Button stop = (Button)findViewById(R.id.BT_stop);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                newarkMap.remove();
                newarkMap=map.addGroundOverlay(new GroundOverlayOptions().position(new LatLng(newarkMap.getPosition().latitude + 0.000002, newarkMap.getPosition().longitude), hw[0], hw[1]));
                //   drawLine();
                ((TextView)findViewById(R.id.TV_Lat)).setText(newarkMap.getPosition().latitude+"");

                ((TextView)findViewById(R.id.TV_Lng)).setText(newarkMap.getPosition().longitude+"");
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newarkMap.remove();
                newarkMap=map.addGroundOverlay(new GroundOverlayOptions().position(new LatLng(newarkMap.getPosition().latitude - 0.000002, newarkMap.getPosition().longitude), hw[0], hw[1]));
                //  drawLine();
                ((TextView)findViewById(R.id.TV_Lat)).setText(newarkMap.getPosition().latitude+"");

                ((TextView)findViewById(R.id.TV_Lng)).setText(newarkMap.getPosition().longitude+"");

            }
        });


        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newarkMap.remove();
                newarkMap=map.addGroundOverlay(new GroundOverlayOptions().position(new LatLng(newarkMap.getPosition().latitude , newarkMap.getPosition().longitude- 0.000002), hw[0], hw[1]));
                //  drawLine();
                ((TextView)findViewById(R.id.TV_Lat)).setText(newarkMap.getPosition().latitude+"");

                ((TextView)findViewById(R.id.TV_Lng)).setText(newarkMap.getPosition().longitude+"");
            }
        });


        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newarkMap.remove();
                newarkMap=map.addGroundOverlay(new GroundOverlayOptions().position(new LatLng(newarkMap.getPosition().latitude, newarkMap.getPosition().longitude+ 0.000002), hw[0], hw[1]));
                //  drawLine();
                ((TextView)findViewById(R.id.TV_Lat)).setText(newarkMap.getPosition().latitude+"");

                ((TextView)findViewById(R.id.TV_Lng)).setText(newarkMap.getPosition().longitude+"");
            }
        });



        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!scan_flag){
                    mBluetoothAdapter.startLeScan(DemoActivity.this);


                }
                scan_flag=true;


            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scan_flag){
                    mBluetoothAdapter.stopLeScan(DemoActivity.this);

                }
                scan_flag=false;

            }
        });

    }

    Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateMap();
        }
    };


    public static  String logstring ="";

    Handler updatelog = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TextView tx = (TextView)findViewById(R.id.textView5);
            tx.setText(logstring);
        }
    };


    protected void onPause(){
        super.onPause();
        mBluetoothAdapter.stopLeScan(this);
    }

}
