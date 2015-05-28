package com.iot.locallization_ibeacon.tools;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.iot.locallization_ibeacon.pojo.BluetoothSensor;
import com.iot.locallization_ibeacon.pojo.GlabalData;
import com.iot.locallization_ibeacon.pojo.Line;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by zhujianjie on 19/5/2015.
 */
public class Tools extends  Activity {
    public static  String path="/sdcard/sensorInfo.txt";
    private static BluetoothAdapter mBluetoothAdapter;
    private static double coefficient1= 0.42093;
    private static double coefficient2= 6.9476;
    private static double coefficient3 =0.54992;
    private static String TAG = "Tools";
    public static float[] hw={188,23f};
    public static LatLng ancer = new LatLng(
            1.342518999,103.679474999
    );
    public static void AppendToConfigFile(BluetoothSensor sensor) {
        try {

            FileWriter writer = new FileWriter("/sdcard/sensorInfo.txt",true);
            String msg = sensor.ID+","+sensor.major+","+sensor.minor+","+sensor.position.latitude+","+ sensor.position.longitude+","+sensor.floor+","+sensor.max_rssi+",";
            Iterator<Line> ita = sensor.lines.iterator();
            while(ita.hasNext())
            {
                Line line = ita.next();
                msg= msg + line.major+"M"+line.minor+"N";
            }
            writer.write(msg+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteToConfigFile(BluetoothSensor sensor) {
        try {

            FileWriter writer = new FileWriter("/sdcard/sensorInfo.txt");
            String msg = sensor.ID+","+sensor.major+","+sensor.minor+","+sensor.position.latitude+","+ sensor.position.longitude+","+sensor.floor+","+sensor.max_rssi+",";
            Iterator<Line> ita = sensor.lines.iterator();
            while(ita.hasNext())
            {
                Line line = ita.next();
                msg= msg + line.major+"M"+line.minor+"N";
            }
            writer.write(msg+"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void ReadConfigFile()  {
        try{
            BufferedReader br = new BufferedReader(new FileReader("/sdcard/sensorInfo.txt"));
            String data = br.readLine();
            GlabalData.blutoothSensorList.clear();
            while( data!=null) {
                BluetoothSensor sensor = new BluetoothSensor();
                String[] info = data.split(",");
               // sensor.mac = info[0];
                sensor.ID = info[0];
                sensor.major= info[1];
                sensor.minor= info[2];
                sensor.position = new LatLng(Double.parseDouble(info[3]),Double.parseDouble(info[4])) ;
                sensor.floor =Integer.parseInt(info[5]);
                sensor.max_rssi = Integer.parseInt(info[6]);

                if(info.length > 7){
                    String[] lines = info[7].split("N");
                    Log.e("debug",info[7]+"    "+lines.length);
                    for (int i = 0; i < lines.length; i++){
                        String[] line = lines[i].split("M");
                        String major = line[0];
                        String minor = line[1];
                        sensor.lines.add(new Line(major,minor,0));

                    }
                }



                sensor.markerOptions.title(sensor.ID).draggable(true);
                sensor.markerOptions.position(sensor.position);
                sensor.markerOptions.snippet("x:" + sensor.position.latitude + "y:" + sensor.position.latitude + "\n max_rssi:" + sensor.max_rssi);
                GlabalData.blutoothSensorList.put(sensor.ID, sensor);
                Log.e("ReadConfigFile", sensor.toString());
                data = br.readLine();
            }
            br.close();
        }catch (IOException e) {
                e.printStackTrace();
        }

    }

    public static String formatFloat(double num)
    {
        DecimalFormat decimalFormat=new DecimalFormat(".00000");
        String p=decimalFormat.format(num);
        return  p;
    }

    public static  String  direction(LatLng src ,LatLng dist){
        HttpOperationUtils httpOperationUtils = new HttpOperationUtils();
        String url = "http://maps.google.com/maps/api/directions/xml?";
        String param = "origin=" + src.latitude + "," + src.longitude + "&destination=" +dist.latitude
                + "," +dist.longitude + "&sensor=false&mode=walking";
        Log.e("dddd", url + param);
        return  httpOperationUtils .doGet(url+param);
    }
    private  LocationManager locationManager;
    private  LocationListener locationListener;

    private  void  initBlueTooth() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

     //   Log.d(TAG, "calculating distance based on mRssi of %s and txPower of %s", rssi, txPower);


        double ratio = rssi*1.0/txPower;
        double distance;
        if (ratio < 1.0) {
            distance =  Math.pow(ratio,10);
        }
        else {
            distance =  (coefficient1)*Math.pow(ratio,coefficient2) + coefficient3;
        }
        //Log.d(TAG, "avg mRssi: %s distance: %s", rssi, distance);
        return distance;
    }

    public static  BluetoothSensor dealScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        int startByte = 2;
        boolean patternFound = false;
        // 寻找ibeacon
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies   iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies correct data length
                patternFound = true;
                break;
            }
            startByte++;
        }
        // 如果找到了的话
      //  if (patternFound) {
            // 转换为16进制
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            // ibeacon的UUID值
            String uuid = hexString.substring(0, 8) + "-"
                    + hexString.substring(8, 12) + "-"
                    + hexString.substring(12, 16) + "-"
                    + hexString.substring(16, 20) + "-"
                    + hexString.substring(20, 32);

            // ibeacon的Major值
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                    + (scanRecord[startByte + 21] & 0xff);

            // ibeacon的Minor值
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                    + (scanRecord[startByte + 23] & 0xff);

            String ibeaconName = device.getName();
            String mac = device.getAddress();
            int txPower = (scanRecord[startByte + 24]);

            BluetoothSensor beacon = new BluetoothSensor(ibeaconName, uuid,mac,major+"",minor+"",rssi,txPower);

            return  beacon;
     //   }
        //return  null;



    }
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static BluetoothSensor getMaxRssiSensor(Hashtable<String, BluetoothSensor> list){
        BluetoothSensor  max_sensor=null;
        String max_key;
        int max_rssi=-10000;
      Iterator<String> keyite =  list.keySet().iterator();
        while (keyite.hasNext()){
            String key = keyite.next();
            BluetoothSensor sensor = list.get(key);
            if (sensor.rssi > max_rssi){
                max_rssi = sensor.rssi;
                max_sensor = sensor;
            }
        }
        return  max_sensor;
    }


    public static  void printList(){
        Iterator<String> keyite =  GlabalData.blutoothSensorList.keySet().iterator();
        while (keyite.hasNext()){
            String key = keyite.next();
            BluetoothSensor sensor = GlabalData.blutoothSensorList.get(key);
          //  Log.e("printList",)
        }
    }

}
