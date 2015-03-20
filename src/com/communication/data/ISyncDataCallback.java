package com.communication.data;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface ISyncDataCallback {
	public void onNullData();
	
	public void onBindSucess();

	public void onConnectSuccessed();
	
	public void onGetVersion(String version);
	
	public void onGetDeviceID(String deviceID);
	
	public void onUpdateTimeSuccessed();
	
	public void onUpdateAlarmReminderSuccessed();
	
	public void onBattery(int battery);
	
	public void onClearDataSuccessed();
	
	public void onGetDeviceTime(String time);
	
	public void onSyncDataProgress(int progress);
	
	public void onUpdateUserinfoSuccessed();
	
	public void onGetUserInfo(int height,int weigh,int age,int gender,int stepLength,int runLength,int sportType,int goalValue);
	
	/**
	 * 
	 * @param datas{
	 * curday_StartTime,  0
	 * curday_During,    1
	 * cur_Steps,  		2
	 * cur_Calories,	3
	 * cur_Metes, 		4
	   total_StartTime, 5
	   total_During     6
	   total_Steps, 	7
	   total_Calories, 	8
	   total_Metes, 	9
	   cur_SleepStartTime, 10
	   cur_SleepEndTime, 11
	   cur_SleepDuring,	12
	   sleepTotaltime, 	13
	   endTime}			14
	 *@param baos
	 * @return
	 */
	public void onSyncDataOver( HashMap<String, AccessoryValues> data,ByteArrayOutputStream baos);
	
	public void onGetOtherDatas(ArrayList<Integer> datas);
	
	public void onTimeOut();

}
