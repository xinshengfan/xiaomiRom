package com.communication.sumsangble;

import android.bluetooth.BluetoothDevice;

public interface BLEConnectCallback {

	/**
	 * 
	 * @param value
	 * @param type
	 */
	public void onGetValueAndTypes(int value, int type);

	/**
	 * 
	 * @param device  if device is null, it seems timeout
	 * @return  if false, will connect device auto. else need connect manufacture
	 */
	public boolean onSeartchCallBack(BluetoothDevice device);

	/**
	 * 
	 * @param device
	 * @param status
	 * @param newState   2: connected  0: disconnected
	 * @return
	 */
	public boolean onConnectStateChanged(BluetoothDevice device, int status,
			int newState);
	
	public boolean onConnectTimeOut();

}
