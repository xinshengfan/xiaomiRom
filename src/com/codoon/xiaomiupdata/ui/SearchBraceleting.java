package com.codoon.xiaomiupdata.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.communication.data.CLog;
import com.communication.ui.entity.Constants;
import com.communication.ui.entity.MyBluetooth;
import com.communication.ui.parser.LeDeviceListAdapter;

public class SearchBraceleting extends MyBaseActivity {
	private ImageView imv;
	private AnimationDrawable searchAnim;
	private int[] images = { R.drawable.seartch_codoon_bleband_1,
			R.drawable.seartch_codoon_bleband_2 };
	private int[] xiaomi_images = { R.drawable.device_searching_xm_0,
			R.drawable.device_searching_xm_1 };

	private BluetoothAdapter mBluetoothAdapter;
	private Handler handler;
	private Runnable runnable;
	private static final long SEARCH_TIME = 4000L;// 搜索时间限定
	private BluetoothDevice device;
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private int currentModel;
	private TextView tv_search_title;

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {
			MyBluetooth bluetooth = new MyBluetooth(device, rssi);
			CLog.i("info", "LescanCallback 地址："
					+ bluetooth.getDevice().getAddress() + "name :"
					+ bluetooth.getDevice().getName());
			if (!mLeDeviceListAdapter.isContainDeviceByAddress(bluetooth)) {
				mLeDeviceListAdapter.addDevice(bluetooth);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_bracelet);
		CLog.i("info", "SearchBraceleting onCreate()");
		initData();
		initView();
		initVariable();
		scanLeDevice(true);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		CLog.i("info", "SearchBraceleting onNewIntent()");
		initData();
		initView();
		initVariable();
		scanLeDevice(true);
	}

	private void initData() {
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();
		// 先默认没有Cboot手环
		MyXiaomiApp.getInstance().setCodoonCboot(false);
	}

