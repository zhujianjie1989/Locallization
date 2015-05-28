package com.iot.locallization_ibeacon.pojo;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class BluetoothSensor {
	public String ID;  // sensor ID
	public String mac=""; // AP mac address
	public Integer rssi=-100;// AP rssi
	public String major;
	public String minor;
	public String Name;
	public String UUID;
	public int TxPower;
	public int max_rssi=-50;
	public Integer floor=0;
	public LatLng position ;
	public MarkerOptions markerOptions = new MarkerOptions();
	public List<Line> lines = new ArrayList<Line>();
	public long updateTime;

	private final  int length =3 ;
	public int[] rssis = new int[length];
	public int pos = 0;

	public BluetoothSensor(){

		for (int i = 0 ;i < length;i++)
		{
			rssis[i]=-20;
		}
	}

	public BluetoothSensor(String Name,String UUID,String Mac,String Major,String Minor,int Rssi,int TxPower){
		this.Name= Name;
		this.UUID= UUID;
		this.mac=Mac;
		this.major  =Major;
		this.minor = Minor;
		this.rssi= Rssi;
		this.TxPower = TxPower;

	}
	public void setRssi(int rssi){
		rssis[pos] = rssi;
		pos= (pos+1)%length;
		int sum=0;
		for (int i = 0 ;i < length;i++)
		{
			sum+=rssis[i];
		}
		this.rssi = sum/length;
	}


	public boolean containLine(String  major,String minor)
	{
		for (int i = 0 ; i < lines.size();i++){
			if(lines.get(i).major.equals(major)&&lines.get(i).minor.equals(minor)){
				return true;
			}
		}

		return  false;

	}

	public String toString(){
		return   this.ID+","+this.major+","+this.minor+","+this.rssi+","+this.position.latitude+","+ this.position.longitude+","+this.floor+""+this.max_rssi;
	}

}
