package com.codoon.xiaomiupdata.ui;

import android.os.Bundle;
import android.view.View;

import com.codoon.xiaomiupdata.R;

public class BackXiaomiOver extends MyBaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backxiaomi_over);
	}

	public void doClickOver(View v) {
		BackXiaomiOver.this.finish();
	}

}
