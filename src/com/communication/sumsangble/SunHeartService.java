package com.communication.sumsangble;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.conn.DefaultClientConnection;

import com.communication.ble.DisconveryManager;
import com.communication.ble.IConnectCallback;
import com.communication.data.TimeoutCheck;
import com.communication.data.TimeoutCheck.ITimeoutCallback;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.style.BulletSpan;
import android.util.Log;

/**
 * 用于心率带的连接
 * @author workEnlong
 *
 */
public class SunHeartService extends Service {

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mDevice;
	private Handler mHandler;

	private static final int CONECT_HEART = 1;
	private static final int CONECT_FAILED = 2;
	private static final int ACTION_SEARTCH = 3;

	private static final int SEARTCH_TIMEOUT = 4;
	private static final int CONNECT_TIMEOUT = 5;

	private int TIME_OUT_SEARTCH = 100000;
	private int TIME_OUT_CONNECT = 20000;

	protected static final String TAG = "SunHeartService";
	private SunHeartBLEGatt mHeartManager;
	private boolean isAutoConnect = true;

	private boolean isStart;
	private boolean isConnect;
	private boolean isSeartching;

	private int heartRate;
	private int battery;

	private List<BLEConnectCallback> mIConnectCallbacks;
	private LocalBinder mBinder = new LocalBinder();

	public static final String ACTION_HEARTRATE_CHANGED = "com.communication.sumsangble.heartrate";
	public static final String ACTION_HEARTRATE_FIND = "com.communication.sumsangble.finddevice";

	public static final String ACTION_SCAN_DEVICE = "com.communication.sumsangble.seartchdevice";
	public static final String EXTRA_DEVICE = "com.communication.sumsangble.device";
	public static final String EXTRA_AUTO = "com.communication.sumsangble.autoconnect";

	private static final int DEFAULT_COUNT = 3;
	private int tryCount = DEFAULT_COUNT;
	private SharedPreferences mSharedPreferences;

	public static final String HEART_RATE_ADRRESS_KEY = "HeartRateAdress";
	public static final String HEART_RATE_PRF = "MyPrefsFile";

	
	private DisconveryManager mDisconveryManager;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mSharedPreferences = this.getSharedPreferences(HEART_RATE_PRF, 0);
		mIConnectCallbacks = new ArrayList<BLEConnectCallback>();
		mHeartManager = new SunHeartBLEGatt(this.getApplicationContext(), new BLEConnectCallback() {

			@Override
			public void onGetValueAndTypes(int value, int type) {
				// TODO Auto-generated method stub

				if (type == SunHeartBLEGatt.TYPE_HEART_RATE) {
					Log.i(TAG, "heart rate is " + value);
					heartRate = value;
				} else {
					battery = value;

					Log.i(TAG, "battery is " + value);
				}

				for (BLEConnectCallback mCallback : mIConnectCallbacks) {
					mCallback.onGetValueAndTypes(value, type);
				}
			}

			@Override
			public boolean onConnectStateChanged(BluetoothDevice device,
					int status, int newState) {

				if (newState == BluetoothAdapter.STATE_CONNECTED) {
					isConnect = true;
					mHandler.removeMessages(CONNECT_TIMEOUT);
					mHandler.removeMessages(CONECT_HEART);

					
					mSharedPreferences.edit().putString(HEART_RATE_ADRRESS_KEY, device.getAddress()).commit();
					
					for (BLEConnectCallback mCallback : mIConnectCallbacks) {

						mCallback.onConnectStateChanged(device, status,
								newState);
					}

				} else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {

					isConnect = false;

					if (isStart && status != BluetoothGatt.GATT_SUCCESS) {

						for (BLEConnectCallback mCallback : mIConnectCallbacks) {

							mCallback.onConnectStateChanged(device, status,
									newState);
						}
						tryCount = DEFAULT_COUNT;
						reConnect();
					}
				}

				Log.d(TAG, "connect state:" + isConnect);
				return false;
			}

			@Override
			public boolean onSeartchCallBack(BluetoothDevice device) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onConnectTimeOut() {
				// TODO Auto-generated method stub
				for (BLEConnectCallback mCallback : mIConnectCallbacks) {
					mCallback.onConnectTimeOut();
				}
				return false;
			}
		});
		BluetoothManager manager = (BluetoothManager) this
		.getSystemService(Context.BLUETOOTH_SERVICE);
		
		mBluetoothAdapter = manager.getAdapter();
		
