package com.codoon.xiaomiupdata.ui;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.codoon.xiaomiupdata.R;

public class RelactionOurActivity extends MyBaseActivity {
	private TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_relaction_our);

		initView();
	}

	private void initView() {
		tv = (TextView) findViewById(R.id.tv_relaction_version);
		PackageManager manager = this.getPackageManager();
		try {
			String name = manager.getPackageInfo(this.getPackageName(), 0).versionName;
			if (!TextUtils.isEmpty(name)) {
				tv.setText("¹¾ßËË¢V" + name);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void backnextView(View v) {
		onBackPressed();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

}
