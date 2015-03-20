package com.communication.sumsangble;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.communication.ble.IConnectCallback;

public class SunHeartBLEGatt {

	private static final String TAG = "SunHeartBLEManager";
	private BluetoothGatt mBluetoothGatt;
	private BLEConnectCallback mConnectCallback;

	private Context mContext;

	private BluetoothDevice mDevice;

	public static final int TYPE_HEART_RATE = 1;
	public static final int TYPE_BATTERY = 2;
	private int curState = TYPE_HEART_RATE;

	private boolean isNotifyHeart;
	private boolean isNotifyBattery;
	private boolean isStart;
	public SunHeartBLEGatt(Context mContext, BLEConnectCallback callback) {
		this.mContext = mContext;
		mConnectCallback = callback;
	}

	/**
	 * connect device
	 * 
	 * @param device
	 * @param autoconnect
	 */
	public void connect(BluetoothDevice device, boolean autoconnect) {
		isStart = true;
		Log.d(TAG, "connect device");
		if (mBluetoothGatt == null) {
			mDevice = device;
			mBluetoothGatt = device.connectGatt(mContext, autoconnect,
					mGattCallback);
		} else {
			mDevice = device;
			mBluetoothGatt.connect();
		}
	}

	/**
	 * close connect device
	 * 
	 * @param device
	 */
	public void close() {
		isStart = false;
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	public void disconnect() {
		isStart = false;
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);

			int value = characteristic.getValue()[0];

			if (isHeartRateInUINT16(characteristic.getValue()[0])) {
//				Log.d(TAG, "HeartRateInUINT16");
				value = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_UINT16, 1);
			} else {
//				Log.d(TAG, "HeartRateInUINT8");
				value = characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_UINT8, 1);
			}

			String typeId = characteristic.getUuid().toString();
			if (typeId.equals(BLEProfile.hreatCharactertUUID)) {

				mConnectCallback.onGetValueAndTypes(value, TYPE_HEART_RATE);
			} else if (typeId.equals(BLEProfile.batteryCharactUUID)) {
				mConnectCallback.onGetValueAndTypes(value, TYPE_BATTERY);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicRead()");
			super.onCharacteristicRead(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicWrite()");
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			Log.d(TAG, "onConnectionStateChange()");
			if (status == BluetoothGatt.GATT_SUCCESS) {

				if (newState == BluetoothProfile.STATE_CONNECTED
						&& mBluetoothGatt != null) {
					mBluetoothGatt = gatt;
					Log.d(TAG, "onConnectionStateChange:connected");
					boolean flg = mBluetoothGatt.discoverServices();
					Log.d(TAG, "disconver service:" + flg);
					mConnectCallback.onConnectStateChanged(mDevice, status,
							newState);
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED
						&& mBluetoothGatt != null) {
					Log.d(TAG, "onConnectionStateChange:disconnected");
					mConnectCallback.onConnectStateChanged(mDevice, status,
							newState);
				}
			} else {
				mConnectCallback.onConnectStateChanged(mDevice,
						BluetoothGatt.GATT_FAILURE, newState);
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.d(TAG, "onDescriptorRead()");
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// TODO Auto-generated method stub
			super.onDescriptorWrite(gatt, descriptor, status);
			if(status == BluetoothGatt.GATT_FAILURE && isStart){
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
					isNotifyHeart = enableNotify(gatt,
							UUID.fromString(BLEProfile.heartServiceUUID),
							UUID.fromString(BLEProfile.hreatCharactertUUID),
							UUID.fromString(BLEProfile.heartRateDescripUUID));

//					isNotifyBattery = enableNotify(gatt,
//							UUID.fromString(BLEProfile.batteryServiceUUID),
//							UUID.fromString(BLEProfile.batteryCharactUUID),
//							UUID.fromString(BLEProfile.batteryDescriptorUUID));

			} else {
				Log.e(TAG, "err reson:" + status + " and try connect ble agin");

			}
		}
	};

	private boolean isHeartRateInUINT16(byte flags) {
		if ((flags & 0x01) != 0)
			return true;
		return false;
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

		Log.i(TAG, "enableNotification ");
		BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
		if (service == null) {
			Log.e(TAG, " service not found!");
			mConnectCallback.onConnectStateChanged(mDevice,
					BluetoothGatt.GATT_FAILURE,
					BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}

		BluetoothGattCharacteristic mHRMcharac = service
				.getCharacteristic(characteristicUUID);
		if (mHRMcharac == null) {
			Log.e(TAG, " charateristic not found!");
			mConnectCallback.onConnectStateChanged(mDevice,
					BluetoothGatt.GATT_FAILURE,
					BluetoothGatt.STATE_DISCONNECTED);
			return false;
		}

		boolean isNotify = false;
		gatt.setCharacteristicNotification(mHRMcharac, true);

		BluetoothGattDescriptor descriptor = mHRMcharac
				.getDescriptor(descriptorUUID);
		if (null != descriptor) {
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			isNotify = gatt.writeDescriptor(descriptor);
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
	public void writeIasAlertLevel(BluetoothDevice iDevice,
			String writeServiceUUID, String characteristicID, byte[] bytes) {

		BluetoothGattService alertService = mBluetoothGatt.getService(UUID
				.fromString(writeServiceUUID));
		if (alertService == null) {
			// showMessage("Immediate Alert service not found!");
			return;
		}
		BluetoothGattCharacteristic alertLevel = alertService
				.getCharacteristic(UUID.fromString(characteristicID));
		if (alertLevel == null) {
			// showMessage("Immediate Alert Level charateristic not found!");
			return;
		}
		boolean status = false;
		int storedLevel = alertLevel.getWriteType();
		Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);
		// alertLevel.setValue(iAlertLevel,
		// BluetoothGattCharacteristic.FORMAT_UINT8, 0);
		alertLevel.setValue(bytes);
		alertLevel
				.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		status = mBluetoothGatt.writeCharacteristic(alertLevel);
		Log.d(TAG, "writeIasAlertLevel() - status=" + status);
	}

	public int getCurState() {
		return curState;
	}

	public void setCurState(int curState) {
		this.curState = curState;
	}

}
