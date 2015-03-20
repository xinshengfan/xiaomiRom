package com.communication.ui;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codoon.xiaomiupdata.HandlActivity;
import com.codoon.xiaomiupdata.OtherHandleActivity;
import com.codoon.xiaomiupdata.R;
import com.communication.ble.CodoonDeviceUpgradeManager;
import com.communication.data.DeviceUpgradeCallback;
import com.communication.data.FileManager;
import com.communication.ui.entity.Constants;
import com.communication.ui.entity.MyBluetooth;
import com.communication.ui.parser.LeDeviceListAdapter;

public class DeviceScanActivity extends Activity {

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private ListView mListView;
	private static final long SEARCH_TIME = 4000L;// 搜索时间限定
	private Handler handler;
	private Runnable runnable;
	private Runnable mRunnable;
	private static final int REQUEST_ENABLE_BT = 1;
	private AlertDialog.Builder dialog;
	// 升级部分
	private CodoonDeviceUpgradeManager upgradeManager;
	// private Thread workThread;
	private ProgressDialog progressDialog;
	private BluetoothDevice device;
	private FrameLayout myFrameLayout;
	private ProgressBar myprProgressBar;
	private ImageButton imtPlay;
	private boolean isPlaying;
	private LinearLayout linearLayout;
	private boolean isClick;
	private TextView tvProgress;
	private AlertDialog.Builder onTimedialog;
	private AlertDialog.Builder onSuccessdialog;
	public static final String apk_URL = "http://slbpkg.codoon.com/app/android/codoonsports.apk";

	private String rom_Name;

