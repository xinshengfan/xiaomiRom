package com.communication.ble;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;

public interface IConnectCallback {
  
  public void connectState(BluetoothDevice device, int status,
			int newState);
 public void getValue(int value);
 public void getValues(ArrayList<Integer> list);
 public void discovered();
}
