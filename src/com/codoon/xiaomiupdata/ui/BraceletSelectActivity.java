package com.codoon.xiaomiupdata.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.codoon.xiaomiupdata.HandlActivity;
import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;

public class BraceletSelectActivity extends MyBaseActivity {
	private TextView tv_bracelet_select;
	private static final String web_buy_Codoon = "http://item.jd.com/1154760.html";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bracelet_select);

		initView();
	}

	private void initView() {
		tv_bracelet_select = (TextView) findViewById(R.id.tv_bracelet_select);
		tv_bracelet_select.setText(Html.fromHtml("<u>"
				+ getResources().getString(R.string.ui_buy_codoon_bracelet)
				+ "</u>"));
		tv_bracelet_select.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(web_buy_Codoon);
				Intent web_intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(web_intent);
			}
		});

	}

	public void backnextView(View v) {
		onBackPressed();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	public void upgradexiaomi(View v) {
		if (android.os.Build.VERSION.SDK_INT < 18) {
			showWarningDialog();
		} else {
			startActivity(new Intent(this, HandlActivity.class));
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			MyXiaomiApp.getInstance().setCurrentModel(
					MyXiaomiApp.UPGRADE_BACK_XIAOMI);
		}
	}

	private void showWarningDialog() {
		new AlertDialog.Builder(this)
				.setMessage("手机系统版本过低，请使用Androd4.3以上并带蓝牙4.0功能的手机升级")
				.setPositiveButton("我知道了", null).show();

	}

	public void upgradeZte(View v) {
		if (android.os.Build.VERSION.SDK_INT < 18) {
			showWarningDialog();
		} else {
			startActivity(new Intent(this, CodoonChoiceActivity.class));
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			MyXiaomiApp.getInstance().setCurrentModel(
					MyXiaomiApp.UPGRADE_CODOON);
		}
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
