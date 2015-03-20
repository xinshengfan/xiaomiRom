package com.codoon.xiaomiupdata;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.communication.ble.CodoonXiaomiDeviceUpgradeManager;
import com.communication.data.CLog;
import com.communication.data.FileManager;
import com.communication.data.IDeviceUpgradeCallback;
import com.communication.ui.G;

public class UpgradeXiaomiService extends Service {
	private BluetoothDevice currentDevice;
	private CodoonXiaomiDeviceUpgradeManager mUpgradeManager;
	private String rom_name;
	private int currentModel;
	private Handler mHandler;

	// private Runnable mRunnable;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initVariable();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initData();
		mHandler.postDelayed(mUpgradeManager, 100);
		return super.onStartCommand(intent, flags, startId);
	}

	private void initData() {
		currentDevice = MyXiaomiApp.getInstance().getCurrentDevice();
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			rom_name = MyXiaomiApp.XIAOMIFILENAME;
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
			rom_name = MyXiaomiApp.CODOONFILENAME;
			break;
		default:
			break;
		}
		mUpgradeManager.setmDevice(currentDevice);
		mUpgradeManager.setUpgradeFilePath(new FileManager().getOtherPath(this)
				+ rom_name);
	}

	private void initVariable() {
		mUpgradeManager = new CodoonXiaomiDeviceUpgradeManager(this,
				new IDeviceUpgradeCallback() {

					@Override
					public void onTimeOut() {
						Toast.makeText(
								MyXiaomiApp.getInstance()
										.getApplicationContext(), "连接超时",
								Toast.LENGTH_SHORT).show();
						Intent intent_timeout = new Intent(G.ACTION_TIME_OUT);
						sendBroadcast(intent_timeout);

					}
				});
		MyXiaomiApp.getInstance().setmUpgradeManager(mUpgradeManager);
		mHandler = new Handler();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		CLog.i("info", "结束UpgradeXiaomiService");
		mUpgradeManager.stop();
	}
}
