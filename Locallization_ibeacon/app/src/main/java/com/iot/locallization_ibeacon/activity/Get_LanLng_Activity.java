package com.iot.locallization_ibeacon.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
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


public class Get_LanLng_Activity extends ActionBarActivity {
    private GoogleMap map;
    private Hashtable<String,BluetoothSensor> markerList = new Hashtable<String,BluetoothSensor>();
    private Marker marker;
    private int markID=0;
    private final Timer timer = new Timer();
    private TimerTask task;
    private boolean addLine_flag=false;
    private boolean curr_or_max=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get__lan_lng);
        initButton();
        initMap();

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                Loghandler.sendMessage(message);
            }
        };
        timer.schedule(task, 500, 500);
        GlabalData.handler = Loghandler;
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
            if (msg.arg1 == 1){

                TextView log3 = (TextView) findViewById( R.id.TV_Log3);
                log3.setText(GlabalData.log);

            }
            TextView log1 = (TextView) findViewById( R.id.TV_Log1);
            TextView log2 = (TextView) findViewById( R.id.TV_Log2);
            if (marker !=null){
                BluetoothSensor sensor =  GlabalData.blutoothSensorList.get(marker.getTitle());
                if (sensor!=null)
                {
                    BluetoothSensor max_sensor = Tools.getSensorByMajorandMinor(sensor.major,sensor.minor);
                    if (max_sensor==null)
                        return;
                    log1.setText("cur_major:" + max_sensor.major + " cur_minor:" + max_sensor.minor + " rssi:" + max_sensor.rssi);
                }

            }



            BluetoothSensor max_sensor = Tools.getMaxRssiSensor(GlabalData.Templist);
            if (max_sensor==null)
                return;
            log2.setText("max_major:" + max_sensor.major + " max_minor:" + max_sensor.minor + " rssi:" + max_sensor.rssi);



            super.handleMessage(msg);
        }
    };


    private void initButton(){

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        Button SetConf = (Button)findViewById(R.id.BT_SetConf);
        Button delete = (Button)findViewById(R.id.BT_DELETE);
        Button calibrate = (Button)findViewById(R.id.BT_Calibreate);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonId = group.getCheckedRadioButtonId();

               if(radioButtonId == R.id.RB_Curr){
                   curr_or_max = false;
               }else   if(radioButtonId == R.id.RB_Max){
                   curr_or_max = true;
               }
            }
        });

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

                TextView log = (TextView) findViewById( R.id.TV_Log1);
                if (marker!=null && curr_or_max==false){
                    BluetoothSensor sensor =  GlabalData.blutoothSensorList.get(marker.getTitle());
                    BluetoothSensor max_sensor = Tools.getSensorByMajorandMinor(sensor.major,sensor.minor);
                    if (max_sensor==null)
                        return;
                    log.setText("major:" + max_sensor.major + " minor:" + max_sensor.minor + " rssi:" + max_sensor.rssi);
                    //BluetoothSensor sensor =  GlabalData.blutoothSensorList.get(marker.getTitle());
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

                }else if (marker!=null && curr_or_max==true){
                    BluetoothSensor sensor =  GlabalData.blutoothSensorList.get(marker.getTitle());
                    BluetoothSensor max_sensor = Tools.getMaxRssiSensor(GlabalData.Templist);
                    if (max_sensor==null)
                        return;
                    log.setText("major:" + max_sensor.major + " minor:" + max_sensor.minor + " rssi:" + max_sensor.rssi);
                    //BluetoothSensor sensor =  GlabalData.blutoothSensorList.get(marker.getTitle());
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

              //  BluetoothSensor max_sensor = Tools.getMaxRssiSensor( GlabalData.Templist);

            }
        });

     /*   addline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLine_flag = !addLine_flag;
                GlabalData.Templist.clear();

            }
        });*/


    }
    private void initMap(){
        map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker= null;
                Log.e("initMap","onMapClick");
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (addLine_flag && Get_LanLng_Activity.this.marker != null) {
                    PolylineOptions rectOptions = new PolylineOptions().add(Get_LanLng_Activity.this.marker.getPosition()).add(marker.getPosition());
                    map.addPolyline(rectOptions);
                    BluetoothSensor sensor1 = markerList.get(Get_LanLng_Activity.this.marker.getTitle());
                    BluetoothSensor sensor2 = markerList.get(marker.getTitle());
                    if (!sensor1.containLine(sensor2.major, sensor2.minor)) {
                        sensor1.lines.add(new Line(sensor2.major, sensor2.minor, 0));
                        sensor2.lines.add(new Line(sensor1.major, sensor1.minor, 0));
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
}

