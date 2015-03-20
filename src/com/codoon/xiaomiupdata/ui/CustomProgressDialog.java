package com.codoon.xiaomiupdata.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.codoon.xiaomiupdata.R;

public class CustomProgressDialog extends ProgressDialog {

	public CustomProgressDialog(Context context) {
		super(context);
	}

	public CustomProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.customprogressdialog);
	}

	public static CustomProgressDialog show(Context context) {
		CustomProgressDialog dialog = new CustomProgressDialog(context,
				R.style.myProgressDialog);
		dialog.show();
		return dialog;
	}

}