		mDisconveryManager =  new DisconveryManager(this, new DisconveryManager.OnSeartchCallback() {
			
			@Override
			public boolean onSeartchTimeOut() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onSeartch(BluetoothDevice device) {
				// TODO Auto-generated method stub
				

				// TODO Auto-generated method stub
				String name = device.getName();

				Log.d(TAG, "find device: " + name);

				for (BLEConnectCallback mIConnectCallback : mIConnectCallbacks) {
					mIConnectCallback.onSeartchCallBack(device);
				}


				if (isAutoConnect) {
					String myAdress = mSharedPreferences.getString(
							HEART_RATE_ADRRESS_KEY, null);
					if (null != myAdress) {

						if (device.getAddress().equals(myAdress)) {

							mDevice = device;
							stopScan();
							connectTodevice();
						}
					} else {
					}

				}
				
				return false;
			}
		});
		
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {

				case ACTION_SEARTCH:
					startScan();
					break;

				case CONECT_HEART:
					startCountConnectTime();
					mHeartManager.connect(mDevice, true);
					break;
				case SEARTCH_TIMEOUT:
					Log.d(TAG, "seartch time out");
					stopScan();
					for (BLEConnectCallback mIConnectCallback : mIConnectCallbacks) {
						mIConnectCallback.onSeartchCallBack(null);
					}

					break;
				case CONNECT_TIMEOUT:
					Log.d(TAG, "connect time out");
					if (tryCount-- > 0) {

						reConnect();
					} else {
						for (BLEConnectCallback mIConnectCallback : mIConnectCallbacks) {
							mIConnectCallback.onConnectTimeOut();
						}
					}
					break;
				default:
					break;
				}
			}

		};

		registerReceiver();
	}

	protected void startCountConnectTime() {
		// TODO Auto-generated method stub
		mHandler.removeMessages(CONNECT_TIMEOUT);
		mHandler.sendEmptyMessageDelayed(CONNECT_TIMEOUT, TIME_OUT_CONNECT);
	}

	/**
	 * 500ms later reconnect to device
	 */
	public void reConnect() {
		mHeartManager.close();
		mHandler.removeMessages(CONNECT_TIMEOUT);
		mHandler.removeMessages(CONECT_HEART);
		mHandler.removeMessages(ACTION_SEARTCH);
		mHandler.removeMessages(SEARTCH_TIMEOUT);
		mHandler.sendEmptyMessageDelayed(CONECT_HEART, 1000);
	}

	/**
	 * 500ms later connect to device
	 */
	public void connectTodevice() {
		tryCount = DEFAULT_COUNT;
		mHandler.removeMessages(CONNECT_TIMEOUT);
		mHandler.removeMessages(CONECT_HEART);
		mHandler.removeMessages(ACTION_SEARTCH);
		mHandler.removeMessages(SEARTCH_TIMEOUT);
		mHandler.sendEmptyMessageDelayed(CONECT_HEART, 500);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		isStart = false;
		isConnect = false;
		mHandler.removeMessages(CONECT_HEART);
		mHandler.removeMessages(CONNECT_TIMEOUT);
		mHeartManager.close();
		if (isSeartching) {
			stopScan();
		}
		unregisterReceiver();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	public boolean startScan() {
		Log.i(TAG, "isSeartching : " + isSeartching);
		if (mBluetoothAdapter.isEnabled()) {

			mHandler.removeMessages(CONNECT_TIMEOUT);
			mHandler.removeMessages(CONECT_HEART);
			mHandler.removeMessages(ACTION_SEARTCH);
			mHandler.removeMessages(SEARTCH_TIMEOUT);
			mHandler.sendEmptyMessageDelayed(SEARTCH_TIMEOUT, TIME_OUT_SEARTCH);

			if (isSeartching) {
				return true;
			} else {
				isSeartching = true;
			}
			mDevice = null;
		} else {
			isSeartching = false;
			return false;
		}
		return mDisconveryManager.startSearch();
	}

	public void stopScan() {
		isSeartching = false;
		mHandler.removeMessages(ACTION_SEARTCH);
		mHandler.removeMessages(SEARTCH_TIMEOUT);
		mDisconveryManager.stopSearch();
	}

	private BroadcastReceiver mBleReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (null != action && action.equals(ACTION_SCAN_DEVICE)) {
				isAutoConnect = intent.getBooleanExtra(EXTRA_AUTO, true);
				startScan();
			}
		}

	};

	public void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SCAN_DEVICE);
		this.registerReceiver(mBleReceiver, filter);
	}

	public void unregisterReceiver() {
		try {
			this.unregisterReceiver(mBleReceiver);
		} catch (Exception e) {

		}
	}

	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}

	public class LocalBinder extends Binder {

		public LocalBinder() {
		}

		/**
		 * After use this method must use method
		 * unRegisterCallback(BLEConnectCallback mIConnectCallback) remove it
		 * 
		 * @param mIConnectCallback
		 */
		public void registerConnectCallBack(BLEConnectCallback mIConnectCallback) {
			mIConnectCallbacks.add(mIConnectCallback);
		}

		public void unRegisterCallback(BLEConnectCallback mIConnectCallback) {
			mIConnectCallbacks.remove(mIConnectCallback);
		}

		public boolean startSeartch(int timeout) {
			TIME_OUT_SEARTCH = timeout;

			return startScan();
		}

		public boolean startSeartch() {
			// Editor mEditor = mSharedPreferences.edit();
			// mEditor.remove("HeartRateAdress");
			// mEditor.commit();

			return startScan();
		}

		/**
		 * start search ble device at timeValue later;
		 * 
		 * @param time
		 * @return
		 */
		public boolean startSearchDelay(int time) {
			// Editor mEditor = mSharedPreferences.edit();
			// mEditor.remove("HeartRateAdress");
			// mEditor.commit();
			return mHandler.sendEmptyMessageDelayed(ACTION_SEARTCH, time);
		}

		public void stopSeartch() {

			stopScan();
		}

		/**
		 * 
		 * @param mDevice
		 * @param isAutoConnect
		 *            always true
		 */
		public void connect(BluetoothDevice mDevice, boolean isAutoConnect) {
			SunHeartService.this.mDevice = mDevice;
			isStart = true;
			connectTodevice();
		}

		public void disConnect() {
			isStart = false;
			mHandler.removeMessages(CONNECT_TIMEOUT);
			mHeartManager.disconnect();
			mHeartManager.close();
		}

		public int getHeartRate() {
			return heartRate;
		}

		public int getBattery() {
			return battery;

		}

		public BluetoothDevice getConnectedDevice() {
			return mDevice;
		}
		
		/**
		 * 曾经是否绑定
		 * @return
		 */
		public boolean isEverBind(){
			String address = mSharedPreferences.getString(HEART_RATE_ADRRESS_KEY , null);
			return (address == null) ? false : true;
		}
	}

}
