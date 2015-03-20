package com.communication.ble;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.communication.data.AccessoryDataParseUtil;
import com.communication.data.AccessoryValues;
import com.communication.data.CLog;
import com.communication.data.ISyncDataCallback;
import com.communication.data.SaveManager.eSaveType;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.data.TransferStatus;

public class BleSyncManager {

	private String TAG = "BleSyncManager";

	private int frameCount = 0;

	private int indexFrame = 0;

	private int[] mLastSendData;

	private ArrayList<ArrayList<Integer>> mRecordDatas;

	private Context mContext;
	
	private BleManager mBleManager;
	
	private eSaveType mSaveType=eSaveType.DATABSE;

	private TimeoutCheck mTimeoutCheck;

	private String mClicentUUID,mCharacteristicUUID;
	
	private String mWriteClicentUUID="0000180f-0000-1000-8000-00805f9b34fb",
				mWriteCharacteristicUUID="00002a19-0000-1000-8000-00805f9b34fb";

	private ByteArrayOutputStream mBaos;
	private Handler mHandler;
	private BluetoothDevice mDevice;
	
	private boolean isStart;
	private final byte[] xorKey = new byte[] { 0x54, (byte) 0x91, 0x28, 0x15,
			0x57, 0x26 };
	private final int TIME_OUT = 12000;
	private final int CONNECT_AGAIN = 2;
	private final int ORDER_CONNECT = 1;
	private final int TIME_OUT_CALL_BACK = 0x111;
	
	private IConnectCallback connectBack;
	
	private List<ISyncDataCallback> mISyncDataCallbacks;

	
	public void register(ISyncDataCallback callback){
		if(null != callback && !mISyncDataCallbacks.contains(callback)){
			mISyncDataCallbacks.add(callback);
		}
	}
	
