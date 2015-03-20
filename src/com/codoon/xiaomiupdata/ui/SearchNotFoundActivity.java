package com.codoon.xiaomiupdata.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;

public class SearchNotFoundActivity extends MyBaseActivity {
	private TextView tv_notfound_title, tv_nofound_content;
	private int currentModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aactivity_notsearch_bracelet);
		intiData();
		intView();
	}

	private void intiData() {
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();

	}

	private void intView() {
		tv_notfound_title = (TextView) findViewById(R.id.tv_search_not_find_title);
		tv_nofound_content = (TextView) findViewById(R.id.tv_search_notfind_content);
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			tv_notfound_title.setText(R.string.search_bracelet);
			tv_nofound_content
					.setText(R.string.ui_search_notfound_xiaomi_content);
			break;
		case MyXiaomiApp.UPGRADE_CODOON:
			tv_notfound_title.setText(R.string.ui_search_codoon_title);
			tv_nofound_content
					.setText(R.string.ui_search_notfound_codoon_content);
			break;
		default:
			break;
		}

	}

	/**
	 * ÖØÐÂËÑË÷
	 * 
	 * @param v
	 */
	public void research(View v) {
		startActivity(new Intent(SearchNotFoundActivity.this,
				SearchBraceleting.class));
		finish();
	}

	public void backnextView(View v) {
		onBackPressed();
	}

}
