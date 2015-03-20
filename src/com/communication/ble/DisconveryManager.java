package com.communication.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.communication.data.TimeoutCheck;
import com.communication.data.TimeoutCheck.ITimeoutCallback;

public class DisconveryManager {
	private Context mContext;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothAdapter.LeScanCallback mLeScanCallback;

	private OnSeartchCallback mOnSeartchCallback;

	private static final int SEARTCH_BY_CLASSIC = 0x1111;

	private TimeoutCheck mTimeoutCheck;
	
	private int time_out = 15000;
	private boolean isScanBLEStart;
	private boolean isScanClassic;
	private Handler mSeartchChangeHander = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == SEARTCH_BY_CLASSIC) {
				startClassicSeartch();
			}
		}

	};

	public DisconveryManager(Context context, OnSeartchCallback onScanCallback) {
		mContext = context;
		this.mOnSeartchCallback = onScanCallback;
		mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

			@Override
			public void onLeScan(BluetoothDevice device, int rssi,
					byte[] scanRecord) {
				// TODO Auto-generated method stub
				boolean isInterrupt = false;
				if (null != mOnSeartchCallback) {
					isInterrupt = mOnSeartchCallback.onSeartch(device);
				}

				/**
				 * 应用端不使用， 那么继续操作
				 */
				if (!isInterrupt) {
					if (null != device) {
						String deviceType = device.getName();
						if (null == deviceType) {
							isScanBLEStart = false;
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							seartchByClassic();
						}
					}
				}
			}
		};

		final BluetoothManager bluetoothManager = (BluetoothManager) mContext
				.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		mTimeoutCheck = new TimeoutCheck(new ITimeoutCallback() {
			
			@Override
			public void onReceivedFailed() {
				// TODO Auto-generated method stub
				timeOutAction();
			}
			
			@Override
			public void onReSend() {
				// TODO Auto-generated method stub
				timeOutAction();
			}
			
			@Override
			public void onReConnect(int tryConnectIndex) {
				// TODO Auto-generated method stub
				timeOutAction();
			}
			
			@Override
			public void onConnectFailed(int tryConnectIndex) {
				// TODO Auto-generated method stub
				timeOutAction();
			}
		});
		
		mTimeoutCheck.setTryConnectCounts(1);
		mTimeoutCheck.setIsConnection(false);
		mTimeoutCheck.setTimeout(time_out);   
	}

	/**
	 * startSearch
	 */
	public boolean startSearch() {
		mTimeoutCheck.startCheckTimeout();
//		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		isScanBLEStart = mBluetoothAdapter.startLeScan(mLeScanCallback);
		return isScanBLEStart;
	}

	/**
	 * stopSearch
	 */
	public void stopSearch() {
		
		mTimeoutCheck.stopCheckTimeout();
		
		try {

			mContext.unregisterReceiver(mReceiver);
		} catch (Exception e) {

		}
		
		try{
			if(isScanBLEStart) {
				isScanBLEStart = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}catch(Exception e){
			
		}

		try{
			if(isScanClassic){
				isScanClassic = false;
				mBluetoothAdapter.cancelDiscovery();
			}
		}catch(Exception e){
			
		}
		mSeartchChangeHander.removeMessages(SEARTCH_BY_CLASSIC);
		
		
	}

	
	/**
	 *  切换经典模式扫描
	 */
	private void seartchByClassic() {
		mSeartchChangeHander.removeMessages(SEARTCH_BY_CLASSIC);
		mSeartchChangeHander.sendEmptyMessageDelayed(SEARTCH_BY_CLASSIC, 500);
	}

	/**
	 *  开始经典模式扫描
	 */
	private void startClassicSeartch() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mContext.registerReceiver(mReceiver, filter);
		 isScanClassic = mBluetoothAdapter.startDiscovery();
		if (!isScanClassic) {
			timeOutAction();
		}
	}

	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (null != mOnSeartchCallback) {
					mOnSeartchCallback.onSeartch(device);
				}

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				
				timeOutAction();
			}
		}
	};
	
	
	public void timeOutAction(){
		boolean isinterrupt = false;
		if (null != mOnSeartchCallback) {
			isinterrupt = mOnSeartchCallback.onSeartchTimeOut();
		}
		
		if(!isinterrupt){
			
			stopSearch();
		}
	}

	public int getTime_out() {
		return time_out;
	}

	public void setTime_out(int time_out) {
		this.time_out = time_out;
	}

	public interface OnSeartchCallback {
		public boolean onSeartch(BluetoothDevice device);

		public boolean onSeartchTimeOut();
	}

}
