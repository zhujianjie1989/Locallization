package com.iot.locallization_ibeacon.algorithm;

import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;
import com.iot.locallization_ibeacon.pojo.BluetoothSensor;
import com.iot.locallization_ibeacon.activity.DemoActivity;
import com.iot.locallization_ibeacon.pojo.GlabalData;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class WPL_Limit_BlutoothLocationAlgorithm extends BluetoothLocalizationAlgorithm {

	private int length=6;   // select a few number of signal
	private Handler handler;
	public void setHandler(Handler h){
		handler=h;
	}
	@Override
	public void DoLocalization( )
		{
			Double rssi_max=-40.0;
			Double rssi_min=-100.0;
			Double sumDef=0.0;
			double x=0;
			double y=0;
			List<BluetoothSensor> sensorList = SortWifiSignal();
			DemoActivity.logstring =sensorList.get(0).minor+" "+(sensorList.get(0).rssi-sensorList.get(0).max_rssi)+"\n";
			DemoActivity.logstring +=sensorList.get(1).minor+" "+(sensorList.get(1).rssi-sensorList.get(1).max_rssi)+"\n";
			DemoActivity.logstring +=sensorList.get(2).minor+" "+(sensorList.get(2).rssi-sensorList.get(2).max_rssi)+"\n";

			handler.sendMessage(new Message());


			int len=0;
			if (length>sensorList.size())
			{
				len=sensorList.size();
			}
			else {
				len=length;
			}
			
			for (int i = 0; i < len; i++) {
				BluetoothSensor sensor = sensorList.get(i);
				rssi_max = sensor.max_rssi*1.0;

				Double tmprssi = rssi_max-sensor.rssi;
				if (tmprssi>rssi_max-rssi_min)
					tmprssi=rssi_max-rssi_min;
				Double def= 1.0 / Math.pow(10, (0.8*tmprssi/10));
				sumDef=sumDef+def;
				x= x + def * sensor.position.latitude;
				y= y + def * sensor.position.longitude;
			}
			x = x / sumDef;
			y = y / sumDef;
			GlabalData.currentPosition = new LatLng(x,y);

			Date time = new Date();
			for (int i = 0 ; i< sensorList.size()  ;i++)
			{
				//Log.e("timeeeee",sensorList.get(i).minor+"   "+(time.getTime()-sensorList.get(i).updateTime.getTime())/1000+"");
				if ((time.getTime()-sensorList.get(i).updateTime)/1000 > 6)
					sensorList.get(i).rssi = -100;
			}

		}
	
	private List<BluetoothSensor> SortWifiSignal()
	{
		List<BluetoothSensor> signalList = new ArrayList<BluetoothSensor>();
		Iterator<Entry<String, BluetoothSensor>> iter = GlabalData.blutoothSensorList.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, BluetoothSensor> entry = (Entry<String, BluetoothSensor>) iter.next();
			BluetoothSensor sensor = entry.getValue();
			signalList.add(sensor);
		}
		
		for (int i = 0; i < signalList.size()-1; i++) {
			for (int j = i+1; j < signalList.size(); j++) {
				int rssi_i=signalList.get(i).rssi-signalList.get(i).max_rssi;
				int rssi_j=signalList.get(j).rssi-signalList.get(j).max_rssi;
				if (rssi_i<rssi_j)
				{
					BluetoothSensor s=signalList.remove(j);
					signalList.add(i, s);
				}
			}
		}
		return signalList;
	}

}
