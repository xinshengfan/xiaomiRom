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
					// �Ѿ������˼���ָ��
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
					CLog.i("info", "��Cbootģʽ��ֱ�ӽ���ˢ��");
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
					CLog.i("info", "������Ϣ��ORDER_BIND ׼����");
					mHandler.sendEmptyMessageDelayed(ORDER_BIND, 300);
				} else if (isStart && isHadBindDevice
						&& (currentModel == MyXiaomiApp.UPGRADE_CODOON)) {
					// �Ѿ����豸���������������豸
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
							"����״̬��" + newState + " �豸���֣�" + mDevice.getName()
									+ ";��ַ��" + mDevice.getAddress());
					// �����Ͼͷ���һ���㲥
					Intent connect_intent = new Intent(
							G.ACTION_CONNECTED_BRACELET);
					mContext.sendBroadcast(connect_intent);
					hasFound = true;
					mHandler.removeMessages(CONNECT_AGAIN);

				} else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
					CLog.i("info", "�Ͽ�����");
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
				CLog.i("info", "���յ�handler:" + msg.what);
				if (msg.what == ORDER_CONNECT) {
					if (isBootMode) {
						CLog.i(TAG,
								"send order to isBootMode device getPostConnection");
						SendDataToDevice(SendData.postConnectBootOrder());
						readDeviceByHand();
					} else {
						CLog.i("info", "��ת����bootģʽָ��");
						SendDataToDevice(SendData.postBootMode());
						readDeviceByHand();
					}
				} else if (msg.what == CONNECT_AGAIN) {
					CLog.i(TAG, "connect  device  again");
					mBleManager.connect(mDevice, true);
				} else if (msg.what == SEND_CONTENT) {
					isSendingData = true;
				} else if (msg.what == ORDER_BIND) {
					CLog.i("info", "���Ͱ����" + SendData.getPostBindOrder());
					SendDataToDevice(SendData.getPostBindOrder());
					readDeviceByHand();
				} else if (msg.what == ORDER_CHARGE) {
					CLog.i("info", "��ʼ���Ͷ�ȡ����ָ�");
					SendDataToDevice(SendData.getPostGetUserInfo2());
					readDeviceByHand();
				}
				// else if (msg.what == ORDER_CHECK) {
				// CLog.i("info", "mhandler ������У���");
				// SendDataToDevice(SendData
				// .postBootEnd((int) (checkData & 0x0000FFFF)));
				// readDeviceByHand();
				// }
				else if (msg.what == ORDER_VERSION) {
					CLog.i("info", "mhander �����Ͷ�ȡ�汾ָ��");
					SendDataToDevice(SendData.getPostDeviceTypeVersion());
					readDeviceByHand();
				} else if (msg.what == ORDER_CHECKED_RESULT) {
					CLog.i("info", "����ڷ����˼���ָ��5����Ƿ��з���ֵ");
					if (CheckResult == null) {
						// û�з���ֵ��˵���ɹ���
						if (MyXiaomiApp.getInstance().isCodoonCboot()) {
							MyXiaomiApp.getInstance().setCodoonCboot(false);
						}
						Intent upgrade_intent = new Intent(
								G.ACTION_UPGRADE_SUCCESS);
						mContext.sendBroadcast(upgrade_intent);
					} else {
						CLog.i("info", "5������յ����ص����ݣ�" + CheckResult.toString());
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
			CLog.i("info", "�Ѿ��󶨣�������������,����bootת������");
			mHandler.removeMessages(ORDER_CONNECT);
			mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 100);
		} else {
			// �տ�ʼ���ӣ���û�а�
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
			CLog.i("info", "���յ�����datas:" + datas.toString());
			switch (msgID) {
			case TransferStatus.RECEIVE_BINED_ID:
				CLog.i("info", "���յ�������ص����ݣ�" + msgID);
				// upgradeCallback.onBindDivce();
				Intent intent1 = new Intent(G.ACTION_BIND_SUCCESS);
				mContext.sendBroadcast(intent1);
				CLog.i("info", "ֹͣ��ʱ���");
				mTimeoutCheck.stopCheckTimeout();
				CLog.i("info", "���Ͷ�ȡ�汾ָ��");
				mHandler.removeMessages(ORDER_VERSION);
				mHandler.sendEmptyMessageDelayed(ORDER_VERSION, 200);
				break;
			case TransferStatus.RECEIVE_GETVERSION_ID:
				CLog.i("info", "����汾��Ϣ");
				handVersionMessage(datas);
				mTimeoutCheck.stopCheckTimeout();
				CLog.i("info", "���Ͷ�ȡ����ָ��");
				mHandler.removeMessages(ORDER_CHARGE);
				mHandler.sendEmptyMessageDelayed(ORDER_CHARGE, 200);
				break;
			case TransferStatus.RECEIVE_GETUSERINFO2_ID:
				CLog.i("info", "�����û���Ϣ2��" + datas.get(15));
				Intent intent2 = new Intent();
				intent2.setAction(G.ACTION_READ_CHARGE);
				intent2.putExtra(G.KEY_BRACELET_CHARGE, datas.get(15));
				mContext.sendBroadcast(intent2);
				CLog.i("info", "ֹͣ��ʱ���");
				mTimeoutCheck.stopCheckTimeout();
				isHadBindDevice = true;
				break;
			case TransferStatus.RECEIVE_BOOT_STATE_ID:
				CLog.i("info", "ת��boot����������,ˢ��С������Ҫ�Ͽ���������Ҫ�Ͽ���������");
				isBootMode = true;
				MyXiaomiApp.getInstance().setCodoonCboot(true);
				if (MyXiaomiApp.UPGRADE_BACK_XIAOMI == currentModel) {
					CLog.i("info", "ˢ��С�ף����Ͽ�");
					mHandler.removeMessages(ORDER_CONNECT);
					mHandler.sendEmptyMessageDelayed(ORDER_CONNECT, 100);
				} else if (MyXiaomiApp.UPGRADE_CODOON == currentModel) {
					CLog.i("info", "�������ˣ��Ͽ���������");
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
						// Log.d(TAG, "check��" + Integer.toHexString(checkData &
						// 0x0000FFFF) + " frame:" + frame);
						// �Ѿ���Cbootģʽ�������ģʽ

						// mTimeoutCheck.stopCheckTimeout();
						CLog.i("info",
								"���͵ļ���ֵ��"
										+ "check��"
										+ Integer
												.toHexString(checkData & 0x0000FFFF)
										+ " frame:" + frame);
						SendDataToDevice(SendData
								.postBootEnd((int) (checkData & 0x0000FFFF)));
						// ���ʹ���ָ���������ȷ��������û�з���ֵ�������˲��з���ֵ
						isHadSendCheckOrder = true;
						// ������ȥ�ж��Ƿ��з��ؽ��
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
				Log.d(TAG, "onCheck��" + result);
				if (result) {
					if (MyXiaomiApp.getInstance().isCodoonCboot()) {
						MyXiaomiApp.getInstance().setCodoonCboot(false);
					}
					Intent upgrade_intent = new Intent(G.ACTION_UPGRADE_SUCCESS);
					mContext.sendBroadcast(upgrade_intent);
				} else {
					CLog.i("info", "У���������������");
					Intent error_intent = new Intent(G.ACTION_CHECK_ERROR);
					mContext.sendBroadcast(error_intent);
					CLog.i("info", "�Ͽ���������");
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
	 * ����Э�鴦��汾��Ϣ
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
			return "�������";
		case 0x0B:
			return "����Ц";
		case 0x0C:
			return "�ֻ�";
		case 0x0D:
			return "�ǹ�";
		case 0x0E:
			return "����Ь�ڼƲ���";
		case 0x10:
			return "����ЦBLE";
		case 0x11:
			return "�ֻ�2";
		case 0x20:
			return "NFC�Ʋ���";
		case 0xA1:
			return "ƥ��Ь�ڼƲ���";
		case 0xA2:
			return "���꿵�Ʋ���";
		case 0xf1:
			return "�ֻ�2boot��";
		default:
			break;
		}
		return "δ֪�豸";
	}

	private boolean checkBackIsRight(ArrayList<Integer> datas) {
		// TODO Auto-generated method stub
		// if(null == datas || datas.size() < 3){
		// return false;
		// }
		//
		// // ��֡����ʼ�� ֱ��У��λ
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
		CLog.i("info", "У������" + check);
		return (check == 0);
	}

	public String getUpgradeFilePath() {
		return filePath;
	}

	public void setUpgradeFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * ����д�ļ����߳�
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
		CLog.i("info", "@@@@@@@�����ļ�·��:" + file.getAbsolutePath() + " �Ƿ���ڣ�"
				+ (file.exists()) + "�ļ����ȣ�" + file.length());
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
			// ÿ�ζ�ȡ18���ֽڣ� Ȼ����̽���ȴ����ȴ��´εĻ��Ѽ���
			int length = 0;
			if (-1 != (length = input.read(buffer))) {

				for (int i = 0; i < length; i++) { // ����У���
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
			Log.e(TAG, "exception��" + frame + " total:" + totalFrame);

		}
		return true;
	}

	protected void readDeviceByHand() {
		CLog.i("info", "�ֶ���ȡ�豸����");
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
					"�ֶ���ȡ�豸����:" + Arrays.toString(characteristic.getValue()));
			mBleManager.readCharacteristic(characteristic);
		}

	}
}