	public BleSyncManager(Context context, ISyncDataCallback callback) {

		
		mISyncDataCallbacks = new ArrayList<ISyncDataCallback>();
		mISyncDataCallbacks.add(callback);
		
		connectBack = new IConnectCallback() {
			
			@Override
			public void getValues(ArrayList<Integer> list) {
				if(isStart){
					analysis(list);
				}else{
					Log.d(TAG, " isStart:" + isStart);
				}
			}
			
			@Override
			public void getValue(int value) {
				
			}
			
			@Override
			public void discovered() {
				// TODO Auto-generated method stub
				//mCallback.onConnectSuccessed();
				if(isStart){
					
					mHandler.removeMessages(ORDER_CONNECT);
					mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 1000);
				}
			}
			
			@Override
			public void connectState(BluetoothDevice device, int status, int newState) {
				// TODO Auto-generated method stub
				
				 if(newState == BluetoothAdapter.STATE_CONNECTED){
					 mDevice = device;
					 mHandler.removeMessages(CONNECT_AGAIN);
				 }else if(newState == BluetoothAdapter.STATE_DISCONNECTED){
					 
					 if(isStart && status != BluetoothGatt.GATT_SUCCESS){
						 CLog.i(TAG, "disconnected been down so connect again");
						 reConnectBle();
					 }
				 }
				 
			}
		};
		// TODO Auto-generated constructor stub
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(msg.what == ORDER_CONNECT){
					
//					SendDataToDevice(SendData
//							.getPostDeviceID());
					if(isStart){
						
						CLog.i(TAG, "send order to device after ble connect " + mISyncDataCallbacks.size());
						for(ISyncDataCallback mCallback: mISyncDataCallbacks){
							mCallback.onConnectSuccessed();
						}
					}else{
						Log.e(TAG, "not start");

					}
					
				}else if(msg.what == CONNECT_AGAIN){
					
					CLog.i(TAG, "connect  ble device  again" );
					mBleManager.connect(mDevice, true);
				}else if(msg.what == TIME_OUT_CALL_BACK){
					if(isStart){
						
						for(ISyncDataCallback mCallback: mISyncDataCallbacks){
							mCallback.onTimeOut();
						}
					}
				}
			}
			
		};
		mContext = context;
		mTimeoutCheck = new TimeoutCheck(mTimeoutCallback);
		mTimeoutCheck.setTryConnectCounts(5);
		mTimeoutCheck.setTimeout(TIME_OUT);
	
		mBleManager =  BleManager.getInstance(mContext); 
	
	}
	
	public void registerBLE(){
		mBleManager.register(connectBack);
	}
	
	public void unRegisterBLE(){
	}
	public boolean isConnect(){
		return mBleManager.isConnect;
	}
	

	/**
	 * 
	 * @param count default count is 3 times
	 */
	public void setTryConnectCounts(int count) {
		mTimeoutCheck.setTryConnectCounts(count);
	}

	/**
	 * 
	 * @param saveType default save type is database
	 */
	public void setSaveType(eSaveType saveType){
		mSaveType=saveType;
	}
	

	/**
	 * 
	 * @param clientUUID
	 */
	public void setClientUUID(String clientUUID){
		mClicentUUID=clientUUID;
	}
	
	/**
	 * 
	 * @param characteristicUUID
	 */
	public void setCharacteristicUUID(String characteristicUUID){
		mCharacteristicUUID=characteristicUUID;
	}
	
	/**
	 * 
	 * @param clientUUID
	 */
	public void setWriteClientUUID(String clientUUID){
		mWriteClicentUUID=clientUUID;
	}
	
	/**
	 * 
	 * @param characteristicUUID
	 */
	public void setWriteCharacteristicUUID(String characteristicUUID){
		mWriteCharacteristicUUID=characteristicUUID;
	}

	public void start(int[] order){
		isStart = true;
		SendDataToDevice(order);
	}
	
	/**
	 * 
	 */
	public void start(BluetoothDevice device) {
		isStart = true;
			
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

	public BluetoothDevice getDevice(){
		return mDevice;
	}
	
	private void reConnectBle(){
	
			mTimeoutCheck.setTryConnectCounts(1);
			mTimeoutCheck.setTimeout(TIME_OUT);
			mTimeoutCheck.startCheckTimeout();
		
			mHandler.removeMessages(ORDER_CONNECT);
			mHandler.removeMessages(CONNECT_AGAIN);
			
		if(null != mBleManager){
			mBleManager.close();
			mHandler.removeMessages(CONNECT_AGAIN);
			mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 1500);
		}
		
	}
	
	public void cancel(){
		isStart = false;
		mLastSendData = null;
		mHandler.removeMessages(ORDER_CONNECT);
		mHandler.removeMessages(CONNECT_AGAIN);
		mHandler.removeMessages(TIME_OUT_CALL_BACK);
		
		if(null != mTimeoutCheck){
			mTimeoutCheck.stopCheckTimeout();
		}
		mBleManager.unRegister(connectBack);
	}
	
	/**
	 * 
	 */
	public void stop() {
		
		cancel();
		mISyncDataCallbacks.clear();
		if(mBleManager!=null){
			mBleManager.close();
			mDevice = null;
		}
	}
	

	private TimeoutCheck.ITimeoutCallback mTimeoutCallback = new TimeoutCheck.ITimeoutCallback() {

		@Override
		public void onReceivedFailed() {
			Log.d(TAG, "receivedFailed()");
			mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
		}

		@Override
		public void onReSend() {
			Log.d(TAG, "reSend()");
			if(isStart){
				
				reSendDataToDevice(mLastSendData);
			}
			
		}

		
		@Override
		public void onReConnect(int tryConnectIndex) {
			Log.d(TAG, "reConnect() tryConnectIndex:"+tryConnectIndex);
				reConnectBle();
		}

		@Override
		public void onConnectFailed(int tryConnectIndex) {
			Log.d(TAG, "ConnectFailed() tryConnectIndex:"+tryConnectIndex);
//			for(ISyncDataCallback mCallback: mISyncDataCallbacks){
//				mCallback.onTimeOut();
//			}
			mHandler.sendEmptyMessage(TIME_OUT_CALL_BACK);
		}
	};

	/**
	 * send data from application to device
	 * 
	 * @param datas
	 */
	public void SendDataToDevice(final int[] datas) {
//		if(DataUtil.equalArray(mLastSendData, datas)){
//			return;
//		}
		if (mBleManager != null) {
			mTimeoutCheck.setIsConnection(false);
			mTimeoutCheck.setTimeout(TIME_OUT);
			mTimeoutCheck.setTryConnectCounts(2);
			mTimeoutCheck.startCheckTimeout();
			mLastSendData = datas;
			mBleManager.writeIasAlertLevel(mWriteClicentUUID, mWriteCharacteristicUUID, intToByte(datas));
		}
	}
	
	
	/**
	 * 
	 * @param datas
	 * @return
	 */
	private byte[] intToByte(int[] datas){
		int size=datas.length;
		byte[] bytes=new byte[size];
		for(int i=0;i<size;i++){
			bytes[i]=(byte)(datas[i] & 0x000000ff);
		}
		return bytes;
	}
 

	/**
	 * 
	 * @param datas
	 */
	private void reSendDataToDevice(final int[] datas) {
		if (mBleManager != null) {
//			mLastSendData = datas;
			mBleManager.writeIasAlertLevel(mWriteClicentUUID, mWriteCharacteristicUUID, intToByte(datas));
		}
	}

	/**
	 * 
	 * @param datas
	 */
	protected void analysis(ArrayList<Integer> datas) {

		if (datas == null) {
			for(ISyncDataCallback mCallback: mISyncDataCallbacks){
				mCallback.onNullData();
			}
		} else {
			final int msgID = datas.get(1);
			switch (msgID) {
			case TransferStatus.RECEIVE_CONNECTION_ID:
				mTimeoutCheck.stopCheckTimeout();
				mTimeoutCheck.setTryConnectCounts(3);
				mTimeoutCheck.setIsConnection(false);
				mTimeoutCheck.setTimeout(3000);
				
				for(ISyncDataCallback callback: mISyncDataCallbacks){
					callback.onConnectSuccessed();
				}
				break;
				
			case TransferStatus.RECEIVE_BINED_ID:
				mTimeoutCheck.stopCheckTimeout();
				for(ISyncDataCallback callback: mISyncDataCallbacks){
					callback.onBindSucess();
				}
				break;
				
			case TransferStatus.RECEIVE_GETVERSION_ID:
				mTimeoutCheck.stopCheckTimeout();
				String hVersion = datas.get(4) + "." + datas.get(5);
				for(ISyncDataCallback callback: mISyncDataCallbacks){
					callback.onGetVersion(hVersion);
				}
				break;
			case TransferStatus.RECEIVE_DEVICE_ID:
				mTimeoutCheck.stopCheckTimeout();
				String deviceid = getDeviceID(datas);
				for(ISyncDataCallback callback: mISyncDataCallbacks){
					callback.onGetDeviceID(deviceid);
				}
				break;
			case TransferStatus.RECEIVE_UPDATETIME_ID:
				mTimeoutCheck.stopCheckTimeout();
				for(ISyncDataCallback callback: mISyncDataCallbacks){
					callback.onUpdateTimeSuccessed();
				}
				break;
			case TransferStatus.RECEIVE_DEVICE_TIME_ID:
				mTimeoutCheck.stopCheckTimeout();
				String time = countTime(datas);
				for(ISyncDataCallback callback: mISyncDataCallbacks){
					callback.onGetDeviceTime(time);
				}
				break;
			case TransferStatus.RECEIVE_CLEARDATA_ID:
				mTimeoutCheck.stopCheckTimeout();
				for(ISyncDataCallback mCallback: mISyncDataCallbacks){
					mCallback.onClearDataSuccessed();
				}
				
				break;
			case TransferStatus.RECEIVE_FRAMECOUNT_ID:
				mTimeoutCheck.stopCheckTimeout();
				frameCount = (datas.get(4) << 8) + datas.get(5) ;     //careful the priority of << is lower than + ;
				indexFrame = 0;
				mBaos = new ByteArrayOutputStream();
				Log.d(TAG, " framecount:" + frameCount);
				// read data after get the frame count
				SendDataToDevice(SendData.getPostReadSportData(indexFrame));

				mRecordDatas = new ArrayList<ArrayList<Integer>>();
				break;
			case TransferStatus.RECEIVE_READDATA_ID:
				mTimeoutCheck.stopCheckTimeout();
				// upload data from device
				final int length = datas.get(2);
				
				ArrayList<Integer> currentDatas = new ArrayList<Integer>();
				for (int i = 3; i < length +3; i++) {
					mBaos.write(encryptMyxor(datas.get(i), mBaos.size() % 6));
					currentDatas.add(datas.get(i));
				}
				mRecordDatas.add(currentDatas);
				++indexFrame;
				
				for(ISyncDataCallback mCallback: mISyncDataCallbacks){
					mCallback.onSyncDataProgress(indexFrame * 100 / frameCount);
				}
				
				if (indexFrame < frameCount) {
					SendDataToDevice(SendData
							.getPostReadSportData(indexFrame));

				} else {
					frameCount = 0;
					indexFrame = 0;
					
					for(ISyncDataCallback mCallback: mISyncDataCallbacks){
						mCallback.onSyncDataProgress(100);
					}
					AccessoryDataParseUtil decode = new AccessoryDataParseUtil(mContext);
					HashMap<String, AccessoryValues> data = decode.analysisDatas(mRecordDatas);
					
					for(ISyncDataCallback mCallback: mISyncDataCallbacks){
						mCallback.onSyncDataOver(data,mBaos);
					}
				}
				break;

			case TransferStatus.RECEIVE_GETUSERINFO_ID:
				mTimeoutCheck.stopCheckTimeout();
				getUserInfo(datas);
				break;

			case TransferStatus.RECEIVE_UPDATEUSERINFO_SUCCESS_ID:
				mTimeoutCheck.stopCheckTimeout();
				for(ISyncDataCallback mCallback: mISyncDataCallbacks){
					mCallback.onUpdateUserinfoSuccessed();
				}
				break;
				
			case TransferStatus.RECEIVE_UPDATEUSERINFO2_SUCCESS_ID:
				mTimeoutCheck.stopCheckTimeout();
				Log.d(TAG, "update alarm and activity remind success.");
				for(ISyncDataCallback mCallback: mISyncDataCallbacks){
					mCallback.onUpdateAlarmReminderSuccessed();
				}
				String values="";
				for(int i:datas){
					values+=",0x"+Integer.toHexString(i);
				}
				Log.d(TAG, values);
				break;
			case TransferStatus.RECEIVE_GETUSERINFO2_ID:
				mTimeoutCheck.stopCheckTimeout();
				int battery = 0;
				if(datas.size()>15){ 
					battery = datas.get(15);
				}else{
					battery = datas.get(10);
				}
				
				for(ISyncDataCallback mCallback: mISyncDataCallbacks){
					mCallback.onBattery(battery);
				}
				
				break;
				
			default:
				Log.d(TAG, "on get other datas");
				for(ISyncDataCallback mCallback: mISyncDataCallbacks){
					mCallback.onGetOtherDatas(datas);
				}
				break;
			}
		}
	}

	private byte encryptMyxor(int original, int n) {
		return (byte) ((original ^ xorKey[n]) & 0xFF);
	}
	
	/**
	 * 
	 * @param datas
	 */
	private void getUserInfo(ArrayList<Integer> datas) {
		int height = datas.get(3);
		int weigh = datas.get(4);
		int age = datas.get(5);
		int gender = datas.get(6);
		int stepLength = datas.get(7);
		int runLength = datas.get(8);
		int sportType = datas.get(11);
		int goalValue = datas.get(12) << 8;
		goalValue += datas.get(13);

		for(ISyncDataCallback mCallback: mISyncDataCallbacks){
			mCallback.onGetUserInfo(height, weigh, age, gender, stepLength,
					runLength, sportType, goalValue);
		}
	}

	/**
	 * 
	 * @param datas
	 * @return
	 */
	private String countTime(ArrayList<Integer> datas) {
		int year = 2000 + Integer.parseInt(Integer.toHexString(datas.get(3)));
		int month = Integer.parseInt(Integer.toHexString(datas.get(4)));
		int day = Integer.parseInt(Integer.toHexString(datas.get(5)));
		int hour = Integer.parseInt(Integer.toHexString(datas.get(6)));
		int minute = Integer.parseInt(Integer.toHexString(datas.get(7)));
		int second = Integer.parseInt(Integer.toHexString(datas.get(8)));

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault());
		cal.set(year, month, day, hour, minute, second);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getDefault());
		return format.format(new Date(cal.getTimeInMillis()));
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	private String getDeviceID(ArrayList<Integer> list) {
		StringBuilder append = new StringBuilder();
		append.append(list.get(3));
		append.append("-");

		append.append((list.get(4) << 8) + list.get(5));
		append.append("-");

		append.append((list.get(6) << 8) + list.get(7));
		append.append("-");

		append.append((list.get(8) << 8) + list.get(9));
		append.append("-");

		append.append(list.get(10));
		append.append("-");

		append.append((list.get(11) << 8) + list.get(12));
		append.append("-");

		append.append((list.get(13) << 8) + list.get(14));
		append.append("-");

		append.append(list.get(15));

		return append.toString();
	}


}
