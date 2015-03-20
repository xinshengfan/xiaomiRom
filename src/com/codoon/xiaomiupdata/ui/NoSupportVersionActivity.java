package com.codoon.xiaomiupdata.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import cn.com.smartdevices.bracelet.BraceletApp;

import com.codoon.xiaomiupdata.R;
import com.xiaomi.hm.bleservice.profile.IMiLiProfile$DeviceInfo;
import com.xiaomi.hm.bleservice.profile.MiLiProfile;

public class NoSupportVersionActivity extends MyBaseActivity {
	private TextView tv_xiaomi_version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nosupport_version);

		initView();
	}

	private void initView() {
		tv_xiaomi_version = (TextView) findViewById(R.id.tv_xiaomi_version);

		MiLiProfile localMiLiProfile = (MiLiProfile) BraceletApp.BLEService
				.getDefaultPeripheral();
		if (localMiLiProfile != null) {
			IMiLiProfile$DeviceInfo localDeviceInfo = localMiLiProfile
					.getCachedDeviceInfo();
			if (localDeviceInfo != null) {
				String version = localDeviceInfo.getFirmwareVersionStr();
				String versionCode = version.replaceAll("\\.", "");
				int code = Integer.parseInt(versionCode);
				if (code >= 1043) {
					tv_xiaomi_version.setText("当前小米手环固件版本为：" + version
							+ "；\n咕咚刷暂不支持！");
				} else {
					tv_xiaomi_version.setText("刷入固件失败，请重试！");
				}
			}
		}

	}

	public void doClickOver(View v) {
		// 返回到主界面
		backMainView();
	}

	private void backMainView() {
		Intent restart_intent = new Intent(this, WelcomeActivity.class);
		restart_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		restart_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		restart_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(restart_intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.KEYCODE_BACK) {
			// 返回到主界面
			backMainView();
		}
		return super.onKeyDown(keyCode, event);
	}
}
