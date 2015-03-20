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
		dialog.setMessage("������ȡ���°汾��...");
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog2, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					// ���·��ؼ�
					if (dialog.isShowing()) {
						// �Ի���������ʾ��ȡ����������һ������
						dialog.cancel();
						Toast.makeText(CodoonChoiceActivity.this,
								"δ��ȡ�����°汾��Ϣ�������»�ȡ", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(CodoonChoiceActivity.this, "�����쳣�����Ժ�����",
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
		// ��鵱ǰ�ֻ��Ƿ�֧��ble ����,�����֧���˳�����
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		// ��ʼ�� Bluetooth adapter, ͨ�������������õ�һ���ο�����������(API����������android4.3�����ϺͰ汾)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// ����豸���Ƿ�֧������
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (mBluetoothAdapter.isEnabled()) {
			// ������,������һ��
			CLog.i("info", "��ǰ��������,������");
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
			Toast.makeText(CodoonChoiceActivity.this, "�������ڴ���,���Ժ�",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
			mBluetoothAdapter.enable();
			return;
		}
		if (!Utils.isNetworkConnected(CodoonChoiceActivity.this)) {
			// ��δ����,����������;
			openNetConnectView();
		} else {
			// ������,��ȡ�汾��Ϣ;
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
					CLog.i("info", "�쳣��Ϣ��" + e.getMessage());
					e.printStackTrace();
				}
				Message.obtain(mHandler, READVERSION, version_name)
						.sendToTarget();
			};
		}.start();

	}

	private void openNetConnectView() {
		new AlertDialog.Builder(CodoonChoiceActivity.this).setTitle("�ף�û������")
				.setMessage("�Ƿ�����磿")
				.setNegativeButton("��", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						NetUtil.openWifiOrGPRS();
					}
				}).setPositiveButton("ȡ��", null).show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dialog.dismiss();
	}
}
