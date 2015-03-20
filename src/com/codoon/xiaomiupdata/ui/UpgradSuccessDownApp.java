package com.codoon.xiaomiupdata.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.smartdevices.bracelet.Utils;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.codoon.xiaomiupdata.netUtils.AsychDownLoadFile;
import com.codoon.xiaomiupdata.netUtils.NetUtil;

public class UpgradSuccessDownApp extends MyBaseActivity {
	private TextView tv_success_hint;
	private Handler mHandler;
	private static final long SHOW_TIME = 2500L;
	private Animation slid_in_animation, slid_out_animation;
	private Button bt_down_load;
	private AsychDownLoadFile asychDownLoadFile;
	private ActivityManager am;
	public static final String apk_URL = "http://slbpkg.codoon.com/app/android/codoonsports.apk";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_success_downapp);

		initView();
	}

	private void initView() {
		slid_in_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_top);
		slid_out_animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_out_to_top);
		slid_out_animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				tv_success_hint.setVisibility(View.INVISIBLE);
			}
		});
		tv_success_hint = (TextView) findViewById(R.id.tv_upgrade_downapp_success_hint);
		// tv_success_hint.setVisibility(View.VISIBLE);
		tv_success_hint.startAnimation(slid_in_animation);
		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				tv_success_hint.startAnimation(slid_out_animation);
			}
		}, SHOW_TIME);
		bt_down_load = (Button) findViewById(R.id.bt_download_codoon);
		asychDownLoadFile = new AsychDownLoadFile(this, bt_down_load);
		am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
	}

	public void downloadCodoon(View v) {
		if (!Utils.isNetworkConnected(UpgradSuccessDownApp.this)) {
			Toast.makeText(this, "没有网络，请打开网络", Toast.LENGTH_SHORT).show();
			NetUtil.openWifiOrGPRS();
			return;
		}
		asychDownLoadFile.execute(apk_URL);

		bt_down_load.setBackground(getResources().getDrawable(
				R.drawable.btn_grey_disable));
		bt_down_load.setText(getResources().getString(R.string.down_load_ing));
		bt_down_load.setEnabled(false);
	}

	@Override
	public void finish() {
		super.finish();
		am.killBackgroundProcesses("com.xiaomi.hm.health");
		am.killBackgroundProcesses("com.codoon.xiaomiupdata");
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.KEYCODE_BACK) {
			if (MyXiaomiApp.getInstance().getCurrentModel() == MyXiaomiApp.UPGRADE_XIAOMI_CODOON) {
				Intent restart_intent = new Intent(this, WelcomeActivity.class);
				restart_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				restart_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(restart_intent);
				overridePendingTransition(R.anim.push_right_in,
						R.anim.push_right_out);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void backnextView(View v) {
		Intent restart_intent = new Intent(this, WelcomeActivity.class);
		restart_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		restart_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(restart_intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}
}
