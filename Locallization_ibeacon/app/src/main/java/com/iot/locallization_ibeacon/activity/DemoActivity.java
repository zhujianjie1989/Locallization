package com.iot.locallization_ibeacon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iot.locallization_ibeacon.R;
import com.iot.locallization_ibeacon.algorithm.WPL_Limit_BlutoothLocationAlgorithm;
import com.iot.locallization_ibeacon.pojo.GlobalData;
import com.iot.locallization_ibeacon.tools.Tools;

import java.io.File;
import java.util.Date;


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
        GlobalData.loghandler = updatelog ;
        initMap();

        changeBuildingMap();
    }

    private void changeBuildingMap()
    {
        BitmapDescriptor img =null;
        Log.e("changeBuildingMap", " floor = " + GlobalData.curr_floor);
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
        Date date = new Date();
        if (Math.abs(date.getTime() - GlobalData.IPS_UpdateTime.getTime()) >  6000)
        {
            openGPSSettings();
            return;
        }

        if(locationManager!=null ){
            locationManager.removeUpdates(GPSlistener);
            locationManager = null;
        }

        location.setHandler(updatelog);
        location.DoLocalization();
        updateLocation(GlobalData.currentPosition);

    }
    public void updateLocation(LatLng location){
        if (currmark!= null)
        {
            currmark.remove();
        }
        currmark=map.addMarker(new MarkerOptions().position(location));
       // currmark=map.addMarker(new MarkerOptions().position(GlobalData.currentPosition));
    }

    private Location currentLocation =null;
    LocationManager locationManager;
    private void openGPSSettings() {

        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 2000, 0, GPSlistener);
            return;

        }

        Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(intent,0); //此为设置完成后返回到获取界面


    }

    LocationListener GPSlistener =  new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if(currentLocation!=null){
                if(Tools.isBetterLocation(location, currentLocation)){
                    Log.v("GPSTEST", "It's a better location");
                    currentLocation=location;
                    updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                }
                else{
                    Log.v("GPSTEST", "Not very good!");
                }
            }
            else if(location.getAccuracy() < 5)
            {
                Log.v("GPSTEST", "It's first location");
                currentLocation=location;
                updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            }

                /*    if (Tools.CalDistatce(new LatLng(location.getLatitude(), location.getLongitude()), GlobalData.currentPosition) > 15) {
                       return;
                    }
                    updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));*/
                   /* CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18);
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


    private Runnable updateMap = new Runnable()
    {
        @Override
        public void run()
        {
            updateMap();
            updateHandler.postDelayed(updateMap, 1500);
        }
    };


    private int count = 0;
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

            if (msg.arg1 == 2)
            {
                changeBuildingMap();
            }

            super.handleMessage(msg);
            TextView tx = (TextView)findViewById(R.id.textView5);
            tx.setText(logstring);
        }
    };

}
