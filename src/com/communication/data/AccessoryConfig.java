package com.communication.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AccessoryConfig {

	public static String userID = "";
	private final static String mXmlFile = "accessory_config_xml";

	/* ��ȡһ������ */
	public static int getIntValue(Context context, String key) {
		try {
			SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// ����storexml.xml
			return store.getInt(key, 0);// ��codoon_config_xml�ж�ȡ�ϴν��ȣ��浽string1��
		} catch (Exception e) {
			return 0;
		}
	}

	/* ��ȡһ������ */
	public static int getIntValue(Context context, String key, int defaultValue) {
		try {
			SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// ����storexml.xml
			return store.getInt(key, defaultValue);// ��codoon_config_xml�ж�ȡ�ϴν��ȣ��浽string1��
		} catch (Exception e) {
			return 0;
		}
	}

	/* ����һ������ */
	public static void setIntValue(Context context, String key, int value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putInt(key, value);
		editor.commit();

	}

	/* ��ȡһ�������� */
	public static Long getLongValue(Context context, String key, long defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// ����storexml.xml
		return store.getLong(key, defalut);// ��codoon_config_xml�ж�ȡ�ϴν��ȣ��浽string1��
	}

	/* ����һ�������� */
	public static void setLongValue(Context context, String key, long value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putLong(key, value);
		editor.commit();

	}

	/* ��ȡһ�������� */
	public static float getFloatValue(Context context, String key, float defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// ����storexml.xml
		return store.getFloat(key, defalut);// ��codoon_config_xml�ж�ȡ�ϴν��ȣ��浽string1��
	}

	/* ����һ�������� */
	public static void setFloatValue(Context context, String key, float value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	/* ��ȡһ���ַ� */
	public static String getStringValue(Context context, String key) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// ����storexml.xml
		return store.getString(key, "");// ��codoon_config_xml�ж�ȡ�ϴν��ȣ��浽string1��
	}

	/* ����һ���ַ� */
	public static void setStringValue(Context context, String key, String value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putString(key, value);
		editor.commit();

	}

	/* ��ȡһ������ֵ */
	public static boolean getBooleanValue(Context context, String key,
			boolean defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		return store.getBoolean(key, defalut);
	}

	/* ����һ������ֵ */
	public static void setBooleanValue(Context context, String key,
			boolean value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}
