package com.iot.locallization_ibeacon.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.iot.locallization_ibeacon.R;
import com.iot.locallization_ibeacon.pojo.BluetoothSensor;
import com.iot.locallization_ibeacon.pojo.GlabalData;
import com.iot.locallization_ibeacon.pojo.Line;
import com.iot.locallization_ibeacon.tools.Tools;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


public class Get_LanLng_Activity extends ActionBarActivity implements BluetoothAdapter.LeScanCallback{
    private GoogleMap map;
    private Hashtable<String,BluetoothSensor> markerList = new Hashtable<String,BluetoothSensor>();
    private Marker marker;

    private int markID=0;
    private String max_major="0";
    private String max_minor="0";
    private int max_rssi=-1000000;
    private boolean bluetooth = true;
    private BluetoothAdapter mBluetoothAdapter;
    private final Timer timer = new Timer();
    private TimerTask task;
    private boolean addLine_flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get__lan_lng);
        initButton();
        initMap();
        initBlueTooth();

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                Loghandler.sendMessage(message);
            }
        };

        timer.schedule(task, 500, 500);


    }

    public  void DrawLine(){
        Iterator<String>ite = GlabalData.blutoothSensorList.keySet().iterator();
        while(ite.hasNext())
        {
            BluetoothSensor sensor = GlabalData.blutoothSensorList.get(ite.next());
            Iterator<Line> Lite = sensor.lines.iterator();
            while(Lite.hasNext()){
                Line line = Lite.next();
                BluetoothSensor dist = GlabalData.blutoothSensorList.get(getID(line.major,line.minor));
                map.addPolyline(new PolylineOptions().add(sensor.markerOptions.getPosition()).add(dist.markerOptions.getPosition()));
            }
        }
    }

    public String getID(String ma ,String mi){
        return "major:" +  ma + " minor:" +  mi;
    }

    Handler Loghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextView log = (TextView) findViewById( R.id.TV_Log);
            BluetoothSensor max_sensor = Tools.getMaxRssiSensor(GlabalData.Templist);
            if (max_sensor==null)
                return;

      /*      TextView lat = (TextView) findViewById(R.id.TV_Lat);
            TextView lng = (TextView) findViewById(R.id.TV_Lng);
            lat.setText(curr_minor);
            lng.setText(sensor.rssi+"");*/
    //        Log.e("lescan","max_major:"+max_sensor.major+" max_minor:"+max_sensor.minor+" maxrssi:"+max_sensor.rssi +" rssi:"+ rssi);
            log.setText("max_major:" + max_sensor.major + " max_minor:" + max_sensor.minor + " maxrssi:" + max_sensor.rssi);

            super.handleMessage(msg);
        }
    };


    private void initButton(){
        Button SetConf = (Button)findViewById(R.id.BT_SetConf);
        Button delete = (Button)findViewById(R.id.BT_DELETE);
        Button calibrate = (Button)findViewById(R.id.BT_Calibreate);
        Button addline = (Button)findViewById(R.id.BT_addLine);

        SetConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlabalData.blutoothSensorList = markerList;
                File file = new File(Tools.path);
                if (file.exists()) {
                    file.delete();
                }
                Iterator<String> ite = markerList.keySet().iterator();
                while (ite.hasNext()) {
                    Tools.AppendToConfigFile(markerList.get(ite.next()));
                }

                Tools.ReadConfigFile();

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerList.remove(marker.getTitle());
                marker.remove();

            }
        });


        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothSensor max_sensor = Tools.getMaxRssiSensor( GlabalData.Templist);

                BluetoothSensor sensor =  GlabalData.blutoothSensorList.get(marker.getTitle());
                GlabalData.blutoothSensorList.remove(sensor.ID);

                sensor.major = max_sensor.major;
                sensor.minor = max_sensor.minor;
                sensor.max_rssi = max_sensor.rssi;

                sensor.ID = "major:" +  sensor.major + " minor:" +  sensor.minor;
                sensor.markerOptions.title(sensor.ID);
                sensor.markerOptions.snippet("x:" + Tools.formatFloat(sensor.position.latitude) + " y:" + Tools.formatFloat(sensor.position.longitude)+"\n"
                        +"max_rssi:" + sensor.max_rssi);

                marker.remove();

                marker =  map.addMarker(sensor.markerOptions);
                marker.showInfoWindow();

                GlabalData.blutoothSensorList.put(sensor.ID, sensor);
            }
        });

        addline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLine_flag = !addLine_flag;
                GlabalData.Templist.clear();

            }
        });


    }

    private void  initBlueTooth() {


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("zdafdaf", "ddfdfdsafdsafsafdsaf");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        };
        mBluetoothAdapter.startLeScan(Get_LanLng_Activity.this);


    }
    private void initMap(){
        map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (addLine_flag && Get_LanLng_Activity.this.marker!=null){
                    PolylineOptions rectOptions = new PolylineOptions().add(Get_LanLng_Activity.this.marker.getPosition()).add(marker.getPosition());
                    map.addPolyline(rectOptions);
                    BluetoothSensor sensor1 = markerList.get(Get_LanLng_Activity.this.marker.getTitle());
                    BluetoothSensor sensor2 = markerList.get(marker.getTitle());
                    if (!sensor1.containLine(sensor2.major,sensor2.minor)){
                        sensor1.lines.add(new Line(sensor2.major,sensor2.minor,0));
                        sensor2.lines.add(new Line(sensor1.major,sensor1.minor,0));
                    }

                }


                Get_LanLng_Activity.this.marker = marker;
                marker.setSnippet("x:" + Tools.formatFloat(marker.getPosition().latitude) + " y:" + Tools.formatFloat(marker.getPosition().longitude)
                        + "\n max_rssi:" + markerList.get(marker.getTitle()).max_rssi);
                markerList.get(marker.getTitle()).markerOptions.position(marker.getPosition());
                markerList.get(marker.getTitle()).position = marker.getPosition();
                return false;
            }
        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                TextView lat = (TextView) findViewById(R.id.TV_Lat);
                TextView lng = (TextView) findViewById(R.id.TV_Lng);
                lat.setText(Tools.formatFloat(latLng.latitude));
                lng.setText(Tools.formatFloat(latLng.longitude) + "");

                BluetoothSensor sensor = new BluetoothSensor();
                sensor.markerOptions =  new MarkerOptions().position(latLng).draggable(true).title(getID("111", markID+""))
                        .snippet("x:" + Tools.formatFloat(latLng.latitude) + " y:" + Tools.formatFloat(latLng.longitude) + "\n"
                                + "max_rssi:" + sensor.max_rssi);
                sensor.ID = sensor.markerOptions.getTitle();
                sensor.position= latLng;
                sensor.major = "111";
                sensor.minor= markID+"";
                markID++;

                map.addMarker(sensor.markerOptions);
                markerList.put(sensor.ID, sensor);



            }
        });


        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                TextView lat = (TextView) findViewById(R.id.TV_Lat);
                TextView lng = (TextView) findViewById(R.id.TV_Lng);
                lat.setText(Tools.formatFloat(marker.getPosition().latitude) + "");
                lng.setText(Tools.formatFloat(marker.getPosition().longitude) + "");


            }

            @Override
            public void onMarkerDrag(Marker marker) {
                TextView lat = (TextView) findViewById(R.id.TV_Lat);
                TextView lng = (TextView) findViewById(R.id.TV_Lng);
                lat.setText(Tools.formatFloat(marker.getPosition().latitude) + "");
                lng.setText(Tools.formatFloat(marker.getPosition().longitude) + "");

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                TextView lat = (TextView) findViewById(R.id.TV_Lat);
                TextView lng = (TextView) findViewById(R.id.TV_Lng);
                lat.setText(Tools.formatFloat(marker.getPosition().latitude) + "");
                lng.setText(Tools.formatFloat(marker.getPosition().longitude) + "");


                marker.setSnippet("x:" + Tools.formatFloat(marker.getPosition().latitude) + " y:" + Tools.formatFloat(marker.getPosition().longitude)
                        + "\n max_rssi:" + markerList.get(marker.getTitle()).max_rssi);
                markerList.get(marker.getTitle()).markerOptions.position(marker.getPosition());
                markerList.get(marker.getTitle()).position = marker.getPosition();

            }
        });


        GroundOverlayOptions newarkMap  = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.k44)).anchor(0,0).bearing(-45f)
                .position(Tools.ancer, Tools.hw[0], Tools.hw[1]);

        map.addGroundOverlay(newarkMap);

        File file = new File(Tools.path);
        if(file.exists()){
            Tools.ReadConfigFile();
            markerList=  GlabalData.blutoothSensorList;

            Iterator<String> ita= markerList.keySet().iterator();
            while(ita.hasNext())
            {
                map.addMarker( markerList.get(ita.next()).markerOptions);
            }
        }

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(Tools.ancer, 22);
        map.moveCamera(update);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        BluetoothSensor beacon =  Tools.dealScan(device, rssi, scanRecord);
      /*  if (!beacon.major.equals("888") || !beacon.minor.equals("1"))
            return;*/

        BluetoothSensor max_sensor =null;
        /*if (GlabalData.blutoothSensorList.containsKey("major:"+beacon.major+" minor:"+beacon.minor)){
            BluetoothSensor sensor = GlabalData.blutoothSensorList.get("major:"+beacon.major+" minor:"+beacon.minor);
            sensor.setRssi(rssi);
        }*/

        if (GlabalData.Templist.containsKey("major:"+beacon.major+" minor:"+beacon.minor)){
            BluetoothSensor sensor = GlabalData.Templist.get("major:"+beacon.major+" minor:"+beacon.minor);
            sensor.rssi=rssi;
            //sensor.setRssi(rssi);

        }else {
            GlabalData.Templist.put("major:" + beacon.major + " minor:" + beacon.minor, beacon);
        }

        max_sensor = Tools.getMaxRssiSensor(GlabalData.Templist);

        Log.e("lescan", "max_major:" + max_sensor.major + " max_minor:" + max_sensor.minor + " maxrssi:" + max_sensor.rssi + " rssi:" + rssi);


    }
    protected void onPause(){
        super.onPause();
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GlabalData.Templist.clear();
        mBluetoothAdapter.startLeScan(this);
    }
}

