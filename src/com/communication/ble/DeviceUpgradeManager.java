package com.communication.ble;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.netUtils.MyUtils;
import com.communication.data.CLog;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.data.TransferStatus;

public class DeviceUpgradeManager implements Runnable {

	protected static final String TAG = "info";
	private static final int SEND_CONTENT = 3;
	private static final int DEVICE_TYPE_ID = 0x13;
	private TimeoutCheck mTimeoutCheck;
	private DeviceUpgradeCallback upgradeCallback;
	private Context mContext;

	private CodmiBleManager mBleManager;
	private boolean hasFound;
	private Handler mHandler;
	// private BluetoothDevice mDevice;
	private boolean isStart;
	private final int TIME_OUT = 12000;
	private final int CONNECT_AGAIN = 2;
	private final int ORDER_CONNECT = 1;
	private int[] mLastSendData;

	private static int EACH_PART_NUM = 12;
	private String filePath;
	private boolean isSendingData;

	private boolean isVerify;

	private String mWriteClicentUUID = "0000180f-0000-1000-8000-00805f9b34fb",
			mWriteCharacteristicUUID = "00002a19-0000-1000-8000-00805f9b34fb";

	IConnectCallback connectBack;
	byte[] buffer = new byte[EACH_PART_NUM];

	private FileInputStream input;
	private BluetoothDevice mDevice;

