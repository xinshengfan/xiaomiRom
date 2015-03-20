package com.codoon.xiaomiupdata.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.codoon.xiaomiupdata.HandlActivity;
import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.codoon.xiaomiupdata.UpgradeXiaomiService;
import com.communication.data.CLog;
import com.communication.ui.G;

public class BraceletConnectActivity extends MyBaseActivity {
	private ImageView imv;
	private AnimationDrawable searchAnim;
	private int[] images = { R.drawable.seartch_codoon_bleband_1,
			R.drawable.seartch_codoon_bleband_2 };
	private int[] xiaomi_images = { R.drawable.device_searching_xm_0,
			R.drawable.device_searching_xm_1 };
	private int currentModel;
	private Intent intent;

	private ConnectedReciver reciver;

	private class ConnectedReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent2) {
			String action = intent2.getAction();
			if (G.ACTION_CONNECTED_BRACELET.equals(action)) {
				// 连接上了
				startActivity(new Intent(BraceletConnectActivity.this,
						SearchOverActivity.class));
				overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
				BraceletConnectActivity.this.finish();
			} else if (G.ACTION_TIME_OUT.equals(action)) {
				// 连接超时
				searchAnim.stop();
				BraceletConnectActivity.this.stopService(intent);
				startActivity(new Intent(BraceletConnectActivity.this,
						HandlActivity.class));
				BraceletConnectActivity.this.finish();
			}

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect_bracelet);

		CLog.i("info", "启动服务 service ");
		intent = new Intent(this, UpgradeXiaomiService.class);
		startService(intent);

		initView();
		initReciver();
	}

	private void initReciver() {
		reciver = new ConnectedReciver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(G.ACTION_CONNECTED_BRACELET);
		filter.addAction(G.ACTION_TIME_OUT);
		registerReceiver(reciver, filter);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(reciver);
	}

	@SuppressLint("NewApi")
	private void initView() {
		imv = (ImageView) findViewById(R.id.icon_connect_device_discovry);
		searchAnim = new AnimationDrawable();
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			for (int i = 0; i < images.length; i++) {
				Drawable drawable = getResources()
						.getDrawable(xiaomi_images[i]);
				searchAnim.addFrame(drawable, 300);
			}
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
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
	}

	public void backnextView(View v) {
		searchAnim.stop();
		BraceletConnectActivity.this.stopService(intent);
		BraceletConnectActivity.this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.KEYCODE_BACK) {
			searchAnim.stop();
			BraceletConnectActivity.this.stopService(intent);
			BraceletConnectActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void finish() {
		super.finish();
		searchAnim.stop();
	}
}
