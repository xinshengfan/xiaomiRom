package com.codoon.xiaomiupdata.ui;

import java.io.File;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.smartdevices.bracelet.Utils;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.codoon.xiaomiupdata.UpgradeXiaomiService;
import com.codoon.xiaomiupdata.netUtils.NetUtil;
import com.communication.data.CLog;
import com.communication.data.FileManager;
import com.communication.ui.G;
import com.communication.ui.MusicPlayService;

public class BraceletDetailActivity extends MyBaseActivity {
	private TextView tv_version, tv_content, tv_accessory, tv_network,
			tv_bracelet_charge, tv_phone_charge, tv_hint, tv_progress,
			tv_detail_warning;
	private boolean isBindBracelect;
	private MyReceiver receiver;
	private FrameLayout mFrameLayout;
	private ProgressBar mProgressBar;
	private ImageButton imtPlay;
	private Button bt_upgrade;
	private boolean isPlaying;
	private String rom_Name;
	private int currentModel;
	private int totalFrame;
	private boolean isHadUpgrade;
	private Intent upgradeService;
	private Intent playService;
	private boolean isCodoonCboot;
	private boolean isCanBack;
	private ProgressDialog mProgressDialog;
	private final int LEAST_BRACELECT_CHARGE = 20; // �ֻ���͵���
	private final int LEAST_PHONE_CHARGE = 10; // �ֻ���͵���
	private ProgressDialog dialog;
	private boolean isDownLoadOver;
	private String error_reason = "";
	private boolean isHadDownLoad; // ��־�����߳�
	private Handler mhHandler;

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			CLog.i("inof", "���յĹ㲥 ��" + action);
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				int rawlevel = intent.getIntExtra("level", -1);
				CLog.i("info", "�ֻ�����:" + rawlevel);
				tv_phone_charge.setText(getResources().getString(
						R.string.phone_electric_charge)
						+ rawlevel + "%");
				if (rawlevel < LEAST_PHONE_CHARGE) {
					tv_phone_charge.setTextColor(android.graphics.Color.RED);
					changWarnView();
				}
			} else if (G.ACTION_READ_CHARGE.equals(action)) {
				int charge = intent.getIntExtra(G.KEY_BRACELET_CHARGE, -1);
				tv_bracelet_charge.setText(getResources().getString(
						R.string.bracelet_electric_charge)
						+ charge + "%");
				if (charge < LEAST_BRACELECT_CHARGE) {
					tv_bracelet_charge.setTextColor(android.graphics.Color.RED);
					changWarnView();
				}
			} else if (G.ACTION_FRAME_NUM.equals(action)) {
				int frame_num = intent.getIntExtra(G.KEY_FRAME_NUM, 0);
				showProgressView(frame_num);
			} else if (G.ACTION_TIME_OUT.equals(action)) {
				// ��ʱ����
				if (mProgressDialog.isShowing()) {
					mProgressDialog.cancel();
				}
				if (dialog.isShowing()) {
					dialog.cancel();
				}
				if (isHadUpgrade) {
					tv_content.setText(getResources().getString(
							R.string.timeout_upgrade));
					resetFrameLayoutGone();
				}
				if (MyXiaomiApp.getInstance().isCodoonCboot()) {
					BraceletDetailActivity.this.stopService(upgradeService);
					MyXiaomiApp.getInstance().setCodoonCboot(true);
				}
			} else if (G.ACTION_UPGRADE_SUCCESS.equals(action)) {
				CLog.i("info", "�����ɹ�");
				resetFrameLayoutGone();
				closeService();
				startNextActivity();
			} else if (G.ACTION_DEVICE_VERSION.equals(action)) {
				CLog.i("info", "���յ��汾��Ϣ�㲥");
				if (dialog.isShowing()) {
					dialog.cancel();
				}
				int versionHeight = intent.getIntExtra(G.KEY_VERSION_HEIGHT, 0);
				int versionLow = intent.getIntExtra(G.KEY_VERSION_LOW, 0);
				handVersionTextView(versionHeight, versionLow);
			} else if (G.ACTION_DOWNLOAD_OVER.equals(action)) {
				isDownLoadOver = intent.getBooleanExtra(G.KEY_DOWNLOAD_OVER,
						false);
				error_reason = intent
						.getStringExtra(G.KEY_DOWNLOAD_ERROR_REASON);
				isHadDownLoad = false;
				// if (!TextUtils.isEmpty(error_reason)) {
				// Toast.makeText(BraceletDetailActivity.this, error_reason,
				// Toast.LENGTH_SHORT).show();
				// }
				CLog.i("info", "���յ����ع̼��ļ���ɹ㲥��" + isDownLoadOver);
			} else if (G.ACTION_CHECK_ERROR.equals(action)) {
				// У��ʧ�ܣ�����������
				Toast.makeText(BraceletDetailActivity.this, "����ʧ�ܣ�����������",
						Toast.LENGTH_SHORT).show();
				// resetViewForCboot();
				resetFrameLayoutGone();
			} else if (G.ACTION_HAD_CBOOT.equals(action)) {
				// �Ѿ�����bootģʽ
				BraceletDetailActivity.this.stopService(upgradeService);
				mhHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						MyXiaomiApp.getInstance().setCodoonCboot(true);
						BraceletDetailActivity.this
								.startService(upgradeService);
					}
				}, 1500L);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CLog.i("info", "BraceletDetailActivity onCreate() taskID" + getTaskId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bracelet_detail);
		initData();
		intiView();
		resetFrameLayoutGone();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		CLog.i("info", "BraceletDetailActivity onNewIntent()");
	}

	/**
	 * ���ݰ汾��Ϣ�������
	 * 
	 * @param versionLow
	 * @param versionHeight
	 */
	public void handVersionTextView(int versionHeight, int versionLow) {
		String deviceVersionName = versionHeight + "." + versionLow;
		if (currentModel == MyXiaomiApp.UPGRADE_CODOON) {
			String latestVersionName = MyXiaomiApp.getInstance()
					.getCurrentInfo().version_name;
			float deviceCode = Float.parseFloat(deviceVersionName);
			float latestCode = Float.parseFloat(latestVersionName);
			if (latestCode > deviceCode) {
				// �������ϵİ汾���豸�ϸ���
				tv_version.setText("��ǰ�汾��V" + deviceVersionName + "(��������V"
						+ latestVersionName + ")");
				tv_content
						.setText(MyXiaomiApp.getInstance().getCurrentInfo().description);
				downloadBinFile();
			} else {
				// ���°汾�����ø�����
				tv_version.setText("��ǰ�汾��V" + deviceVersionName + "(�������°汾��)");
				changWarnView();
			}
		} else if (currentModel == MyXiaomiApp.UPGRADE_BACK_XIAOMI) {
			tv_version.setText("��ǰ�汾��V" + deviceVersionName + "(��ˢ��С��ROM)");
		}

	}

	private void downloadBinFile() {
		if (!Utils.isNetworkConnected(BraceletDetailActivity.this)) {
			return;
		}
		// �����߳�����
		isHadDownLoad = true;
		new Thread() {
			public void run() {
				NetUtil.downloadLastBin(BraceletDetailActivity.this,
						MyXiaomiApp.getInstance().getCurrentInfo());
			};
		}.start();
	}

	private void openNetConnectView() {
		new AlertDialog.Builder(BraceletDetailActivity.this).setTitle("�ף�û������")
				.setMessage("�Ƿ�����磿")
				.setNegativeButton("��", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						NetUtil.openWifiOrGPRS();
					}
				}).setPositiveButton("ȡ��", null).show();
	}

	public void changWarnView() {
		tv_hint.setVisibility(View.INVISIBLE);
		bt_upgrade.setBackground(getResources().getDrawable(
				R.drawable.btn_grey_disable));
		bt_upgrade.setEnabled(false);
		tv_detail_warning.setVisibility(View.VISIBLE);

	}

	/**
	 * ����Service
	 */
	public void closeService() {
		CLog.i("info", "��������SerVice");
		BraceletDetailActivity.this.stopService(upgradeService);
		BraceletDetailActivity.this.stopService(playService);
	}

	public void startNextActivity() {
		if (currentModel == MyXiaomiApp.UPGRADE_BACK_XIAOMI) {
			// ˢ��С��
			startActivity(new Intent(BraceletDetailActivity.this,
					BackXiaomiOver.class));
			overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
			BraceletDetailActivity.this.finish();
		} else if (isInstalledCodoon()) {
			startActivity(new Intent(BraceletDetailActivity.this,
					UpgradeSuccessActivity.class));
			overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
			BraceletDetailActivity.this.finish();
		} else {
			startActivity(new Intent(BraceletDetailActivity.this,
					UpgradSuccessDownApp.class));
			overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
			BraceletDetailActivity.this.finish();
		}
	}

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

	public void resetFrameLayoutGone() {
		mFrameLayout.setVisibility(View.GONE);
		isPlaying = false;
		isHadUpgrade = false;
		isCanBack = true;
		imtPlay.setBackgroundResource(R.drawable.ic_play_press);
		Intent pause_intent = new Intent(G.ACTION_PAUSE);
		sendBroadcast(pause_intent);

	}

	public void showProgressView(int frame_num) {
		if (mFrameLayout.getVisibility() == View.GONE) {
			mProgressDialog.cancel();
			mFrameLayout.setVisibility(View.VISIBLE);
			isHadUpgrade = true;
			isCanBack = false; // ���÷��ؼ�
		}
		int progres = (100 * frame_num) / totalFrame;
		if (progres > 100) {
			progres = 100;
		}
		tv_progress.setText(progres + "%");
		mProgressBar.setProgress(frame_num);

	}

	private void initData() {
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			rom_Name = MyXiaomiApp.XIAOMIFILENAME;
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
			rom_Name = MyXiaomiApp.CODOONFILENAME;
			break;
		default:
			break;
		}
		isBindBracelect = getIntent().getBooleanExtra(G.KEY_CONNECT_BRACELECT,
				true);
		isCodoonCboot = MyXiaomiApp.getInstance().isCodoonCboot();
		isDownLoadOver = false;
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(G.ACTION_READ_CHARGE);
		filter.addAction(G.ACTION_FRAME_NUM);
		filter.addAction(G.ACTION_TIME_OUT);
		filter.addAction(G.ACTION_UPGRADE_SUCCESS);
		filter.addAction(G.ACTION_DEVICE_VERSION);
		filter.addAction(G.ACTION_DOWNLOAD_OVER);
		filter.addAction(G.ACTION_CHECK_ERROR);
		filter.addAction(G.ACTION_HAD_CBOOT);
		registerReceiver(receiver, filter);
		upgradeService = new Intent(this, UpgradeXiaomiService.class);
		playService = new Intent(this, MusicPlayService.class);
		mhHandler = new Handler();
	}

	private void resetViewForCboot() {
		tv_version.setText("�����޸�");
		tv_version.setTextColor(android.graphics.Color.RED);
		tv_content.setText(getResources().getString(
				R.string.cboot_detail_content));
		tv_bracelet_charge.setText(getResources().getString(
				R.string.bracelet_electric_charge)
				+ getString(R.string.bracelet_electric_not_reading));
	}

	@Override
	public void finish() {
		super.finish();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	private void intiView() {
		tv_accessory = (TextView) findViewById(R.id.tv_bracelet_detail_accessory);
		tv_version = (TextView) findViewById(R.id.tv_bracelet_detail_version);
		tv_bracelet_charge = (TextView) findViewById(R.id.tv_bracelet_detail_bracelet_electric);
		tv_content = (TextView) findViewById(R.id.tv_bracelet_detail_content);
		tv_network = (TextView) findViewById(R.id.tv_bracelet_detail_network);
		tv_phone_charge = (TextView) findViewById(R.id.tv_bracelet_detail_phone_electric);
		tv_detail_warning = (TextView) findViewById(R.id.tv_detail_warning);
		tv_hint = (TextView) findViewById(R.id.tv_tv_bracelet_detail_hint);
		if (Utils.isNetworkConnected(this)) {
			tv_network.setText(getResources().getString(R.string.network)
					+ getResources().getString(R.string.had_connect));
		} else {
			tv_network.setText(getResources().getString(R.string.network)
					+ getResources().getString(R.string.not_connect));
		}
		if (isBindBracelect) {
			tv_accessory.setText(getResources().getString(R.string.accessory)
					+ getResources().getString(R.string.had_connect));
		} else {
			tv_accessory.setText(getResources().getString(R.string.accessory)
					+ getResources().getString(R.string.not_connect));
		}
		tv_bracelet_charge.setText(getResources().getString(
				R.string.bracelet_electric_charge)
				+ getString(R.string.bracelet_electric_reading));

		mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_detail_upgrade);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1_detail);
		imtPlay = (ImageButton) findViewById(R.id.imt_list_play_detail);
		bt_upgrade = (Button) findViewById(R.id.bt_bracelet_detail_upgrade);
		File file = new File(new FileManager().getOtherPath(this) + rom_Name);
		if (file.exists()) {
			totalFrame = (int) (file.length() / 12);
			mProgressBar.setMax(totalFrame);
		}
		tv_progress = (TextView) findViewById(R.id.tv_progress_detail);
		mProgressDialog = new CustomProgressDialog(this);
		mProgressDialog.setCancelable(false);
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage("���ڶ�ȡ�̼���Ϣ...");
		if (isCodoonCboot) {
			resetViewForCboot();
		} else if (!isBindBracelect) {
			// û�а󶨳ɹ������ӳ�ʱ
			resetViewForTimeOut();
		} else {
			dialog.show();
		}
		addListener();
	}

	private void resetViewForTimeOut() {
		tv_version.setText("���ӳ�ʱ");
		tv_version.setTextColor(android.graphics.Color.RED);
		tv_content.setText("�뽫�ֻ������ֻ�����������");
		tv_bracelet_charge.setText(getResources().getString(
				R.string.bracelet_electric_charge)
				+ getString(R.string.bracelet_electric_not_reading));
		changWarnView();
	}

	private void addListener() {
		mFrameLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}

	/**
	 * ����/��ͣ����
	 * 
	 * @param v
	 */
	public void playClick(View v) {
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

	/**
	 * ˢRom��ť
	 * 
	 * @param v
	 */
	public void upgradeRom(View v) {
		if (isDownLoadOver || (MyXiaomiApp.getInstance().isCodoonCboot())
				|| (currentModel == MyXiaomiApp.UPGRADE_BACK_XIAOMI)) {
			CLog.i("info", "��ʼˢRom");
			isHadDownLoad = false;
			startService(upgradeService);
			startService(playService);
			openDialog();
		} else {
			if (!Utils.isNetworkConnected(BraceletDetailActivity.this)) {
				// û�д����磬����������Ի���
				openNetConnectView();
			} else {
				// �ٵ㣬�����Ѵ�
				if (!isHadDownLoad) {
					if (!TextUtils.isEmpty(error_reason)) {
						// ���ع��ˣ��������˴���,�����߳̽�����
						Toast.makeText(BraceletDetailActivity.this,
								error_reason, Toast.LENGTH_SHORT).show();
					}
					downloadBinFile();
					return;
				} else {
					// ���������С�����
					Toast.makeText(this, "�����������¹̼���...", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}

	private void openDialog() {
		mProgressDialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/**
		 * ���÷��ؼ�
		 */
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (!isCanBack) {
				return false;
			} else {
				mProgressDialog.dismiss();
				closeService();
				onBackPressed();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void backnextView(View v) {
		mProgressDialog.dismiss();
		closeService();
		onBackPressed();
	}
}
