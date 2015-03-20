package com.codoon.xiaomiupdata.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.codoon.xiaomiupdata.R;

public class WebCustomProgressDialog extends ProgressDialog {

	public WebCustomProgressDialog(Context context) {
		super(context);
	}

	public WebCustomProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webcustomprogressdialog);
	}

	public static WebCustomProgressDialog show(Context context) {
		WebCustomProgressDialog dialog = new WebCustomProgressDialog(context,
				R.style.myProgressDialog);
		dialog.show();
		return dialog;
	}

}
