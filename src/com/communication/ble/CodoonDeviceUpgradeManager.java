package com.communication.ble;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.communication.data.CLog;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.data.TransferStatus;

public class CodoonDeviceUpgradeManager implements Runnable {

	protected static final String TAG = "info";
	private static final int SEND_CONTENT = 3;
	private TimeoutCheck mTimeoutCheck;
	private DeviceUpgradeCallback upgradeCallback;
	private Context mContext;

	private BleManager mBleManager;
	private boolean hasFound;
	private Handler mHandler;
	private BluetoothDevice mDevice;
	private boolean isStart;
	private final int TIME_OUT = 12000;
	private final int CONNECT_AGAIN = 2;
	private final int ORDER_CONNECT = 1;
	private int[] mLastSendData;

	private boolean isBootMode;
	private int frame;
	private int totalFrame;
	private static int EACH_PART_NUM = 12;
	private String filePath;
	private boolean isSendingData;

	private boolean isVerify;

	private String mWriteClicentUUID = "0000180f-0000-1000-8000-00805f9b34fb",
			mWriteCharacteristicUUID = "00002a19-0000-1000-8000-00805f9b34fb";

	IConnectCallback connectBack;
	private int checkData = 0;
	byte[] buffer = new byte[EACH_PART_NUM];

	private FileInputStream input;

	// protected boolean isBindDevice;