	private void initVariable() {
		handler = new Handler();
		runnable = new Runnable() {

			@Override
			public void run() {
				// 停止扫描
				scanLeDevice(false);
				// 连接跳转
				if (mLeDeviceListAdapter != null
						&& mLeDeviceListAdapter.getCount() > 0) {
					if (currentModel == MyXiaomiApp.UPGRADE_BACK_XIAOMI) {
						// 刷回小米
						for (int i = 0; i < mLeDeviceListAdapter.getCount(); i++) {
							MyBluetooth bluetooth = mLeDeviceListAdapter
									.getItem(i);
							if (Constants.TYPE_COD_MI_NAME
									.equalsIgnoreCase((bluetooth.getDevice()
											.getName()))) {
								device = bluetooth.getDevice();
								MyXiaomiApp.getInstance().setCurrentDevice(
										device);
								startActivity(new Intent(
										SearchBraceleting.this,
										BraceletConnectActivity.class));
								overridePendingTransition(R.anim.fw_in,
										R.anim.fw_out);
								finish();
								// break;
								return;
							}
						}
						// 根据名字没有找到设备
						startActivity(new Intent(SearchBraceleting.this,
								SearchDivceListActivity.class));
						MyXiaomiApp.getInstance()
								.setCurrentLeDeviceListAdapter(
										mLeDeviceListAdapter);
						overridePendingTransition(R.anim.zoom_enter,
								R.anim.zoom_exit);
						finish();

					} else if (currentModel == MyXiaomiApp.UPGRADE_CODOON) {
						// 咕咚升级
						// 先找是否有Cboot类型的手环
						for (int i = 0; i < mLeDeviceListAdapter.getCount(); i++) {
							MyBluetooth bluetooth = mLeDeviceListAdapter
									.getItem(i);
							CLog.i("info", "搜索的设备名字："
									+ bluetooth.getDevice().getName());
							if (Constants.TYPE_CBOOT_NAME
									.equalsIgnoreCase((bluetooth.getDevice()
											.getName()))) {
								device = bluetooth.getDevice();
								MyXiaomiApp.getInstance().setCurrentDevice(
										device);
								Toast.makeText(SearchBraceleting.this,
										"你的手环当前已处于Boot模式", Toast.LENGTH_LONG)
										.show();
								MyXiaomiApp.getInstance().setCodoonCboot(true);
								startActivity(new Intent(
										SearchBraceleting.this,
										SearchOverActivity.class));
								overridePendingTransition(R.anim.zoom_enter,
										R.anim.zoom_exit);
								finish();
								// break;
								return;
							}
						}
						// 没有Cboot，找到信号最强的设备
						device = mLeDeviceListAdapter.getMostRssiDevice()
								.getDevice();
						if (Constants.TYPE_CODOON_NAME.equalsIgnoreCase(device
								.getName())) {
							// 信号最强的设备是咕咚，则连接
							MyXiaomiApp.getInstance().setCurrentDevice(device);
							startActivity(new Intent(SearchBraceleting.this,
									BraceletConnectActivity.class));
							overridePendingTransition(R.anim.fw_in,
									R.anim.fw_out);
							finish();
							return;
						}
						/************* 测试使用 ****************/
						startActivity(new Intent(SearchBraceleting.this,
								SearchDivceListActivity.class));
						overridePendingTransition(R.anim.zoom_enter,
								R.anim.zoom_exit);
						MyXiaomiApp.getInstance()
								.setCurrentLeDeviceListAdapter(
										mLeDeviceListAdapter);
						finish();
					}

				} else {
					CLog.i("info", "没有搜索到设备");
					startActivity(new Intent(SearchBraceleting.this,
							SearchNotFoundActivity.class));
					overridePendingTransition(R.anim.zoom_enter,
							R.anim.zoom_exit);
					finish();
				}

			}
		};

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		mLeDeviceListAdapter.clearDeviceList();
		scanLeDevice(true);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// mScanning = true;
			mLeDeviceListAdapter.clearDeviceList();
			// CLog.i("info", "开始搜索");
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, SEARCH_TIME);
			// 搜索按钮不可见
		} else {
			CLog.i("info", "停止扫描");
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			// if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
			// }
			// upgradeManager.stop();
			// 搜索按钮可见
		}
	}

	public void backnextView(View v) {
		scanLeDevice(false);
		searchAnim.stop();
		handler.removeCallbacks(runnable);
		SearchBraceleting.this.finish();
	}

	private void initView() {
		imv = (ImageView) findViewById(R.id.icon_device_discovry);
		tv_search_title = (TextView) findViewById(R.id.tv_search_ing_title);
		searchAnim = new AnimationDrawable();

		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			tv_search_title.setText(R.string.search_bracelet);
			for (int i = 0; i < images.length; i++) {
				Drawable drawable = getResources()
						.getDrawable(xiaomi_images[i]);
				searchAnim.addFrame(drawable, 300);
			}
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
			tv_search_title.setText(R.string.ui_search_codoon_title);
			for (int i = 0; i < images.length; i++) {
				Drawable drawable = getResources().getDrawable(images[i]);
				searchAnim.addFrame(drawable, 300);
			}
			break;
		default:
			break;
		}
		searchAnim.setOneShot(false);
		imv.setBackground(searchAnim);
		searchAnim.start();

		mLeDeviceListAdapter = new LeDeviceListAdapter(this);
		mBluetoothAdapter = MyXiaomiApp.getInstance().getmBluetoothAdapter();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CLog.i("info", "searchBraceleting onDestory()");
		searchAnim.stop();
		// handler.removeCallbacks(runnable);
		scanLeDevice(false);
	}

	@Override
	public void finish() {
		super.finish();
		CLog.i("info", "searchBraceleting finish()");
		searchAnim.stop();
		scanLeDevice(false);
		// handler.removeCallbacks(runnable);
	}
}
