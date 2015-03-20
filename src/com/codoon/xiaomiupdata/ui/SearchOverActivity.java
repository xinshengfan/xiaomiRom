package com.codoon.xiaomiupdata.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.codoon.xiaomiupdata.UpgradeXiaomiService;
import com.communication.data.CLog;
import com.communication.ui.G;

public class SearchOverActivity extends MyBaseActivity {
	private TextView tv_titil, tv_research, tv_content, tv_hint;
	private int currentModel;
	private MyRecevier recevier;
	private boolean isCodoonCboot;

	private ImageView im_over;

	private class MyRecevier extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (G.ACTION_BIND_SUCCESS.equals(action)) {
				CLog.i("info", "绑定成功，接收广播");
				Intent startIntent = new Intent(SearchOverActivity.this,
						BraceletDetailActivity.class);
				startIntent.putExtra(G.KEY_CONNECT_BRACELECT, true);
				startActivity(startIntent);
				overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
				SearchOverActivity.this.finish();
			} else if (G.ACTION_TIME_OUT.equals(action)) {
				//连接超时
				Intent intent_timeout = new Intent(SearchOverActivity.this,
						BraceletDetailActivity.class);
				intent_timeout.putExtra(G.KEY_CONNECT_BRACELECT, false);
				startActivity(intent_timeout);
				overridePendingTransition(R.anim.fw_in, R.anim.fw_out);
				SearchOverActivity.this.finish();
			}

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_over);

		intidata();
		initView();

		recevier = new MyRecevier();
		IntentFilter filter = new IntentFilter();
		filter.addAction(G.ACTION_BIND_SUCCESS);
		filter.addAction(G.ACTION_TIME_OUT);
		registerReceiver(recevier, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (recevier != null) {
			unregisterReceiver(recevier);
			recevier = null;
		}
	}

	private void intidata() {
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();
		isCodoonCboot = MyXiaomiApp.getInstance().isCodoonCboot();
		if (isCodoonCboot) {
			// 直接跳转
			Intent startIntent = new Intent(SearchOverActivity.this,
					BraceletDetailActivity.class);
			startIntent.putExtra(G.KEY_CONNECT_BRACELECT, true);
			startActivity(startIntent);
			SearchOverActivity.this.finish();
		}
		// else {
		// CLog.i("info", "启动服务 service ");
		// intent = new Intent(this, UpgradeXiaomiService.class);
		// startService(intent);
		// }
	}

	@SuppressLint("NewApi")
	private void initView() {
		tv_titil = (TextView) findViewById(R.id.tv_search_over_title);
		tv_research = (TextView) findViewById(R.id.tv_search_over_research);
		tv_content = (TextView) findViewById(R.id.tv_search_over_content);
		tv_hint = (TextView) findViewById(R.id.tv_search_over_hint);
		im_over = (ImageView) findViewById(R.id.imv_search_over);
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			tv_titil.setText(R.string.search_bracelet);
			tv_content.setText(R.string.ui_search_over_xiaomi_content);
			tv_hint.setText(R.string.ui_search_over_xiaomi);
			im_over.setBackground(getResources().getDrawable(
					R.drawable.ic_xm_ban_h));
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
			tv_titil.setText(R.string.ui_search_codoon_title);
			tv_content.setText(R.string.ui_search_over_codoon_content);
			tv_hint.setText(R.string.ui_search_over_codoon);
			im_over.setBackground(getResources().getDrawable(
					R.drawable.icon_codoon_bleband_h));
			break;
		default:
			break;
		}
		addListener();
	}

	private void addListener() {
		tv_research.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//停止后台搜索服务
				Intent intent =  new Intent(SearchOverActivity.this, UpgradeXiaomiService.class);
				stopService(intent);
				startActivity(new Intent(SearchOverActivity.this,
						SearchBraceleting.class));
				finish();
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			SearchOverActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void backnextView(View v) {
		SearchOverActivity.this.finish();
	}

}