	@SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				public void run() {
					MyBluetooth bluetooth = new MyBluetooth(device, rssi);
					Log.i("info", "LescanCallback 地址："
							+ bluetooth.getDevice().getAddress() + "name :"
							+ bluetooth.getDevice().getName());
					if (!mLeDeviceListAdapter
							.isContainDeviceByAddress(bluetooth)) {
						mLeDeviceListAdapter.addDevice(bluetooth);
					}
				}
			});
		}
	};

	// private BroadcastReceiver classicScanReceiver;

	/**
	 * 经典扫描方式，当不能识别名字时使用
	 */
	// private void startClassicScan() {
	// Log.i("info", "步骤4 startClassicScan 并发送广播");
	// classicScanReceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String actionStr = intent.getAction();
	// if (BluetoothDevice.ACTION_FOUND.equalsIgnoreCase(actionStr)) {
	// // 已经找到设备；从广播中取出搜索到的蓝牙设备对象
	// BluetoothDevice device = intent
	// .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	// if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
	// // 是BLE设备
	// // 添加到请求扫描设备列表
	// MyBluetooth bluetooth = new MyBluetooth(device,
	// intent.getIntExtra(BluetoothDevice.EXTRA_RSSI,
	// -1));
	// // Log.i("info", "经典 地址："
	// // + bluetooth.getDevice().getAddress() + "name :"
	// // + bluetooth.getDevice().getName());
	// for (int i = 0; i < mLeDeviceListAdapter.getCount(); i++) {
	// MyBluetooth mybluetooth = mLeDeviceListAdapter
	// .getItem(i);
	// if ("unknown".equalsIgnoreCase(mybluetooth
	// .getDevice().getName())) {
	// mLeDeviceListAdapter.removeDevice(i);
	// }
	// }
	// if (!mLeDeviceListAdapter
	// .isContainDeviceByAddress(bluetooth)) {
	// mLeDeviceListAdapter.addDevice(bluetooth);
	// }
	// }
	// } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
	// .equalsIgnoreCase(actionStr)) {
	// // 接收到蓝适配器完成时发送的广播
	// // 开始扫描
	// mBluetoothAdapter.startDiscovery();
	// }
	//
	// }
	//
	// };
	// // 注册广播
	// IntentFilter filter = new IntentFilter();
	// filter.addAction(BluetoothDevice.ACTION_FOUND);
	// filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	// registerReceiver(classicScanReceiver, filter);
	// mBluetoothAdapter.startDiscovery();
	// }

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getActionBar().setTitle(R.string.title_scan);
		setContentView(R.layout.activity_device_scan);

		Intent intent = new Intent(this, MusicPlayService.class);
		startService(intent);

		initData();
		initUpgradeManager();
		initBle();
		initView();
		initVariable();
		scanLeDevice(true);
		// startClassicScan();
	}

	private void initData() {
		rom_Name = getIntent().getStringExtra(HandlActivity.KEY_ROM_NAME);
		Log.i("info", "要升级的固件名:" + rom_Name);
	}

	/**
	 * 搜索设备
	 * 
	 * @param v
	 */
	public void researchDevice(View v) {
		if (isClick) {
			scanLeDevice(true);
			// startClassicScan();
		}
	}

	private void initUpgradeManager() {
		this.upgradeManager = new CodoonDeviceUpgradeManager(this,
				new DeviceUpgradeCallback() {

					@Override
					public void onWriteFrame(final int frame, final int total) {
						Log.i("info", "+++++++++++++onWriteFrame(): frame:"
								+ frame + " total:" + total);
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (progressDialog.isShowing()) {
									progressDialog.cancel();
								}
								Log.i("info",
										"myFrameLayout.getVisibility() == View.GONE");
								if (myFrameLayout.getVisibility() == View.GONE) {
									myFrameLayout.setVisibility(View.VISIBLE);
									isClick = false;
									isPlaying = true;
									imtPlay.setBackgroundResource(R.drawable.ic_stop_press);
									Log.i("info", "播放音乐");
									Intent intent = new Intent(G.ACTION_PLAY);
									sendBroadcast(intent);
								}
								int progres = (100 * frame) / total;
								tvProgress.setText(progres + "%");
							}
						});
						// 升级过程
						myprProgressBar.setMax(total);
						myprProgressBar.setProgress(frame);

					}

					@Override
					public void onTimeOut() {
						Log.i("info", "+++++++++++++onTimeOut()");
						// 连接超时
						// 停止音乐
						Intent intent = new Intent(G.ACTION_PAUSE);
						sendBroadcast(intent);
						if (null == onTimedialog) {
							onTimedialog = new Builder(DeviceScanActivity.this);
						}
						onTimedialog.setTitle("温馨提示");
						onTimedialog.setMessage("  连接超时，请重新连接");
						onTimedialog.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// upgradeManager.start(device);
										// dialog.cancel();
										isClick = true;
										progressDialog.cancel();
										myFrameLayout.setVisibility(View.GONE);
										mLeDeviceListAdapter.clearDeviceList();
										upgradeManager.stop();
										// upgradeManager.registeBleManager();
										initVariable();
										initUpgradeManager();
										// scanLeDevice(false);
										// upgradeManager.stop();

									}
								});
						onTimedialog.show();

					}

					@Override
					public void onGetBootVersion(String onGetBootVersion) {
						Log.i("info",
								"+++++++++++++onGetBootVersion(): onGetBootVersion:"
										+ onGetBootVersion);
						// 得到当前设备版本
						Toast.makeText(DeviceScanActivity.this,
								"当前版本：" + onGetBootVersion, Toast.LENGTH_LONG)
								.show();

					}

					@Override
					public void onConnectBootSuccess() {
						Log.i("info", "+++++++++++++onConnectBootSuccess()");
						// 成功连上设

					}

					@Override
					public void onCheckBootResult(final boolean isSuccess) {
						Log.i("info",
								"+++++++++++++onCheckBootResult(): isSuccess:"
										+ isSuccess);
						// 检查手环升级是否成功，若成功返回true
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (isSuccess) {
									// 停止音乐
									Intent intent = new Intent(G.ACTION_PAUSE);
									isPlaying = false;
									imtPlay.setBackgroundResource(R.drawable.ic_play_press);
									sendBroadcast(intent);
									// 断开连接
									scanLeDevice(false);
									upgradeManager.stop();
									// progressDialog.cancel();
									onSuccessdialog.setTitle("温馨提示");
									onSuccessdialog
											.setMessage("   刷机成功，可正常使用手环,赶快去体验一下吧!");
									onSuccessdialog
											.setPositiveButton(
													"去看看",
													new DialogInterface.OnClickListener() {

														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															ComponentName componentName = new ComponentName(
																	"com.codoon.gps",
																	"com.codoon.gps.ui.WelcomeActivity");
															try {
																Intent intent = new Intent();
																intent.setComponent(componentName);
																startActivity(intent);
															} catch (Exception e) {
																e.printStackTrace();
																Toast.makeText(
																		DeviceScanActivity.this,
																		"你手机上还未安装咕咚运动",
																		Toast.LENGTH_SHORT)
																		.show();
																Intent intent = new Intent(
																		Intent.ACTION_VIEW,
																		Uri.parse(apk_URL));
																startActivity(intent);
															}
															DeviceScanActivity.this
																	.finish();
														}
													});
									onSuccessdialog.show();
								}

							}
						});

					}

					@Override
					public void onChangeToBootMode() {
						Log.i("info", "onChangeToBootMode()");

					}

					@Override
					public void onBindDivce(boolean isBind) {

					}
				});
		upgradeManager.setUpgradeFilePath(new FileManager().getOtherPath(this)
				+ rom_Name);
	}

	private void initVariable() {
		handler = new Handler();
		runnable = new Runnable() {

			@Override
			public void run() {
				// 停止扫描
				scanLeDevice(false);
				progressDialog.cancel();
				// mBluetoothAdapter.cancelDiscovery();
				// 重新搜索可见
				// 连接跳转
				if (mLeDeviceListAdapter != null
						&& mLeDeviceListAdapter.getCount() > 0) {
					for (int i = 0; i < mLeDeviceListAdapter.getCount(); i++) {
						MyBluetooth bluetooth = mLeDeviceListAdapter.getItem(i);

						if (Constants.TYPE_COD_MI_NAME
								.equalsIgnoreCase((bluetooth.getDevice()
										.getName()))) {
							// 弹出对话框选择
							dialog.setMessage("经检测，地址为"
									+ bluetooth.getDevice().getAddress()
									+ "的设备可以刷机");
							// progressDialog.setMessage("正在为您拼命修复...");
							showDialogchoice(bluetooth.getDevice());
							device = bluetooth.getDevice();
							break;
						}
					}

				} else {
					dialog.setTitle("温馨提示");
					dialog.setMessage("  没有找到您的手环,请确认您的手环是否开启并在附近?");
					dialog.setNegativeButton("返回", null).setPositiveButton(
							"是的", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									scanLeDevice(true);
								}

							});
					dialog.show();
				}

			}
		};
		mRunnable = new Runnable() {

			@Override
			public void run() {
				Log.i("info", "延迟一秒在主线程中执行");
				upgradeManager.start(device);
				scanLeDevice(false);
			}
		};

	}

	protected void showDialogchoice(final BluetoothDevice bluetoothDevice) {
		dialog.setTitle("注意：");
		dialog.setNegativeButton("放弃", null).setPositiveButton("好的",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 修复
						Log.i("info", "+++++开始修复++++++");
						scanLeDevice(false);
						// mBluetoothAdapter.cancelDiscovery();
						device = bluetoothDevice;
						handler.removeCallbacks(runnable);
						handler.postDelayed(mRunnable, 1);
						progressDialog.setMessage("正在拼命连接中...");
						progressDialog.show();
					}
				});
		dialog.show();

	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.device_listview);
		mLeDeviceListAdapter = new LeDeviceListAdapter(this);
		mListView.setAdapter(mLeDeviceListAdapter);
		mListView.setFocusable(true);
		dialog = new Builder(this);
		onTimedialog = new Builder(this);
		onSuccessdialog = new Builder(this);
		dialog.setCancelable(false);
		progressDialog = new ProgressDialog(this);
		// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle("亲，请稍侯");
		progressDialog.setIcon(R.drawable.icon_log);
		progressDialog.setCancelable(false);
		myprProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		File file = new File(new FileManager().getOtherPath(this) + rom_Name);
		if (file.exists()) {
			int totalFrame = (int) (file.length() / 12);
			myprProgressBar.setMax(totalFrame);
			// progressDialog.setMax(totalFrame);
			myprProgressBar.setProgress(totalFrame / 2);
		}
		// progressDialog.setCancelable(true);// 打包设为false
		myFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_upgrade);
		// myFrameLayout.setVisibility(View.VISIBLE);
		imtPlay = (ImageButton) findViewById(R.id.imt_list_play);
		linearLayout = (LinearLayout) findViewById(R.id.linear_device);
		isClick = true;
		tvProgress = (TextView) findViewById(R.id.tv_progress);
		addListener();

	}

	private void addListener() {
		Log.i("info", "添加条目事件");

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (isClick) {
					if (position >= 0
							&& position < mLeDeviceListAdapter.getCount()) {
						scanLeDevice(false);
						// mBluetoothAdapter.cancelDiscovery();
						handler.removeCallbacks(runnable);
						MyBluetooth Mydevice = mLeDeviceListAdapter
								.getItem(position);
						dialog.setMessage("请先连接你的设备？");

						bindDevice(Mydevice.getDevice());
						device = Mydevice.getDevice();
					}
				}
			}
		});

		// imtPlay.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Log.i("info", "play music");
		// Intent intent = new Intent();
		// if (isPlaying) {
		// intent.setAction(G.ACTION_PAUSE);
		// isPlaying = false;
		// imtPlay.setBackgroundResource(R.drawable.ic_play_press);
		// } else {
		// intent.setAction(G.ACTION_PLAY);
		// isPlaying = true;
		// imtPlay.setBackgroundResource(R.drawable.ic_stop_press);
		// }
		// sendBroadcast(intent);
		// }
		// });

	}

	public void playClick(View v) {
		Log.i("info", "play music");
		Intent intent = new Intent();
		if (isPlaying) {
			intent.setAction(G.ACTION_PAUSE);
			isPlaying = false;
			imtPlay.setBackgroundResource(R.drawable.ic_play_press);
		} else {
			intent.setAction(G.ACTION_PLAY);
			isPlaying = true;
			imtPlay.setBackgroundResource(R.drawable.ic_stop_press);
		}
		sendBroadcast(intent);
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

		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivity(enableBtIntent);
			}
		}
	}

	@Override
	protected void onDestroy() {
		upgradeManager.stop();
		if (dialog != null) {
			dialog = null;
		}
		if (progressDialog != null) {
			progressDialog = null;
		}
		Intent intent = new Intent(G.ACTION_EXIT);
		sendBroadcast(intent);
		// if (null != classicScanReceiver) {
		// unregisterReceiver(classicScanReceiver);
		// }
		scanLeDevice(false);
		// upgradeManager.stop();
		// mBluetoothAdapter.cancelDiscovery();
		super.onDestroy();
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setVisible(false)
					.setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// scanLeDevice(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (!isClick) {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			mLeDeviceListAdapter.clearDeviceList();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		// scanLeDevice(false);
		// mLeDeviceListAdapter.clearDeviceList();
	}

	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			mScanning = true;
			mLeDeviceListAdapter.clearDeviceList();
			progressDialog.setTitle("亲,请稍等");
			progressDialog.setMessage("正在努力寻找您的手环...");
			progressDialog.show();
			// Log.i("info", "开始搜索");
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			handler.postDelayed(runnable, SEARCH_TIME);
			// 搜索按钮不可见
		} else {
			Log.i("info", "停止扫描");
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			// if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
			// }
			// upgradeManager.stop();
			// 搜索按钮可见
		}
		invalidateOptionsMenu();
	}

	private void bindDevice(final BluetoothDevice device2) {
		dialog.setTitle("注意：");
		dialog.setNegativeButton("算了", null).setPositiveButton("好的",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 修复
						Log.i("info", "+++++开始绑定++++++");
						scanLeDevice(false);
						// mBluetoothAdapter.cancelDiscovery();
						device = device2;
						handler.removeCallbacks(runnable);
						handler.postDelayed(mRunnable, 1);
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.setMessage("正在为你升级设备...");
						progressDialog.show();
					}
				});
		dialog.show();

	}

}
