package com.codoon.xiaomiupdata.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import cn.com.smartdevices.bracelet.Utils;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.codoon.xiaomiupdata.netUtils.AsychDownLoadApp;
import com.codoon.xiaomiupdata.view.CircleWaveView;
import com.codoon.xiaomiupdata.xmlUtil.ParseStream;
import com.codoon.xiaomiupdata.xmlUtil.StateInfo;
import com.codoon.xiaomiupdata.xmlUtil.StreamUtils;
import com.communication.data.CLog;

/**
 * 欢迎界面
 * 
 * @author FANFAN
 * 
 */
public class WelcomeActivity extends Activity {
	private CircleWaveView circleWaveView;
	private ImageView imv;
	private static final String APP_VERSION_URL = "http://static.codoon.com/app/android/codoon_rom.xml";
	private int currentVersionCode;
	private Handler mHandler;
	private static final int WHAT_UPGRAE_APP = 1;
	private static final int WHAT_CHECK_JSON = 2;
	private AlertDialog.Builder builder;
	private StateInfo info;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		CLog.i("info", "main onCreate()");
		initView();
		intitData();
		judgeVersion();
		// 为了优化性能，故在页面已经加载完成后再去启动线程做复制文件及联网操作
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void judgeVersion() {
		if (android.os.Build.VERSION.SDK_INT < 18) {
			new AlertDialog.Builder(this)
					.setMessage(
							"当前手机系统版本：" + (Build.VERSION.RELEASE)
									+ "\n\n咕咚刷支持Android4.3以上并有蓝牙4.0功能的机型")
					.setPositiveButton("我知道了", null).show();
		} else {
			if (Utils.isNetworkConnected(this)) {
				// 若已正确连网，就去获取最新版本信息
				mHandler.sendEmptyMessageDelayed(WHAT_CHECK_JSON, 10L);
			} else {
				CLog.i("info", "没有连接到网络");
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private void intitData() {
		PackageManager manager = this.getPackageManager();
		try {
			currentVersionCode = manager.getPackageInfo(this.getPackageName(),
					0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (WHAT_UPGRAE_APP == msg.what) {
					showUpgradeDialog();
				} else if (WHAT_CHECK_JSON == msg.what) {
					if (!MyXiaomiApp.getInstance().isHadHintUpgrade()) {
						// 还没有提示过
						handleVersion();
						MyXiaomiApp.getInstance().setHadHintUpgrade(true); // 设置为已经提示过了
					} else {
						CLog.i("info", "已经提示过了");
					}
				}
			}
		};
	}

	protected void showUpgradeDialog() {
		builder.setTitle("版本更新提示")
				.setMessage(
						"更新内容：\n" + info.getVersion_name() + "\n大小："
								+ info.getSize() + "\n时间："
								+ info.getData_time() + "\n说明："
								+ info.getDescription())
				.setPositiveButton("立即更新", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AsychDownLoadApp asychDownLoadApp = new AsychDownLoadApp(
								WelcomeActivity.this);
						asychDownLoadApp.execute(info.getApp_url());
					}
				}).setNegativeButton("下次再说", null).show();

	}

	private void handleVersion() {
		// 若已正确连网，就去获取最新版本信息
		new Thread() {
			public void run() {
				try {
					HttpEntity entity = StreamUtils.getEntity(APP_VERSION_URL,
							null, StreamUtils.METHOD_GET);
					InputStream inStream = StreamUtils.getStream(entity);
					CLog.i("info", "inStream is null?" + (inStream == null));
					ArrayList<StateInfo> list = ParseStream
							.parseStream(inStream);
					if (list != null && list.size() != 0) {
						info = list.get(0);
						CLog.i("info", "version:" + info.getVersion());
						if (info.getVersion() > currentVersionCode) {
							// 服务端版本比当前版本新
							mHandler.sendEmptyMessage(WHAT_UPGRAE_APP);
						}
					} else {
						CLog.i("info", "没有返回对象");
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		}.start();

	}

	private void initView() {
		circleWaveView = (CircleWaveView) findViewById(R.id.circle_welcome);
		imv = (ImageView) findViewById(R.id.imv_welcome);
		imv.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					imv.setBackgroundResource(R.drawable.btn_braceletrom_pressed);
					break;
				case MotionEvent.ACTION_MOVE:
					circleWaveView.setTime(10L);
					break;
				case MotionEvent.ACTION_UP:
					imv.setBackgroundResource(R.drawable.btn_braceletrom_normal);
					startActivity(new Intent(WelcomeActivity.this,
							BraceletSelectActivity.class));
					overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
					break;
				default:
					break;
				}
				return true;
			}
		});
		circleWaveView.setVisibility(View.VISIBLE);
		circleWaveView.start();
		builder = new AlertDialog.Builder(this);

	}

	public void relationOur(View v) {
		startActivity(new Intent(this, RelactionOurActivity.class));
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			circleWaveView.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		CLog.i("info", "onResume");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.KEYCODE_BACK) {
			// 退出
			MyXiaomiApp.getInstance().exitAll();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

}
