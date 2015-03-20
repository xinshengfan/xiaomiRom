package com.codoon.xiaomiupdata.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;

public class NewFunctionActivity extends MyBaseActivity {
	private WebView web_new_function;
	private static final String NEW_CODMI_FUNCTION = "http://www.codoon.com/help/operation_guidence.html";
	private static final String NEW_CODOON_FUNCTION = "http://www.codoon.com/help/romlist.html";
	private int currentModel;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_function);
		intiData();
		initView();
	}

	private void intiData() {
		currentModel = MyXiaomiApp.getInstance().getCurrentModel();
	}

	@SuppressLint("NewApi")
	private void initView() {
		web_new_function = (WebView) findViewById(R.id.webview_new_function);
		dialog = new WebCustomProgressDialog(this);
		dialog.show();
		// ����ҳ���е����Ӽ����ڱ�browser����Ӧ
		web_new_function.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				view.setInitialScale(100);
				dialog.cancel();
			}
		});
		// �����������ԣ�
		WebSettings settings = web_new_function.getSettings();
		settings.setSupportZoom(true); // ֧�����ſ��أ�
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); // ����ҳ����Ӧ��Ļ���
		settings.setBuiltInZoomControls(true); // �Ƿ���ʾ���Ű�ť
		settings.setUseWideViewPort(true); // �������������
		// settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);
		switch (currentModel) {
		case MyXiaomiApp.UPGRADE_CODOON:
			// ��������
			web_new_function.loadUrl(NEW_CODOON_FUNCTION);
			break;
		case MyXiaomiApp.UPGRADE_BACK_XIAOMI:
			// ˢС��
			web_new_function.loadUrl(NEW_CODMI_FUNCTION);
			break;
		default:
			break;
		}

	}

	public void backnextView(View v) {
		onBackPressed();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dialog != null) {
			dialog.dismiss();
		}
	}
}
