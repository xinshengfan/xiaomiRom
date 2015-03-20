package com.communication.ble;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.communication.data.CLog;
import com.communication.data.DataUtil;

public class CodmiBleManager {

	private static CodmiBleManager instance;
	private final String TAG = "info";

	private Context mContext;

	private BluetoothDevice mDevice;

	public static final UUID CCC = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");

	protected static final int NOTIFY = 5;

	private BluetoothGatt mBluetoothGatt;
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean isForceDisconnect = false;
	private IConnectCallback mConnectCallback;
	private String mClicentUUID = "0000180f-0000-1000-8000-00805f9b34fb";
	private String mCharacteristicUUID = "00002a19-0000-1000-8000-00805f9b34fb";

	public boolean isConnect = false;
	private BluetoothGattDescriptor mDescriptor;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == NOTIFY) {

				// TODO Auto-generated method stub
				while (!mDescriptor
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
					CLog.i(TAG, "Failed to set descriptor value");
					mHandler.sendEmptyMessageDelayed(NOTIFY, 10);
				}
				// mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				if (mBluetoothGatt != null) {

					boolean result = mBluetoothGatt
							.writeDescriptor(mDescriptor);
					CLog.i(TAG, "notify suceess ? " + result);
					if (result) {
						CLog.i("info", "mConnectCallback is null?"
								+ (mConnectCallback == null));
						if (null != mConnectCallback)
							mConnectCallback.discovered();
					}
				} else {
					CLog.i(TAG, "mBluetoothGatt is null ");
				}

			}
		}

	};

	public CodmiBleManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		initialize();
	}

	public static CodmiBleManager getInstance(Context context) {
		if (null == instance) {
			instance = new CodmiBleManager(context);
		}
		instance.setContext(context);
		return instance;
	}

	private void setContext(Context context) {
		// TODO Auto-generated method stub
		mContext = context;
	}

	public void register(IConnectCallback callback) {
		CLog.i("info", "callback is null?" + (callback == null));
		mConnectCallback = callback;
	}

	public void unRegister(IConnectCallback callback) {
		mConnectCallback = null;
	}

	/**
	 * 
	 * @param clientUUID
	 */
	public void setClientUUID(String clientUUID) {
		mClicentUUID = clientUUID;
	}

	/**
	 * 
	 * @param characteristicUUID
	 */
	public void setCharacteristicUUID(String characteristicUUID) {
		mCharacteristicUUID = characteristicUUID;
	}

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	private boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) mContext
					.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		Log.d(TAG, "start ble service");
		return true;
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			Log.d(TAG, "onCharacteristicChanged()");
			byte[] bytes = characteristic.getValue();
			int length = bytes.length;
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int i = 0; i < length; i++) {

				// values.add(((int) bytes[i]) & 0x000000ff);
				values.add(bytes[i] & 0xff);
			}

			DataUtil.DebugPrint(values);

			if (mConnectCallback != null) {
				mConnectCallback.getValues(values);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicRead()");
			super.onCharacteristicRead(gatt, characteristic, status);
			// characteristic.getWriteType();
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicWrite() result ?  "
					+ (status == BluetoothGatt.GATT_SUCCESS));
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			Log.d(TAG, "onConnectionStateChange: status:" + status
					+ " newState:" + newState);
			if (status == BluetoothGatt.GATT_SUCCESS) {

				if (newState == BluetoothProfile.STATE_CONNECTED
						&& mBluetoothGatt != null) {
					isConnect = true;
					mBluetoothGatt = gatt;
					Log.d(TAG, "onConnectionStateChange:connected");

					boolean flg = mBluetoothGatt.discoverServices();

					Log.d(TAG, "disconver service:" + flg);
					if (null != mConnectCallback) {
						mConnectCallback
								.connectState(mDevice, status, newState);
					}
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED
						&& mBluetoothGatt != null) {
					Log.d(TAG, "onConnectionStateChange:disconnected");
					if (null != mConnectCallback) {

						mConnectCallback
								.connectState(mDevice, status, newState);
					}
				}
			} else {
				isConnect = false;
				if (null != mConnectCallback) {

					mConnectCallback.connectState(mDevice, status, newState);
				}
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.d(TAG, "onDescriptorRead() status? :"
					+ (status == BluetoothGatt.GATT_SUCCESS));
			mHandler.removeMessages(NOTIFY);
			if (status == BluetoothGatt.GATT_SUCCESS) {

				descriptor
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

				gatt.writeDescriptor(descriptor);
			} else {
				gatt.readDescriptor(descriptor);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onDescriptorWrite() status ? "
					+ (status == BluetoothGatt.GATT_SUCCESS));

			super.onDescriptorWrite(gatt, descriptor, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (null != mConnectCallback) {
					mConnectCallback.discovered();
				}
			} else {
				descriptor
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				gatt.writeDescriptor(descriptor);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// super.onServicesDiscovered(gatt, status);
			Log.d(TAG, "onServicesDiscovered()");
			if (status == BluetoothGatt.GATT_SUCCESS) {

				mHandler.removeMessages(NOTIFY);
				UUID serviceUUID = UUID.fromString(mClicentUUID);
				UUID characteristicUUID = UUID.fromString(mCharacteristicUUID);
				enableNotify(gatt, serviceUUID, characteristicUUID, CCC);
			} else {
				Log.e(TAG, "err reson:" + status + " and try connect ble agin");

			}
		}
	};
	private String mBluetoothDeviceAddress;

	/**
	 * connect device
	 * 
	 * @param device
	 * @param autoconnect
	 */
	public void connect(BluetoothDevice device, boolean autoconnect) {
		Log.d(TAG,
				"connect device:" + device.getAddress() + " name:"
						+ device.getName());
		// 尝试先前连接
		// if (mDevice != null &&
		// mDevice.getAddress().equals(device.getAddress())
		// && mBluetoothManager != null) {
		// Log.d(TAG, "尝试先前的连接");
		// mBluetoothGatt.connect();
		// return;
		// }
		//if (mBluetoothGatt == null) {
		Log.d(TAG, "mBluetoothGatt == null");
		mDevice = device;
		isForceDisconnect = true;
		mBluetoothGatt = device.connectGatt(mContext, autoconnect,
				mGattCallback);
		// } else {
		// CLog.i("info", "mBluetoothGatt ！= null直接连接");
		// mDevice = device;
		// mBluetoothGatt.connect();
		// }
	}

	/**
	 * close connect device
	 * 
	 * @param device
	 */
	public void close() {
		isConnect = false;
		mHandler.removeMessages(NOTIFY);
		if (mBluetoothGatt != null) {
			isForceDisconnect = false;
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	public void disconnect() {
		CLog.i("info", "断开连接");
		isConnect = false;
		mHandler.removeMessages(NOTIFY);
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
			// CLog.i("info", "清空蓝牙缓存");
			// mBluetoothGatt.close();
			// mBluetoothGatt = null; //清空缓存
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean getConnectState() {
		return isForceDisconnect;
	}

	/**
	 * 
	 * @param device
	 * @param serviceUUID
	 * @param characteristicUUID
	 */
	private void enableNotification(UUID serviceUUID, UUID characteristicUUID) {
		boolean result = false;
		Log.i(TAG, "enableHRNotification ");
		BluetoothGattService gattService = mBluetoothGatt
				.getService(serviceUUID);
		if (gattService == null) {
			Log.e(TAG, "HRP service not found!");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}
		BluetoothGattCharacteristic mCharac = gattService
				.getCharacteristic(characteristicUUID);
		if (mCharac == null) {
			Log.e(TAG, "HEART RATE MEASUREMENT charateristic not found!");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}
		BluetoothGattDescriptor mCcc = mCharac.getDescriptor(CCC);
		if (mCcc == null) {
			Log.e(TAG,
					"CCC for HEART RATE MEASUREMENT charateristic not found!");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}
		result = mBluetoothGatt.readDescriptor(mCcc);
		if (result == false) {
			Log.e(TAG, "readDescriptor() is failed");
			if (null != mConnectCallback) {
				mConnectCallback.connectState(mDevice, 1,
						BluetoothAdapter.STATE_DISCONNECTED);
			}
			return;
		}

	}

	/**
	 * 
	 * @param device
	 * @param serviceUUID
	 * @param characteristicUUID
	 */
	private boolean enableNotify(BluetoothGatt gatt, UUID serviceUUID,
			UUID characteristicUUID, UUID descriptorUUID) {
		if (null == gatt) {
			return false;
		}

		Log.d(TAG, "enableNotification ");
		List<BluetoothGattService> list = mBluetoothGatt.getServices();
		for (BluetoothGattService service : list) {
			Log.d(TAG, "service:" + service.getUuid());
		}

		BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
		if (service == null) {
			Log.e(TAG, " service not found!");
			List<BluetoothGattService> services = mBluetoothGatt.getServices();
			for (BluetoothGattService s : services) {
				String uuids = s.getUuid().toString();
				CLog.i("info", "可用service:" + uuids);
				// if (uuids.contains("180")) {
				// CLog.i("info", "getSupportedGattService uuid :" + uuids);
				// service = s;
				// }
			}
			if (null != mConnectCallback)
				mConnectCallback.connectState(mDevice,
						BluetoothGatt.GATT_FAILURE,
						BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}
		Log.d(TAG, "find service");
		BluetoothGattCharacteristic mHRMcharac = service
				.getCharacteristic(characteristicUUID);
		if (mHRMcharac == null) {
			Log.e(TAG, " charateristic not found!");
			if (null != mConnectCallback)
				mConnectCallback.connectState(mDevice,
						BluetoothGatt.GATT_FAILURE,
						BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}
		Log.d(TAG, "find BluetoothGattCharacteristic");

		boolean isNotify = false;

		boolean result = gatt.setCharacteristicNotification(mHRMcharac, true);

		Log.d(TAG, "setCharacteristicNotification result :" + result);

		BluetoothGattDescriptor descriptor = mHRMcharac
				.getDescriptor(descriptorUUID);
		if (null != descriptor) {
			mDescriptor = descriptor;
			mHandler.sendEmptyMessageDelayed(NOTIFY, 300);
			// gatt.readDescriptor(descriptor);
			//
			// descriptor
			// .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			// isNotify = gatt.writeDescriptor(descriptor);

		} else {
			if (null != mConnectCallback)
				mConnectCallback.connectState(mDevice,
						BluetoothGatt.GATT_FAILURE,
						BluetoothGatt.STATE_DISCONNECTED);
		}

		return isNotify;
	}

	/**
	 * 
	 * @param iDevice
	 * @param writeServiceUUID
	 * @param characteristicID
	 * @param bytes
	 */
	public void writeIasAlertLevel(String writeServiceUUID,
			String characteristicID, byte[] bytes) {
		String str = "write ";
		for (int i = 0; i < bytes.length; i++) {
			str += Integer.toHexString(bytes[i] & 0xff) + ",";
		}

		Log.i(TAG, str);
		if (null == mBluetoothGatt) {
			return;
		}
		BluetoothGattService alertService = mBluetoothGatt.getService(UUID
				.fromString(writeServiceUUID));
		if (alertService == null) {
			// showMessage("Immediate Alert service not found!");
			return;
		}
		BluetoothGattCharacteristic mCharacter = alertService
				.getCharacteristic(UUID.fromString(characteristicID));
		if (mCharacter == null) {
			// showMessage("Immediate Alert Level charateristic not found!");
			return;
		}
		boolean status = false;
		int writeType = mCharacter.getWriteType();
		Log.d(TAG, "Character writeType：" + writeType);
		mCharacter.setValue(bytes);
		mCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		status = mBluetoothGatt.writeCharacteristic(mCharacter);
		Log.d(TAG, "write status:" + status);
	}

	/**
	 * 手动读取数据
	 * 
	 * @param characteristic
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		Log.w(TAG, "手动读取数据readCharacteristic");
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	public BluetoothGattService getSupportedGattService() {
		if (mBluetoothGatt == null) {
			CLog.i("info", "mBluetoothGatt is  null");
			return null;
		}
		BluetoothGattService service = null;
		List<BluetoothGattService> list = mBluetoothGatt.getServices();
		CLog.i("info", "BluetoothGattServicelist size:" + list.size());
		for (int i = 0; i < list.size(); i++) {
			BluetoothGattService bService = list.get(i);
			String uuid = bService.getUuid().toString();
			if (uuid.contains("180f")) {
				CLog.i("info", "getSupportedGattService uuid :" + uuid);
				service = bService;
				return service;
			} else {
				CLog.i("info", "uuid" + uuid);
			}
		}
		return service;
	}
}
