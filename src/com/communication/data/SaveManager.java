package com.communication.data;

import com.communication.provider.DeviceDataToDB;
import com.communication.provider.Java2Xml;

import android.content.Context;
import android.util.Log;

public class SaveManager {
	
	private String TAG="SaveManager";
	private Context mContext;
	private eSaveType mSaveType;
	
	private DeviceDataToDB mDeviceDataToDB;
	
	private Java2Xml mJava2Xml;
	public SaveManager(Context context,eSaveType saveType){
		mContext=context;
		mSaveType=saveType;
		if(mSaveType==eSaveType.XML){
			mJava2Xml=new Java2Xml(mContext);
		}else if(mSaveType==eSaveType.DATABSE){
			mDeviceDataToDB=new DeviceDataToDB(mContext);
		}
	}
	
	 
	/**
	 * 
	 * @param step
	 * @param calorie
	 * @param meter
	 * @param time
	 */
	public void addASportData(int step,int calorie,int meter,long time){
		if(mSaveType==eSaveType.DATABSE){
			mDeviceDataToDB.addASportData(step, calorie, meter, time);
		}else {
			mJava2Xml.addSportNode(step, calorie, meter, time);
		}
	}
	
	
	/**
	 * 
	 * @param level
	 * @param time
	 */
	public void addASleepData(int level, long time){
		if(mSaveType==eSaveType.DATABSE){
			Log.d(TAG, "addASleepData() level:"+level+",time:"+time);
			mDeviceDataToDB.addASleepData(level, time);
		}else{
			mJava2Xml.addSleepNode(level, time);
		}
	}
	
	/**
	 * 
	 */
	public void save(){
		if(mSaveType==eSaveType.DATABSE){
			mDeviceDataToDB.save();
		}else{
			mJava2Xml.save();
		}
	}
	
	public enum eSaveType{
		XML,DATABSE;
	}
}
