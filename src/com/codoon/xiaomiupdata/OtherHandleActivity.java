package com.codoon.xiaomiupdata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import cn.com.smartdevices.bracelet.BraceletApp;
import cn.com.smartdevices.bracelet.Keeper;

import com.codoon.xiaomiupdata.ui.NoSupportVersionActivity;
import com.codoon.xiaomiupdata.ui.UpgradSuccessDownApp;
import com.codoon.xiaomiupdata.ui.UpgradeSuccessActivity;
import com.communication.ble.DeviceUpgradeManager;
import com.communication.data.CLog;
import com.communication.data.DeviceUpgradeCallback;
import com.xiaomi.hm.bleservice.profile.MiLiProfile;

public class OtherHandleActivity extends Activity {
	private ProgressDialog dialog;
	private DeviceUpgradeManager mUpgradeManager;
	private Handler mHandler;
	private Runnable mRunnable;
	private Runnable timeOutRunnable;
	private Runnable bluetoothCheckRunnable;
	private static final long BLUETOOTH_CHECK_TIME = 2000L;
	private static final long TIME_OUT = 10000L;
	private static final String SAMSUNG_NAME = "samsung";
	private String phone_name;
	private BluetoothAdapter mBluetoothAdapter;
	@SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			// 88:0F:10:0F:92:FC Keeper.readBraceletBtInfo().address)
			if (device.getAddress().equals(Keeper.readBraceletBtInfo().address)) {
				// mdevice = device;
				CLog.i("info", "������֮ǰ���豸:" + device.getAddress());
				scanLeDevice(false);
				mHandler.removeCallbacks(timeOutRunnable);
				mHandler.removeCallbacks(mRunnable);
				mUpgradeManager.setmDevice(device);
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						CLog.i("info", "1.5�����UI�߳̽�������");
						runOnUiThread(mUpgradeManager);
					}
				}, 1500L);
			}
			CLog.i("info", "���������豸:" + device.getAddress());

		}
	};
	private MyBluetoothReceiver receiver;

	private class MyBluetoothReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
					mHandler.removeCallbacks(bluetoothCheckRunnable);
					mHandler.postDelayed(mRunnable, 1500L);// �ӳ�ɨ��
				} else if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
					mBluetoothAdapter.enable();
					if (!SAMSUNG_NAME.equalsIgnoreCase(phone_name)) {
						// �������ֻ������Զ�����ʱ�Ĵ�����Ҫ��ʱ���
						mHandler.removeCallbacks(bluetoothCheckRunnable);
						mHandler.postDelayed(bluetoothCheckRunnable,
								BLUETOOTH_CHECK_TIME);
					}
				}
			}

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_handle);

		initHintDialog();
		initmUpgradeManager();
		initReceiver();
		// �ȶ��Ƿ�ˢ���ɹ������ж�Ȼ����ת
		if (!MyXiaomiApp.isUpgradeCodMi) {
			// û��ˢ���ɹ�����ת��ʧ��ҳ��
			startActivity(new Intent(this, NoSupportVersionActivity.class));
			this.finish();
			return;
		}
		MiLiProfile localMiLiProfile = (MiLiProfile) BraceletApp.BLEService
				.getDefaultPeripheral();
		if (localMiLiProfile != null) {
			String firmwareVersion = localMiLiProfile.getCachedDeviceInfo()
					.getFirmwareVersionStr();
			String versionCode = firmwareVersion.replaceAll("\\.", "");
			int version = Integer.parseInt(versionCode);
			CLog.i("info", "version:" + version + ";isUpgradeCodMi"
					+ MyXiaomiApp.isUpgradeCodMi);
			if (version >= 1043) {
				// û��ˢ���ɹ�����ת��ʧ��ҳ��
				startActivity(new Intent(this, NoSupportVersionActivity.class));
				this.finish();
				return;
			}
		} else {
			CLog.i("info", "û�ж�ȡ���豸��Ϣ");
		}
		// MyXiaomiApp.getInstance().setProductId("265d91a42f16f8ad7b1892b2");
		String phoneInfo = "BOARD: " + android.os.Build.BOARD;
		phoneInfo += ", MANUFACTURER: " + android.os.Build.MANUFACTURER;
		phoneInfo += ", MODEL: " + android.os.Build.MODEL;
		CLog.i("info", "$$$$$$$$$$ �������� $$$$$$$$$+\n" + phoneInfo);
		phone_name = android.os.Build.MANUFACTURER;

		initBle();

	}

	private void initReceiver() {
		receiver = new MyBluetoothReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(receiver, filter);

	}

	private void initBle() {
		mBluetoothAdapter = MyXiaomiApp.getInstance().getmBluetoothAdapter();
		// BluetoothManager manager = (BluetoothManager) this
		// .getSystemService(Context.BLUETOOTH_SERVICE);
		// mBluetoothAdapter = manager.getAdapter();
		if (mBluetoothAdapter.isEnabled()) {
			CLog.i("info", "��ǰ��������,������");
			mBluetoothAdapter.disable();
		}
	}

	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			CLog.i("info", "��ʼ����");
			mBluetoothAdapter.startLeScan(mLeCallback);
			// ������ť���ɼ�
			mHandler.removeCallbacks(timeOutRunnable);
			mHandler.postDelayed(timeOutRunnable, TIME_OUT);
		} else {
			CLog.i("info", "ֹͣ����");
			mBluetoothAdapter.stopLeScan(mLeCallback);
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	private void initmUpgradeManager() {
		this.mUpgradeManager = new DeviceUpgradeManager(this,
				new DeviceUpgradeCallback() {

					@Override
					public void onWriteFrame(int frame, int total) {
					}

					@Override
					public void onTimeOut() {
						showTimeOutDialog();
					}

					@Override
					public void onGetBootVersion(String version) {
					}

					@Override
					public void onConnectBootSuccess() {
					}

					@Override
					public void onCheckBootResult(boolean isSuccess) {
					}

					@Override
					public void onChangeToBootMode() {
					}

					@Override
					public void onBindDivce(boolean isBind) {
						CLog.i("info", "OtherActivity onBindDivce(true)");
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								dialog.cancel();
								scanLeDevice(false);
								mUpgradeManager.stop();
								// hintDialog.show();
								// ��� ��ת
								startNextActivity();
							}
						});

					}
				});
		mHandler = new Handler();
		mRunnable = new Runnable() {

			@Override
			public void run() {
				scanLeDevice(true);
				dialog.show();
			}
		};
		timeOutRunnable = new Runnable() {

			@Override
			public void run() {
				if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
					// û�д�
					mBluetoothAdapter.enable();
				}
				CLog.i("info", "ֹͣɨ��");
				scanLeDevice(false);
				showTimeOutDialog();
			}
		};
		bluetoothCheckRunnable = new Runnable() {

			@Override
			public void run() {
				if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
					mBluetoothAdapter.enable();
					mHandler.removeCallbacks(bluetoothCheckRunnable);
					mHandler.postDelayed(bluetoothCheckRunnable,
							BLUETOOTH_CHECK_TIME);
				}

			}
		};
	}

	protected void startNextActivity() {
		if (isInstalledCodoon()) {
			startActivity(new Intent(OtherHandleActivity.this,
					UpgradeSuccessActivity.class));
			overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
			finish();
		} else {
			startActivity(new Intent(OtherHandleActivity.this,
					UpgradSuccessDownApp.class));
			overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
			finish();
		}
	}

	/**
	 * �Ƿ�װ�˹����˶�
	 * 
	 * @return
	 */
	private boolean isInstalledCodoon() {
		try {
			getPackageManager().getApplicationInfo("com.codoon.gps",
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	protected void showTimeOutDialog() {
		dialog.cancel();
		new AlertDialog.Builder(OtherHandleActivity.this).setTitle("��ܰ��ʾ")
				.setMessage("û���ҵ��������ֻ�,�뽫�����ֻ������ֻ�").setCancelable(false)
				.setPositiveButton("�õ�", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dia, int which) {
						scanLeDevice(true);
						dialog.show();
					}
				}).show();

	}

	private void initHintDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage("����׼��д�빾��ID,�����ж�...");

	}

	@Override
	protected void onDestroy() {
		if (dialog != null) {
			dialog = null;
		}
		// if (hintDialog != null) {
		// hintDialog = null;
		// }
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		mUpgradeManager.stop();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/**
		 * ���÷��ؼ�
		 */
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void backnextView(View v) {
		// onBackPressed();
		Toast.makeText(this, getResources().getString(R.string.set_initialize),
				Toast.LENGTH_SHORT).show();
	}

}
