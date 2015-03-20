package com.codoon.xiaomiupdata.ui;

import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.communication.data.CLog;

public class UpgradeSuccessActivity extends MyBaseActivity {
	private TextView tv_success_hint;
	private Handler mHandler;
	private ActivityManager am;
	private static final long SHOW_TIME = 2500L;
	private Animation slid_in_animation, slid_out_animation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upgradesuccess);

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
		tv_success_hint = (TextView) findViewById(R.id.tv_upgrade_success_hint);
		// tv_success_hint.setVisibility(View.VISIBLE);
		tv_success_hint.startAnimation(slid_in_animation);
		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				tv_success_hint.startAnimation(slid_out_animation);
			}
		}, SHOW_TIME);
		am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
	}

	public void openCodoon(View v) {
		closeAllActivity();
		try {
			ComponentName componentName = new ComponentName("com.codoon.gps",
					"com.codoon.gps.ui.WelcomeActivity");
			Intent intent = new Intent();
			intent.setComponent(componentName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			CLog.i("info", "跳转咕咚运动");
			startActivity(intent);
		} catch (Exception e) {
			CLog.i("info", "打开咕咚异常信息：" + e.getMessage());
			e.printStackTrace();
		}
		CLog.i("info", "exitAll()");
		if (MyXiaomiApp.getInstance().getCurrentModel() != MyXiaomiApp.UPGRADE_XIAOMI_CODOON) {
			MyXiaomiApp.getInstance().exitAll();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.KEYCODE_BACK) {
			if (MyXiaomiApp.getInstance().getCurrentModel() == MyXiaomiApp.UPGRADE_XIAOMI_CODOON) {
				// 返回到主界面
				Intent restart_intent = new Intent(this, WelcomeActivity.class);
				restart_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				restart_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(restart_intent);
				overridePendingTransition(R.anim.push_right_in,
						R.anim.push_right_out);
				// this.finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void finish() {
		super.finish();
		am.killBackgroundProcesses("com.codoon.xiaomiupdata");
		CLog.i("info", "利用反射强制停止运行");
		try {
			forceStopPackage(this.getPackageName(), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		// System.exit(0);
	}

	/**
	 * 利用反射强制停止应用程序
	 * 
	 * @param pkgName
	 */
	private void forceStopPackage(String pkgName, Context context)
			throws Exception {
		ActivityManager am2 = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		Method method = Class.forName("android.app.ActivityManager").getMethod(
				"forceStopPackage", String.class);
		method.invoke(am2, pkgName);
	}

	protected void closeAllActivity() {
		CLog.i("info", "打开主屏幕");
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

	public void backnextView(View v) {
		// 返回到主界面
		Intent restart_intent = new Intent(this, WelcomeActivity.class);
		restart_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		restart_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(restart_intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		// finish();
	}
}
