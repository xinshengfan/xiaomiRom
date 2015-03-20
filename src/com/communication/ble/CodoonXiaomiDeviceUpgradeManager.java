package com.communication.ble;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.communication.data.CLog;
import com.communication.data.IDeviceUpgradeCallback;
import com.communication.data.SendData;
import com.communication.data.TimeoutCheck;
import com.communication.data.TransferStatus;
import com.communication.ui.G;

public class CodoonXiaomiDeviceUpgradeManager implements Runnable {

	protected static final String TAG = "info";

	private TimeoutCheck mTimeoutCheck;
	private IDeviceUpgradeCallback upgradeCallback;
	private Context mContext;

	private BleManager mBleManager;
	private boolean hasFound;
	private Handler mHandler;
	private BluetoothDevice mDevice;
	private boolean isStart;
	private final int TIME_OUT = 12000;
	private final int CONNECT_AGAIN = 2;
	private final int ORDER_CONNECT = 1;
	private static final int SEND_CONTENT = 3;
	private final int ORDER_BIND = 4;
	private final int ORDER_CHARGE = 5;
	// private final int ORDER_CHECK = 6;
	private final int ORDER_VERSION = 7;
	private final int ORDER_CHECKED_RESULT = 8;
	private static int EACH_PART_NUM = 12;
	private int[] mLastSendData;

	private boolean isBootMode;
	private int frame;
	private int totalFrame;
	private String filePath;
	private boolean isSendingData;
	private boolean isHadBindDevice;
	private int currentModel;
	private boolean isVerify;
	// private boolean isUpgradeOver;

	private String mWriteClicentUUID = "0000180f-0000-1000-8000-00805f9b34fb",
			mWriteCharacteristicUUID = "00002a19-0000-1000-8000-00805f9b34fb";

	IConnectCallback connectBack;
	private int checkData = 0;
	byte[] buffer = new byte[EACH_PART_NUM];

	private FileInputStream input;
	private boolean isHadSendCheckOrder;
	private ArrayList<Integer> CheckResult;

	// protected boolean isBindDevice;

	@SuppressLint("HandlerLeak")
	public CodoonXiaomiDeviceUpgradeManager(Context context,
			IDeviceUpgradeCallback upgradeCallback) {
		this.connectBack = new IConnectCallback() {

			@Override
			public void getValues(ArrayList<Integer> list) {
				if (isHadSendCheckOrder) {
					// 已经发送了检验指令
					CheckResult = list;
				}
				if (isStart) {
					analysis(list);
				}
			}

			@Override
			public void getValue(int value) {

			}

			@Override
			public void discovered() {
				CLog.i("info", "**************discovered()*********isStart:"
						+ isStart);
				if (MyXiaomiApp.getInstance().isCodoonCboot()) {
					CLog.i("info", "是Cboot模式，直接进入刷机");
					isBootMode = true;
					if (isStart) {
						mHandler.removeMessages(ORDER_CONNECT);
						mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 300);
					}
					return;
				}
				// if (isUpgradeOver) {
				// mHandler.removeMessages(ORDER_CHECK);
				// mHandler.sendEmptyMessageDelayed(ORDER_CHECK, 10);
				// return;
				// }
				if (isStart && !isHadBindDevice) {
					mHandler.removeMessages(ORDER_BIND);
					CLog.i("info", "处理消息：ORDER_BIND 准备绑定");
					mHandler.sendEmptyMessageDelayed(ORDER_BIND, 300);
				} else if (isStart && isHadBindDevice
						&& (currentModel == MyXiaomiApp.UPGRADE_CODOON)) {
					// 已经绑定设备，且是升级咕咚设备
					mHandler.removeMessages(ORDER_CONNECT);
					mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 300);
				}
			}

