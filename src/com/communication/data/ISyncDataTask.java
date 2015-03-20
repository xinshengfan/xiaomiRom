package com.communication.data;

import com.communication.data.SaveManager.eSaveType;

public interface ISyncDataTask {
	public void start();
	public void stop();
	public void connectDevice();
	public void SendDataToDevice(int[] datas);
	public void setTryConnectCounts(int count);
	public void setSaveType(eSaveType saveType);
}