	public DeviceUpgradeManager(Context context,
			DeviceUpgradeCallback upgradeCallback) {
		this.connectBack = new IConnectCallback() {

			@Override
			public void getValues(ArrayList<Integer> list) {
				if (isStart)
					analysis(list);
			}

			@Override
			public void getValue(int value) {

			}

			@Override
			public void discovered() {
				CLog.i("info", "**************discovered()*********isStart:"
						+ isStart);
				if (isStart) {
					mHandler.removeMessages(ORDER_CONNECT);
					CLog.i("info", "处理消息：ORDER_CONNECT");
					mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 100);
				}
			}

			@Override
			public void connectState(BluetoothDevice device, int status,
					int newState) {
				isSendingData = false;
				if (newState == BluetoothAdapter.STATE_CONNECTED) {
					CLog.i("info", "连接上设备");

				} else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
					CLog.i("info", "断开连接");
					if (isStart && status != BluetoothGatt.GATT_SUCCESS) {
						hasFound = false;
						CLog.i(TAG, "disconnected been down so connect again");
						reConnectBle();
					}
				}

				CLog.i(TAG, "isconnected:" + hasFound);
			}
		};
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				CLog.i("info", "接收的handler:" + msg.what);
				if (msg.what == ORDER_CONNECT) {
					String productId = MyXiaomiApp.getInstance().getProductId();
					CLog.i("info", "发送ID指令,id是:" + productId);
					SendDataToDevice(SendData.getPostWriteDeviceID(
							DEVICE_TYPE_ID, MyUtils.stringToInts(productId)));
					// CLog.i("info", "发送指令读取ID号");
					// SendDataToDevice(SendData.getPostDeviceID());
					readDeviceByHand();
				} else if (msg.what == CONNECT_AGAIN) {
					Log.i(TAG, "connect  device  again");
					mBleManager.connect(mDevice, true);
				} else if (msg.what == SEND_CONTENT) {
					isSendingData = true;
				}
			}

		};
		mContext = context;
		this.upgradeCallback = upgradeCallback;
		mBleManager = CodmiBleManager.getInstance(mContext);
		mBleManager.register(connectBack);
		mTimeoutCheck = new TimeoutCheck(mTimeoutCallback);
		mTimeoutCheck.setTryConnectCounts(4);
		mTimeoutCheck.setTimeout(TIME_OUT);

	}

	public void registeBleManager() {
		mBleManager.register(connectBack);
	}

	public void setmDevice(BluetoothDevice mDevice) {
		this.mDevice = mDevice;
	}

	/**
	 * 
	 * @param count
	 *            default count is 3 times
	 */
	public void setTryConnectCounts(int count) {
		mTimeoutCheck.setTryConnectCounts(count);
	}

	/**
	 * 
	 * @param clientUUID
	 */
	public void setWriteClientUUID(String clientUUID) {
		mWriteClicentUUID = clientUUID;
	}

	/**
	 * 
	 * @param characteristicUUID
	 */
	public void setWriteCharacteristicUUID(String characteristicUUID) {
		mWriteCharacteristicUUID = characteristicUUID;
	}

	/**
	 * 
	 */
	public void start(BluetoothDevice device) {
		mDevice = device;
		isStart = true;
		mBleManager.disconnect();
		mBleManager.connect(device, true);

		mTimeoutCheck.startCheckTimeout();
		mTimeoutCheck.setIsConnection(true);
		mTimeoutCheck.setTryConnectCounts(2);
		mTimeoutCheck.setTimeout(TIME_OUT);
	}

	private void reConnectBle() {

		mTimeoutCheck.setTryConnectCounts(1);
		mTimeoutCheck.setTimeout(TIME_OUT);
		mTimeoutCheck.startCheckTimeout();

		mHandler.removeMessages(ORDER_CONNECT);
		mHandler.removeMessages(CONNECT_AGAIN);

		if (null != mBleManager) {
			mBleManager.disconnect();
			mHandler.removeMessages(CONNECT_AGAIN);
			mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 10);
		}
	}

	public boolean isConnect() {
		return mBleManager.isConnect;
	}

	/**
	 * 
	 */
	public void stop() {
		isStart = false;
		isSendingData = false;
		mHandler.removeMessages(ORDER_CONNECT);
		mHandler.removeMessages(CONNECT_AGAIN);
		if (null != mTimeoutCheck) {
			mTimeoutCheck.stopCheckTimeout();
		}
		if (mBleManager != null) {
			mBleManager.close();
		}
		mBleManager.unRegister(connectBack);
		if (null != input) {
			try {
				input.close();
			} catch (Exception e) {

			}
		}

	}

	private TimeoutCheck.ITimeoutCallback mTimeoutCallback = new TimeoutCheck.ITimeoutCallback() {

		@Override
		public void onReceivedFailed() {
			Log.d(TAG, "receivedFailed()");
			if (isVerify) {
				isVerify = false;
				upgradeCallback.onCheckBootResult(true);
			} else {
				upgradeCallback.onTimeOut();
			}
		}

		@Override
		public void onReSend() {
			Log.d(TAG, "reSend() hasFound?" + hasFound);
			if (hasFound) {
				reSendDataToDevice(mLastSendData);
			}

		}

		@Override
		public void onReConnect(int tryConnectIndex) {
			Log.d(TAG, "reConnect() tryConnectIndex:" + tryConnectIndex);
			reConnectBle();
		}

		@Override
		public void onConnectFailed(int tryConnectIndex) {
			Log.d(TAG, "ConnectFailed() tryConnectIndex:" + tryConnectIndex);
			upgradeCallback.onTimeOut();
		}
	};

	/**
	 * send data from application to device
	 * 
	 * @param datas
	 */
	public void SendDataToDevice(final int[] datas) {
		if (mBleManager != null) {
			mTimeoutCheck.setIsConnection(false);
			mTimeoutCheck.setTimeout(TIME_OUT);
			mTimeoutCheck.setTryConnectCounts(2);
			mTimeoutCheck.startCheckTimeout();
			mLastSendData = datas;
			mBleManager.writeIasAlertLevel(mWriteClicentUUID,
					mWriteCharacteristicUUID, intToByte(datas));
		}
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
			bytes[i] = (byte) datas[i];
		}
		return bytes;
	}

	/**
	 * 
	 * @param datas
	 */
	private void reSendDataToDevice(final int[] datas) {
		if (mBleManager != null) {
			mLastSendData = datas;
			mBleManager.writeIasAlertLevel(mWriteClicentUUID,
					mWriteCharacteristicUUID, intToByte(datas));
		}
	}

	/**
	 * 
	 * @param datas
	 */
	@SuppressWarnings("deprecation")
	protected void analysis(ArrayList<Integer> datas) {
		if (datas == null) {
			return;
		} else {
			CLog.i("info", "接收的数据datas.get(1):" + datas.toString());
			final int msgID = datas.get(1);
			switch (msgID) {
			case TransferStatus.RECEIVE_WRITE_ID:
				CLog.i("info", "写入ID成功");
				upgradeCallback.onBindDivce(true);
				break;
			default:
				Log.d(TAG, "$$$$$$$4on get other datas:" + msgID);
				if (isVerify) {
					upgradeCallback.onCheckBootResult(true);
					isVerify = false;
				} else {
					reSendDataToDevice(mLastSendData);
				}
				break;
			}
		}
	}

	public String getUpgradeFilePath() {
		return filePath;
	}

	public void setUpgradeFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public void run() {
		start(mDevice);
	}

	public void setInput(FileInputStream input) {
		this.input = input;
	}

	@SuppressLint("NewApi")
	protected void readDeviceByHand() {
		CLog.i("info", "手动读取设备数据");
		BluetoothGattService service = mBleManager.getSupportedGattService();
		if (service == null) {
			return;
		}
		List<BluetoothGattCharacteristic> gattCharacteristics = service
				.getCharacteristics();
		if (gattCharacteristics == null) {
			return;
		}
		for (int i = 0; null != gattCharacteristics
				&& i < gattCharacteristics.size(); i++) {
			BluetoothGattCharacteristic characteristic = gattCharacteristics
					.get(i);
			CLog.i("info",
					"手动读取设备数据:" + Arrays.toString(characteristic.getValue()));
			mBleManager.readCharacteristic(characteristic);
		}

	}
}