	public CodoonDeviceUpgradeManager(Context context,
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
					CLog.i("info",
							"连接状态：" + newState + " 设备名字：" + mDevice.getName());
					/**
					 * 判断是否已经处于boot模式
					 */
					// if (!isBootMode && null != mDevice.getName()) {
					// isBootMode = true;
					// CLog.i("info", "已经处于boot模式");
					// // if (isStart) {
					// // mHandler.removeMessages(ORDER_CONNECT);
					// // CLog.i("info", "处理消息：ORDER_CONNECT");
					// // mHandler.sendEmptyMessageDelayed(ORDER_CONNECT,
					// // 1000);
					// // }
					// return;
					// }

					hasFound = true;
					mHandler.removeMessages(CONNECT_AGAIN);

				} else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
					CLog.i("info", "断开连接");
					if (isStart && status != BluetoothGatt.GATT_SUCCESS) {
						hasFound = false;
						Log.i(TAG, "disconnected been down so connect again");
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
					if (isBootMode) {
						CLog.i(TAG,
								"send order to isBootMode device getPostConnection");
						SendDataToDevice(SendData.postConnectBootOrder());
						readDeviceByHand();
					} else {
						CLog.i("info", "发转换到boot模式指令");
						SendDataToDevice(SendData.postBootMode());
						readDeviceByHand();
					}
				} else if (msg.what == CONNECT_AGAIN) {
					CLog.i(TAG, "connect  device  again");
					mBleManager.connect(mDevice, true);
				} else if (msg.what == SEND_CONTENT) {
					isSendingData = true;
				}
			}

		};
		mContext = context;
		this.upgradeCallback = upgradeCallback;
		mBleManager = BleManager.getInstance(mContext);
		mBleManager.register(connectBack);
		mTimeoutCheck = new TimeoutCheck(mTimeoutCallback);
		mTimeoutCheck.setTryConnectCounts(4);
		mTimeoutCheck.setTimeout(TIME_OUT);

	}

	public void registeBleManager() {
		mBleManager.register(connectBack);
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
		isBootMode = false;
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
		isBootMode = false;
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
			final int msgID = datas.get(1);
			CLog.i("info", "接收的数据datas.get(1):" + msgID);
			switch (msgID) {
			case TransferStatus.RECEIVE_BOOT_STATE_ID:
				upgradeCallback.onChangeToBootMode();
				CLog.i("info", "转换boot区重新连接");
				isBootMode = true;
				// SendDataToDevice(SendData.postConnectBootVersion());
				mHandler.removeMessages(ORDER_CONNECT);
				mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 100);

				break;

			case TransferStatus.RECEIVE_BOOT_VERSION_ID:

				if (null == datas || datas.size() < 6) {
					reSendDataToDevice(mLastSendData);
					return;
				}
				mTimeoutCheck.stopCheckTimeout();

				upgradeCallback.onGetBootVersion(datas.get(4) + "."
						+ datas.get(5));
				mBleManager.disconnect();
				mTimeoutCheck.setTryConnectCounts(3);
				mTimeoutCheck.setTimeout(TIME_OUT);
				mTimeoutCheck.startCheckTimeout();
				mHandler.removeMessages(CONNECT_AGAIN);
				mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 10);
				break;
			case TransferStatus.RECEIVE_BOOT_CONNECT_ID:
				upgradeCallback.onConnectBootSuccess();
				isBootMode = true;
				frame = 0;
				isSendingData = true;
				calcToatals();
				sendData();
				break;
			case TransferStatus.RECEIVE_BOOT_UPGRADE_ID:
				upgradeCallback.onWriteFrame(frame, totalFrame);
				int frame_back = datas.get(3) << 8 + datas.get(4);
				if (checkBackIsRight(datas)) {
					// Log.d(TAG, "backframe == frame");
					frame++;
					if (!sendData()) {
						try {
							input.close();
							input = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
						isVerify = true;
						// Log.d(TAG, "check：" + Integer.toHexString(checkData &
						// 0x0000FFFF) + " frame:" + frame);
						SendDataToDevice(SendData
								.postBootEnd((int) (checkData & 0x0000FFFF)));

					}
				} else {
					Log.e(TAG, "frame: err:" + frame);
					reSendDataToDevice(mLastSendData);
				}

				break;

			case TransferStatus.RECEIVE_UPGRADE_OVER_ID:
				boolean result = parseIsUpSuccess(datas);
				Log.d(TAG, "onCheck：" + result);
				upgradeCallback.onCheckBootResult(result);
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

	private boolean checkBackIsRight(ArrayList<Integer> datas) {
		// TODO Auto-generated method stub
		// if(null == datas || datas.size() < 3){
		// return false;
		// }
		//
		// // 从帧数开始， 直到校验位
		// for(int i = 3; i < mLastSendData.length -1 ; i++ ){
		// if((mLastSendData[i] & 0x000000ff) != datas.get(i)){
		// return false;
		// }
		// }
		return true;
	}

	/**
	 * 0x00 success else failed
	 * 
	 * @param datas
	 * @return
	 */
	private boolean parseIsUpSuccess(ArrayList<Integer> datas) {
		// TODO Auto-generated method stub
		if (null == datas || datas.size() < 5) {
			return false;
		}
		int check = datas.get(3);

		return (check == 0);
	}

	public String getUpgradeFilePath() {
		return filePath;
	}

	public void setUpgradeFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * 升级写文件的线程
	 */

	@Override
	public void run() {

	}

	public void setInput(FileInputStream input) {
		this.input = input;
	}

	public void calcToatals() {
		File file = new File(filePath);
		if (!file.exists()) {
			Log.e(TAG, "err not find file:" + filePath);
			return;
		}
		CLog.i("info", "@@@@@@@升级文件路径:" + file.getAbsolutePath());
		totalFrame = (int) (file.length() / (EACH_PART_NUM));
		frame = 0;
		checkData = 0;
		Log.d(TAG, "totalFrame is:" + totalFrame);
		if (null != input) {
			try {
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		input = null;
		try {
			input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean sendData() {

		try {
			// 每次读取18个字节， 然后进程进入等待，等待下次的唤醒继续
			int length = 0;
			if (-1 != (length = input.read(buffer))) {

				for (int i = 0; i < length; i++) { // 计算校验和
					checkData += (buffer[i] & 0x00ff);
				}
				// Log.d(TAG, "send frame is:" + frame + " total:" + totalFrame
				// + " length:" + length);
				if (length < buffer.length) {
					for (int i = length; i < buffer.length; i++) {
						buffer[i] = 0;
					}
				}
				SendDataToDevice(SendData.postBootUploadData(frame, buffer,
						buffer.length));
			} else {
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "exception：" + frame + " total:" + totalFrame);

		}
		return true;
	}

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
