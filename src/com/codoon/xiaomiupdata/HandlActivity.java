package com.codoon.xiaomiupdata;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.smartdevices.bracelet.Keeper;
import cn.com.smartdevices.bracelet.Utils;
import cn.com.smartdevices.bracelet.model.LoginData;
import cn.com.smartdevices.bracelet.model.MiliConfig;
import cn.com.smartdevices.bracelet.model.PersonInfo;
import cn.com.smartdevices.bracelet.ui.SearchSingleBraceletActivity;

import com.codoon.xiaomiupdata.netUtils.NetUtil;
import com.codoon.xiaomiupdata.ui.CustomProgressDialog;
import com.codoon.xiaomiupdata.ui.NewFunctionActivity;
import com.codoon.xiaomiupdata.ui.SearchBraceleting;
import com.communication.data.CLog;

public class HandlActivity extends Activity {
	public final static String KEY_ROM_NAME = "key_codoon_rom";
	private LoginData loginData;
	private PersonInfo personInfo;
	private Button btUpgradeXiaomi, btXiaomiBack;
	private String id;
	private BluetoothAdapter mBluetoothAdapter;
	private Handler mHandler;
	private CustomProgressDialog dialog;
	private TextView tv_new_function;

	// private static final int bluerequestCode = 2;
	private static final int READ_ID = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_handle);
		initData();
		initView();
		initBle();
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
			// openBluetooth();
		}
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mBluetoothAdapter.enable();
			}
		}, 100);
		MyXiaomiApp.getInstance().setmBluetoothAdapter(mBluetoothAdapter);
	}

	@SuppressLint("HandlerLeak")
	private void initView() {
		btUpgradeXiaomi = (Button) findViewById(R.id.bt_updata_xiaomi);
		btXiaomiBack = (Button) findViewById(R.id.bt_updata_xiaomiBack);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				dialog.cancel();
				if (TextUtils.isEmpty(MyXiaomiApp.getInstance().getProductId())) {
					// û�л�ȡ����ȷ��id;
					Toast.makeText(HandlActivity.this, "��ȡ��ƷIDʧ��,���Ժ�����!",
							Toast.LENGTH_SHORT).show();
				} else {
					tryConnent();
				}
			}
		};
		dialog = new CustomProgressDialog(this);
		dialog.setCancelable(false);
		tv_new_function = (TextView) findViewById(R.id.tv_handle_change);
		tv_new_function.setText(Html
				.fromHtml("<u>"
						+ getResources().getString(R.string.ui_change_upgrade)
						+ "</u>"));
		addListener();

	}

	private void addListener() {
		btUpgradeXiaomi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON) {
					Toast.makeText(HandlActivity.this, "�������ڴ���,���Ժ�",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
					mBluetoothAdapter.enable();
					return;
				}
				showDisclaimerDialog();

			}
		});
		btXiaomiBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON) {
					Toast.makeText(HandlActivity.this, "�������ڴ���,���Ժ�",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
					mBluetoothAdapter.enable();
					return;
				}
				Intent intent = new Intent(HandlActivity.this,
						SearchBraceleting.class);
				HandlActivity.this.startActivity(intent);
				HandlActivity.this.finish();
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
			}
		});
		tv_new_function.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HandlActivity.this,
						NewFunctionActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in,
						R.anim.push_left_out);
			}
		});
	}

	/**
	 * ��������Ի���
	 */
	protected void showDisclaimerDialog() {
		new AlertDialog.Builder(HandlActivity.this).setTitle("ע��")
				.setMessage(getResources().getString(R.string.ui_disclaimer))
				.setPositiveButton("ͬ��", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog2, int which) {
						if (!Utils.isNetworkConnected(HandlActivity.this)) {
							// ��δ����,����������;
							openNetConnectView();
						} else {
							// ������,��ȡID;
							dialog.show();
							readProductId();
						}
					}
				}).setNegativeButton("ȡ��", null).show();

	}

	public void readProductId() {
		new Thread() {
			public void run() {
				id = NetUtil.getID(HandlActivity.this);
				MyXiaomiApp.getInstance().setProductId(id);
				mHandler.sendEmptyMessage(READ_ID);
			};
		}.start();
	}

	protected void openNetConnectView() {
		new AlertDialog.Builder(HandlActivity.this).setTitle("�ף�û������")
				.setMessage("�Ƿ�����磿")
				.setNegativeButton("��", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						NetUtil.openWifiOrGPRS();
					}
				}).setPositiveButton("ȡ��", null).show();

	}

	private void initData() {
		loginData = new LoginData();
		loginData.uid = 248231073L;
		loginData.security = "c9a9cde0782e1c50e1e979a470c0af43";
		Keeper.keepLoginData(248231073L, "c9a9cde0782e1c50e1e979a470c0af43");
		initpersonData();
		Keeper.keepNeedBind(1);
	}

	private void initpersonData() {
		personInfo = new PersonInfo();
		/**
		 * {"totalSportData":{"averageSteps":"","calories":"","distance":"",
		 * "totalwearingdays":"","steps":"","points":"","iPoints":0,"iSteps":0,
		 * "iTotalwearingdays" :0,"iDistance":0,"iCalories":0,"iAverageSteps":0}
		 * */
		/**
		 * ,"alarmClockItems"
		 * :[{"calendar":{"year":2014,"month":7,"dayOfMonth":25,"hourOfDay":8,
		 * "minute":0,"second":36},"enabled":false,"isUpdate":false,"mDays":31,
		 * "mSmartWakeupDuration"
		 * :0},{"calendar":{"year":2014,"month":7,"dayOfMonth"
		 * :25,"hourOfDay":8,"minute"
		 * :0,"second":36},"enabled":false,"isUpdate":false
		 * ,"mDays":31,"mSmartWakeupDuration"
		 * :0},{"calendar":{"year":2014,"month"
		 * :7,"dayOfMonth":25,"hourOfDay":8,"minute"
		 * :0,"second":36},"enabled":false
		 * ,"isUpdate":false,"mDays":31,"mSmartWakeupDuration"
		 * :0}],"avatarPath":""
		 * ,"avatarUrl":"","birthday":"","createTime":"","deviceId"
		 * :"880028D500000249"
		 * ,"source":"","sh":"","pinyin":"#","lastLoginTime":"" ,
		 */
		/**
		 * "location":{"city"
		 * :"","latitude":"","longitude":""},"miliConfig":{"wearHand"
		 * :"LEFT_HAND"
		 * ,"lightColor":"BLUE","inComingCallNotifyTime":20,"goalStepsCount"
		 * :8000}
		 */
		MiliConfig config = new MiliConfig();
		config.wearHand = "LEFT_HAND";
		config.inComingCallNotifyTime = 20;
		config.goalStepsCount = 8000;
		config.lightColor = "BLUE";
		personInfo.miliConfig = config;
		/**
		 * ,"personSignature":"","nickname":"����","needSyncServer":0,"uid":
		 * 248231073
		 * ,"gid":-1,"gender":1,"state":0,"age":20,"height":170,"weight":60}
		 */
		personInfo.nickname = "����";
		personInfo.uid = 248231073L;
		personInfo.age = 20;
		personInfo.height = 170;
		personInfo.weight = 60;
		personInfo.gender = 1;
		personInfo.state = 0;
		personInfo.gid = -1;

		Keeper.keepPersonInfo(personInfo);
	}

	private void tryConnent() {
		Intent intent = new Intent(this, SearchSingleBraceletActivity.class);
		startActivity(intent);
		MyXiaomiApp.getInstance().setCurrentModel(
				MyXiaomiApp.UPGRADE_XIAOMI_CODOON);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		HandlActivity.this.finish();

	}

	/**
	 * ˼·��:��дFwUpgradeActivity�е�finishAnimation()����,��ִ�к���رղ���������ת��ָ���Ľ���
	 */
	// public void finishAnimation() {
	// startActivity(new Intent(this, OtherHandleActivity.class));
	// MyXiaomiApp.getInstance().finished();
	// finish();
	//
	// }

	public void backnextView(View v) {
		onBackPressed();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	/**
	 * ���ڷ�����ʵ����еĽ���ص�����;���Իص������еķ����½��д���;
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

}
