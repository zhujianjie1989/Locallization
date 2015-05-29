package com.iot.locallization_ibeacon.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.iot.locallization_ibeacon.pojo.BluetoothSensor;
import com.iot.locallization_ibeacon.pojo.GlabalData;

import java.util.Date;

public class ScanBluetoothService extends Service implements BluetoothAdapter.LeScanCallback{
    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public ScanBluetoothService() {
      //  initBlueTooth();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       // throw new UnsupportedOperationException("Not yet implemented");
        initBlueTooth();
        Log.e("localliziton", "onBind");
        return  new Binder();

    }

    private void  initBlueTooth()  {
        Log.e("localliziton", "initBlueTooth");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try{
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                throw new Exception("Bluetooth is not available");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        mBluetoothAdapter.startLeScan(this);


    }
    private int count =0;
    @Override
    public void onLeScan( BluetoothDevice device, int rssi,  byte[] scanRecord){


        String ibeaconName = device.getName();
        String mac      = device.getAddress();
        int startByte   = 24;
        int major       = (scanRecord[startByte + 1] & 0xff) * 0x100 + (scanRecord[startByte + 2] & 0xff);
        int minor       = (scanRecord[startByte + 3] & 0xff) * 0x100 + (scanRecord[startByte + 4] & 0xff);
        int txPower     = (scanRecord[startByte + 5]);
       // count++;
        BluetoothSensor beacon = new BluetoothSensor(ibeaconName, "",mac,major+"",minor+"",rssi,txPower);
        GlabalData.log= "major:"+beacon.major + " minor:" + beacon.minor + " rssi:" + rssi;

        Message msg = new Message();
        msg.arg1=1;
        GlabalData.handler.sendMessage(msg);

        Log.e("lescon", beacon.major + "  " + beacon.minor + "  " + rssi + " count : "+count );
        if (GlabalData.blutoothSensorList.containsKey("major:"+beacon.major+" minor:"+beacon.minor)){
            BluetoothSensor sensor = GlabalData.blutoothSensorList.get("major:"+beacon.major+" minor:"+beacon.minor);
            sensor.setRssi(rssi);
            sensor.updateTime = new Date().getTime();
            Log.e("lescon", beacon.major + "  " + beacon.minor + "  " + rssi + " count : "+count );
        }

        if (GlabalData.Templist.containsKey("major:"+beacon.major+" minor:"+beacon.minor)){
            BluetoothSensor sensor = GlabalData.Templist.get("major:"+beacon.major+" minor:"+beacon.minor);
            sensor.rssi=rssi;
        }else {
            GlabalData.Templist.put("major:" + beacon.major + " minor:" + beacon.minor, beacon);
        }


    }

}
