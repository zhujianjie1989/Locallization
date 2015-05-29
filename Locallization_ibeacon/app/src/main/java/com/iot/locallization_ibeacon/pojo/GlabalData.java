package com.iot.locallization_ibeacon.pojo;

import android.os.Environment;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.Hashtable;

public class GlabalData {
	// WIFIsensor map, <macAddress,WiFiSensor>
	public static Hashtable<String, BluetoothSensor> blutoothSensorList = new Hashtable<String, BluetoothSensor>();
	public static Hashtable<String, BluetoothSensor> Templist = new Hashtable<String, BluetoothSensor>();
	// Current position
	public static LatLng currentPosition ;
	
	private static File sd = Environment.getExternalStorageDirectory();
	public static String path = sd.getPath() + "/WiFiSensor";
	public static Handler handler ;
	public static String log;

}
