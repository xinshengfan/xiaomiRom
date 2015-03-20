package com.codoon.xiaomiupdata.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.smartdevices.bracelet.Utils;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.codoon.xiaomiupdata.netUtils.NetUtil;
import com.communication.data.CLog;

public class CodoonChoiceActivity extends MyBaseActivity {
	private BluetoothAdapter mBluetoothAdapter;
	// private static final int bluerequestCode = 2;
	private ProgressDialog dialog;
	private Handler mHandler;
	private static final int READVERSION = 3;
	private TextView tv_new_function;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_codoon_choice);
		iniView();
		initBle();
	}

	@SuppressLint("HandlerLeak")
	private void iniView() {
		tv_new_function = (TextView) findViewById(R.id.tv_handle_change_choice);
		tv_new_function.setText(Html
				.fromHtml("<u>"
						+ getResources().getString(R.string.ui_change_upgrade)
						+ "</u>"));
		dialog = new ProgressDialog(this);
		// dialog.setCancelable(false);
		dialog.setMessage("联网获取最新版本号...");
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog2, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					// 按下返回键
					if (dialog.isShowing()) {
						// 对话框正在显示，取消并返回上一个界面
						dialog.cancel();
						Toast.makeText(CodoonChoiceActivity.this,
								"未获取到最新版本信息，请重新获取", Toast.LENGTH_SHORT).show();
						CodoonChoiceActivity.this.finish();
						overridePendingTransition(R.anim.push_right_in,
								R.anim.push_right_out);
						return true;
					}
				}
				return false;
			}
		});
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				dialog.cancel();
				if (MyXiaomiApp.getInstance().getCurrentInfo() == null
						|| TextUtils.isEmpty(MyXiaomiApp.getInstance()
								.getCurrentInfo().version_name)) {
					Toast.makeText(CodoonChoiceActivity.this, "网络异常，请稍候再试",
							Toast.LENGTH_SHORT).show();
				} else {
					startActivity(new Intent(CodoonChoiceActivity.this,
							SearchBraceleting.class));
					finish();
				}
			}
		};
		addListener();
	}

	private void addListener() {
		tv_new_function.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CodoonChoiceActivity.this,
						NewFunctionActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
			}
		});

	}

	@SuppressLint("NewApi")
	private void initBle() {
		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		// 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// 检查设备上是否支持蓝牙
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (mBluetoothAdapter.isEnabled()) {
			// 蓝牙打开,则重启一下
			CLog.i("info", "当前蓝牙可用,则重启");
			mBluetoothAdapter.disable();
		}
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mBluetoothAdapter.enable();
			}
		}, 100);
		MyXiaomiApp.getInstance().setmBluetoothAdapter(mBluetoothAdapter);
	}

	public void backnextView(View v) {
		CodoonChoiceActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	public void upgradeCodoon(View v) {
		if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON) {
			Toast.makeText(CodoonChoiceActivity.this, "蓝牙正在打开中,请稍侯",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
			mBluetoothAdapter.enable();
			return;
		}
		if (!Utils.isNetworkConnected(CodoonChoiceActivity.this)) {
			// 还未联网,打开联网界面;
			openNetConnectView();
		} else {
			// 已联网,获取版本信息;
			readVersionForNet();
			dialog.show();
		}

		// finish();
	}

	private void readVersionForNet() {
		new Thread() {

			public void run() {
				String version_name = "";
				try {
					version_name = NetUtil
							.getVersion(CodoonChoiceActivity.this);
				} catch (Exception e) {
					CLog.i("info", "异常信息：" + e.getMessage());
					e.printStackTrace();
				}
				Message.obtain(mHandler, READVERSION, version_name)
						.sendToTarget();
			};
		}.start();

	}

	private void openNetConnectView() {
		new AlertDialog.Builder(CodoonChoiceActivity.this).setTitle("亲，没网啊？")
				.setMessage("是否打开网络？")
				.setNegativeButton("打开", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						NetUtil.openWifiOrGPRS();
					}
				}).setPositiveButton("取消", null).show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dialog.dismiss();
	}
}
