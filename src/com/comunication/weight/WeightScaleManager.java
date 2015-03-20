package com.comunication.weight;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.communication.ble.BleManager;
import com.communication.ble.IConnectCallback;
import com.communication.data.DataUtil;
import com.communication.data.ErrInfo;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;

public class WeightScaleManager {

	private String mWriteClicentUUID = "0000180f-0000-1000-8000-00805f9b34fb",
			mWriteCharacteristicUUID = "00002a19-0000-1000-8000-00805f9b34fb";

	private Context mContext;
	private BleManager mBleManager;
	private Handler mHandler;
	private BluetoothDevice mDevice;
	private int[] mLastSendData;
	private OnWeightListener mOnWeightListener;
	private TimeoutCheck mTimeoutCheck;

	private final int TIME_OUT = 12000;
	private final int CONNECT_AGAIN = 2;
	private final int ORDER_CONNECT = 1;
	private final int TIME_OUT_CALL_BACK = 0x111;

	private boolean isStart;

	private int errCount = 0;

	public WeightScaleManager(Context mContext, OnWeightListener listener) {
		this.mContext = mContext;
		this.mOnWeightListener = listener;
		mBleManager = new BleManager(mContext);
		mBleManager.register(new IConnectCallback() {

			@Override
			public void connectState(BluetoothDevice device, int status,
					int newState) {
				// TODO Auto-generated method stub
				if (newState == BluetoothAdapter.STATE_CONNECTED) {
					 mDevice = device;
					mHandler.removeMessages(CONNECT_AGAIN);
				} else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {

					if (isStart && status != BluetoothGatt.GATT_SUCCESS) {
						reConnectBle();
					}
				}

			}

			@Override
			public void discovered() {
				// TODO Auto-generated method stub
				if (isStart) {

					mHandler.removeMessages(ORDER_CONNECT);
					mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 1000);
				}
			}

			@Override
			public void getValues(ArrayList<Integer> list) {
				// TODO Auto-generated method stub
				if(isStart){
					
					analysis(list);
				}
			}

			@Override
			public void getValue(int value) {
				// TODO Auto-generated method stub

			}

		});

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ORDER_CONNECT:
//					mOnWeightListener.onConnect();
					sendDataToService(SendData.postWeightScaleConnect());
					break;
				case CONNECT_AGAIN:
					mBleManager.connect(mDevice, true);
					break;
				case TIME_OUT_CALL_BACK:
					mOnWeightListener.onTimeOut(ErrInfo.ERR_CONNECT);
					break;
				}
			}

		};

		mTimeoutCheck = new TimeoutCheck(mTimeoutCallback);
		mTimeoutCheck.setTryConnectCounts(5);
		mTimeoutCheck.setTimeout(TIME_OUT);
	}

	private TimeoutCheck.ITimeoutCallback mTimeoutCallback = new TimeoutCheck.ITimeoutCallback() {

		@Override
		public void onReceivedFailed() {
			mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
		}

		@Override
		public void onReSend() {
			if (isStart) {

				reSendDataToDevice(mLastSendData);
			}

		}

		@Override
		public void onReConnect(int tryConnectIndex) {
			reConnectBle();
		}

		@Override
		public void onConnectFailed(int tryConnectIndex) {
			mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
		}
	};

	protected void reConnectBle() {
		// TODO Auto-generated method stub

		mTimeoutCheck.setTryConnectCounts(1);
		mTimeoutCheck.setTimeout(TIME_OUT);
		mTimeoutCheck.startCheckTimeout();

		mHandler.removeMessages(ORDER_CONNECT);
		mHandler.removeMessages(CONNECT_AGAIN);

		if (null != mBleManager) {
			mBleManager.close();
			mHandler.removeMessages(CONNECT_AGAIN);
			mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 1200);
		}
	}

	public void sendDataToService(int[] order) {
		mTimeoutCheck.setTryConnectCounts(1);
		mTimeoutCheck.setTimeout(TIME_OUT * 5);
		mTimeoutCheck.startCheckTimeout();

		if (mBleManager != null) {
			mLastSendData = order;
			mBleManager.writeIasAlertLevel(mWriteClicentUUID,
					mWriteCharacteristicUUID, intToByte(order));
		}
	}

	/**
	 * 
	 */
	public void start(BluetoothDevice device) {
		isStart = true;
		errCount = 0;
		mDevice = device;
		if(null == mDevice || !mDevice.getAddress().equals(device.getAddress())){
			mBleManager.connect(device, true);
		}else if(null != mDevice && mDevice.getAddress().equals(device.getAddress())){
			
			if(!mBleManager.isConnect){
				mBleManager.connect(device, true);
			}else{
				mHandler.removeMessages(ORDER_CONNECT);
				mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 1000);
			}
		}else{
			mBleManager.connect(device, true);
		}

		mTimeoutCheck.startCheckTimeout();
		mTimeoutCheck.setIsConnection(true);
		mTimeoutCheck.setTryConnectCounts(2);
		mTimeoutCheck.setTimeout(TIME_OUT);
	}

	/**
	 * 
	 */
	public void stop() {
		isStart = false;
		errCount = 0;
		mLastSendData = null;
		mHandler.removeMessages(ORDER_CONNECT);
		mHandler.removeMessages(CONNECT_AGAIN);
		mHandler.removeMessages(TIME_OUT_CALL_BACK);

		if (null != mTimeoutCheck) {
			mTimeoutCheck.stopCheckTimeout();
		}
		if (mBleManager != null) {
			mBleManager.close();
		}
		mDevice = null;
	}

	/**
	 * 
	 * @param datas
	 * @return
	 */
	private byte[] intToByte(int[] datas) {
		int size = datas.length;
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++) {
			bytes[i] = (byte) (datas[i] & 0x000000ff);
		}
		return bytes;
	}

	/**
	 * 
	 * @param datas
	 */
	private void reSendDataToDevice(final int[] datas) {
		if (mBleManager != null) {
			// mLastSendData = datas;
			mBleManager.writeIasAlertLevel(mWriteClicentUUID,
					mWriteCharacteristicUUID, intToByte(datas));
		}
	}

	public void analysis(List<Integer> org_list) {
		// FD 31 00 00 00 00 00 31
		if (null != org_list && org_list.size() > 0) {
			mTimeoutCheck.stopCheckTimeout();
			
			if(isConnectWeightScale(org_list)){
				
				mOnWeightListener.onConnect();
				sendDataToService(SendData.postWeightInfo(mOnWeightListener.onLoadPersonInfo()));
			}else if(org_list.get(1) == 0x85){
				
				List<Integer> list = new ArrayList<Integer>();
				
				int length = org_list.get(2);
				for(int i = 3; i < length + 3; i++){
					list.add(org_list.get(i));
				}
				
				if (isErrBluetoothInfo(list)) {
					if (errCount < 3) {

						errCount++;
						reSendDataToDevice(mLastSendData);
					} else {
						mOnWeightListener.onTimeOut(ErrInfo.ERR_BLUETOOTH);
					}
				} else if (isErrFatInfo(list)) {
					mOnWeightListener.onTimeOut(ErrInfo.ERR_FAT);
					
				} else {
					
					WeightScaleType type = WeightScaleType.getValue(list.get(0));
					if (type.ordinal() != WeightScaleType.NONE.ordinal()) {
						
						try{
							WeightInfo info = new WeightInfo();
							info.level = (list.get(1) >> 4) & 0x0f;
							info.group = list.get(1) & 0x0f;
							info.sex = (list.get(2) >> 7) & 0x01;
							info.age = list.get(2) & 0x7f;
							info.height = list.get(3);
							info.weight = ((list.get(4) << 8) + list.get(5))
									* WeightScaleType.getResolutionByType(type);
							info.fatRate = ((list.get(6) << 8) + list.get(7)) * 0.1f;
							info.boneRate = list.get(8) * 0.1f / info.weight * 100;
							info.muscleRate = ((list.get(9) << 8) + list.get(10)) * 0.1f;
							info.fatLevel = list.get(11);
							info.waterRate = ((list.get(12) << 8) + list.get(13)) * 0.1f;
							info.BMR = (list.get(14) << 8) + list.get(15);
							
							if (null != mOnWeightListener) {
								mOnWeightListener.onGetDeiveId(type.ordinal());
								mOnWeightListener.onGetWeightInfo(info);
							}
						}catch(Exception e){
							
						}
						
						
					} else {
						mOnWeightListener.onTimeOut(ErrInfo.ERR_UNKOWN);
					}
				}
				
			}
		}
	}

	
	private boolean isConnectWeightScale(List<Integer> list) {
		// TODO Auto-generated method stub
		if(list.get(1) == 0x81){
			
			return true;
		}
		return false;
	}

	private boolean isErrFatInfo(List<Integer> list) {
		// TODO Auto-generated method stub
		DataUtil.DebugPrint(list);
		int[] err = new int[] { 0x00fd, 0x31, 0x0, 0x0, 0x0, 0x0, 0x0, 0x33 };
		try {

			for (int i = 0; i < err.length; i++) {
				if ((list.get(i).intValue() & 0x00ff)  != err[i]) {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		
		DataUtil.DebugPrint("fat err");
		return true;
	}

	private boolean isErrBluetoothInfo(List<Integer> list) {
		// TODO Auto-generated method stub
		DataUtil.DebugPrint(list);
		int[] err = new int[] { 0x00fd, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x31 };
		try {

			for (int i = 0; i < err.length; i++) {
				if ((list.get(i).intValue() & 0x00ff) != err[i]) {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		
		DataUtil.DebugPrint("Bluetooth connect err");
		return true;
	}
}
