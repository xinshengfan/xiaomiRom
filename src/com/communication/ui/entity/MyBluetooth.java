package com.communication.ui.entity;

import android.bluetooth.BluetoothDevice;

public class MyBluetooth {
	private BluetoothDevice device;
	private int rssi;

	public MyBluetooth() {
		super();
	}

	public MyBluetooth(BluetoothDevice device, int rssi) {
		super();
		this.device = device;
		this.rssi = rssi;
	}

	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o instanceof MyBluetooth) {
			MyBluetooth bluetooth = (MyBluetooth) o;
			return bluetooth.getDevice().getAddress()
					.equals(this.getDevice().getAddress());
		}
		return super.equals(o);
	}
}
