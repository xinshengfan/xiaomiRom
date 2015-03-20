package com.codoon.xiaomiupdata.xmlUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Perference {
	private SharedPreferences pf;
	private static final String KEY_FIRSTUSED = "keyfirstused";

	public Perference(Context context) {
		pf = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * �ж��Ƿ�Ϊ��һ��ʹ��
	 * 
	 * @return
	 */
	public boolean isFirstUsed() {
		boolean isFirst = pf.getBoolean(KEY_FIRSTUSED, true);
		if (isFirst) {
			pf.edit().putBoolean(KEY_FIRSTUSED, false).commit();
		}
		return isFirst;
	}
}