			@Override
			public void connectState(BluetoothDevice device, int status,
					int newState) {
				isSendingData = false;
				if (newState == BluetoothAdapter.STATE_CONNECTED) {
					CLog.i("info",
							"连接状态：" + newState + " 设备名字：" + mDevice.getName()
									+ ";地址：" + mDevice.getAddress());
					// 连接上就发送一个广播
					Intent connect_intent = new Intent(
							G.ACTION_CONNECTED_BRACELET);
					mContext.sendBroadcast(connect_intent);
					hasFound = true;
					mHandler.removeMessages(CONNECT_AGAIN);

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
				} else if (msg.what == ORDER_BIND) {
					CLog.i("info", "发送绑定命令：" + SendData.getPostBindOrder());
					SendDataToDevice(SendData.getPostBindOrder());
					readDeviceByHand();
				} else if (msg.what == ORDER_CHARGE) {
					CLog.i("info", "开始发送读取电量指令：");
					SendDataToDevice(SendData.getPostGetUserInfo2());
					readDeviceByHand();
				}
				// else if (msg.what == ORDER_CHECK) {
				// CLog.i("info", "mhandler 处理发送校验和");
				// SendDataToDevice(SendData
				// .postBootEnd((int) (checkData & 0x0000FFFF)));
				// readDeviceByHand();
				// }
				else if (msg.what == ORDER_VERSION) {
					CLog.i("info", "mhander 处理发送读取版本指令");
					SendDataToDevice(SendData.getPostDeviceTypeVersion());
					readDeviceByHand();
				} else if (msg.what == ORDER_CHECKED_RESULT) {
					CLog.i("info", "检查在发送了检验指令5秒后是否有返回值");
					if (CheckResult == null) {
						// 没有返回值，说明成功了
						if (MyXiaomiApp.getInstance().isCodoonCboot()) {
							MyXiaomiApp.getInstance().setCodoonCboot(false);
						}
						Intent upgrade_intent = new Intent(
								G.ACTION_UPGRADE_SUCCESS);
						mContext.sendBroadcast(upgrade_intent);
					} else {
						CLog.i("info", "5秒后有收到返回的数据：" + CheckResult.toString());
					}
				}
			}

		};
		mContext = context;
		this.upgradeCallback = upgradeCallback;
		this.isHadBindDevice = false;
		this.currentModel = MyXiaomiApp.getInstance().getCurrentModel();
		mBleManager = BleManager.getInstance(mContext);
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
		if (isHadBindDevice) {
			CLog.i("info", "已经绑定，并读出电量了,发送boot转换命令");
			mHandler.removeMessages(ORDER_CONNECT);
			mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 100);
		} else {
			// 刚开始连接，还没有绑定
			isStart = true;
			isBootMode = false;
			isHadSendCheckOrder = false;
			mBleManager.disconnect();
			mBleManager.connect(device, true);

			mTimeoutCheck.startCheckTimeout();
			mTimeoutCheck.setIsConnection(true);
			mTimeoutCheck.setTryConnectCounts(2);
			mTimeoutCheck.setTimeout(TIME_OUT);
		}
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
		CLog.i("info", "CodoonXiaomiDeviceUpgradeManager is stop()");
		isStart = false;
		isBootMode = false;
		isSendingData = false;
		isHadBindDevice = false;
		isHadSendCheckOrder = false;
		MyXiaomiApp.getInstance().setCodoonCboot(false);
		mTimeoutCheck.stopCheckTimeout();
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
				// upgradeCallback.onCheckBootResult(true);
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
	protected void analysis(ArrayList<Integer> datas) {
		if (datas == null) {
			return;
		} else {
			final int msgID = datas.get(1);
			CLog.i("info", "接收的数据datas:" + datas.toString());
			switch (msgID) {
			case TransferStatus.RECEIVE_BINED_ID:
				CLog.i("info", "接收到绑定命令返回的数据：" + msgID);
				// upgradeCallback.onBindDivce();
				Intent intent1 = new Intent(G.ACTION_BIND_SUCCESS);
				mContext.sendBroadcast(intent1);
				CLog.i("info", "停止超时检查");
				mTimeoutCheck.stopCheckTimeout();
				CLog.i("info", "发送读取版本指令");
				mHandler.removeMessages(ORDER_VERSION);
				mHandler.sendEmptyMessageDelayed(ORDER_VERSION, 200);
				break;
			case TransferStatus.RECEIVE_GETVERSION_ID:
				CLog.i("info", "处理版本信息");
				handVersionMessage(datas);
				mTimeoutCheck.stopCheckTimeout();
				CLog.i("info", "发送读取电量指令");
				mHandler.removeMessages(ORDER_CHARGE);
				mHandler.sendEmptyMessageDelayed(ORDER_CHARGE, 200);
				break;
			case TransferStatus.RECEIVE_GETUSERINFO2_ID:
				CLog.i("info", "接收用户信息2：" + datas.get(15));
				Intent intent2 = new Intent();
				intent2.setAction(G.ACTION_READ_CHARGE);
				intent2.putExtra(G.KEY_BRACELET_CHARGE, datas.get(15));
				mContext.sendBroadcast(intent2);
				CLog.i("info", "停止超时检查");
				mTimeoutCheck.stopCheckTimeout();
				isHadBindDevice = true;
				break;
			case TransferStatus.RECEIVE_BOOT_STATE_ID:
				CLog.i("info", "转换boot区重新连接,刷回小米则不需要断开，咕咚需要断开重新连接");
				isBootMode = true;
				MyXiaomiApp.getInstance().setCodoonCboot(true);
				if (MyXiaomiApp.UPGRADE_BACK_XIAOMI == currentModel) {
					CLog.i("info", "刷回小米，不断开");
					mHandler.removeMessages(ORDER_CONNECT);
					mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 100);
				} else if (MyXiaomiApp.UPGRADE_CODOON == currentModel) {
					CLog.i("info", "升级咕咚，断开重新连接");
					mBleManager.disconnect();
					// mTimeoutCheck.setTryConnectCounts(3);
					// mTimeoutCheck.setTimeout(TIME_OUT);
					// mTimeoutCheck.startCheckTimeout();
					// mHandler.removeMessages(CONNECT_AGAIN);
					// mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 100);
					Intent hadCboot_intent = new Intent(G.ACTION_HAD_CBOOT);
					mContext.sendBroadcast(hadCboot_intent);
				}
				break;

			case TransferStatus.RECEIVE_BOOT_VERSION_ID:

				if (null == datas || datas.size() < 6) {
					reSendDataToDevice(mLastSendData);
					return;
				}
				mTimeoutCheck.stopCheckTimeout();
				// upgradeCallback.onGetBootVersion(datas.get(4) + "."
				// + datas.get(5));
				mBleManager.disconnect();
				mTimeoutCheck.setTryConnectCounts(3);
				mTimeoutCheck.setTimeout(TIME_OUT);
				mTimeoutCheck.startCheckTimeout();
				mHandler.removeMessages(CONNECT_AGAIN);
				mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 10);
				break;
			case TransferStatus.RECEIVE_BOOT_CONNECT_ID:
				// upgradeCallback.onConnectBootSuccess();
				isBootMode = true;
				frame = 0;
				isSendingData = true;
				calcToatals();
				sendData();
				break;
			case TransferStatus.RECEIVE_BOOT_UPGRADE_ID:
				// upgradeCallback.onWriteFrame(frame, totalFrame);
				Intent frame_intent = new Intent();
				frame_intent.setAction(G.ACTION_FRAME_NUM);
				frame_intent.putExtra(G.KEY_FRAME_NUM, frame);
				mContext.sendBroadcast(frame_intent);
				// int frame_back = datas.get(3) << 8 + datas.get(4);
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
						// 已经从Cboot模式变成正常模式

						// mTimeoutCheck.stopCheckTimeout();
						CLog.i("info",
								"发送的检验值："
										+ "check："
										+ Integer
												.toHexString(checkData & 0x0000FFFF)
										+ " frame:" + frame);
						SendDataToDevice(SendData
								.postBootEnd((int) (checkData & 0x0000FFFF)));
						// 发送此条指令后，若是正确跳出，则没有返回值，错误了才有返回值
						isHadSendCheckOrder = true;
						// 过五秒去判断是否有返回结果
						mHandler.sendEmptyMessageDelayed(ORDER_CHECKED_RESULT,
								5000L);
					}
				} else {
					Log.e(TAG, "frame: err:" + frame);
					reSendDataToDevice(mLastSendData);
				}

				break;

			case TransferStatus.RECEIVE_UPGRADE_OVER_ID:
				boolean result = parseIsUpSuccess(datas);
				Log.d(TAG, "onCheck：" + result);
				if (result) {
					if (MyXiaomiApp.getInstance().isCodoonCboot()) {
						MyXiaomiApp.getInstance().setCodoonCboot(false);
					}
					Intent upgrade_intent = new Intent(G.ACTION_UPGRADE_SUCCESS);
					mContext.sendBroadcast(upgrade_intent);
				} else {
					CLog.i("info", "校验出错，需重新升级");
					Intent error_intent = new Intent(G.ACTION_CHECK_ERROR);
					mContext.sendBroadcast(error_intent);
					CLog.i("info", "断开重新连接");
					mBleManager.disconnect();
					mTimeoutCheck.setTryConnectCounts(3);
					mTimeoutCheck.setTimeout(TIME_OUT);
					mTimeoutCheck.startCheckTimeout();
					mHandler.removeMessages(CONNECT_AGAIN);
					mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN, 500);
				}
				break;

			default:
				Log.d(TAG, "$$$$$$$4on get other datas:" + msgID);
				if (isVerify) {
					isVerify = false;
				} else {
					reSendDataToDevice(mLastSendData);
				}
				break;
			}
		}
	}

	/**
	 * 根据协议处理版本信息
	 * 
	 * @param datas
	 */
	private void handVersionMessage(ArrayList<Integer> datas) {
		int deviceType = datas.get(3);
		String deviceName = getDeviceNameByType(deviceType);
		int versionHeight = datas.get(4);
		int versionLow = datas.get(5);
		Intent intent = new Intent(G.ACTION_DEVICE_VERSION);
		intent.putExtra(G.KEY_VERSION_NAME, deviceName);
		intent.putExtra(G.KEY_VERSION_HEIGHT, versionHeight);
		intent.putExtra(G.KEY_VERSION_LOW, versionLow);
		mContext.sendBroadcast(intent);

	}

	private String getDeviceNameByType(int deviceType) {
		switch (deviceType) {
		case 0x0A:
			return "网络码表";
		case 0x0B:
			return "咕咚笑";
		case 0x0C:
			return "手环";
		case 0x0D:
			return "糖果";
		case 0x0E:
			return "蓝牙鞋内计步器";
		case 0x10:
			return "咕咚笑BLE";
		case 0x11:
			return "手环2";
		case 0x20:
			return "NFC计步器";
		case 0xA1:
			return "匹克鞋内计步器";
		case 0xA2:
			return "万年康计步器";
		case 0xf1:
			return "手环2boot区";
		default:
			break;
		}
		return "未知设备";
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
		CLog.i("info", "校验结果：" + check);
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
		start(mDevice);
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
		CLog.i("info", "@@@@@@@升级文件路径:" + file.getAbsolutePath() + " 是否存在？"
				+ (file.exists()) + "文件长度：" + file.length());
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
