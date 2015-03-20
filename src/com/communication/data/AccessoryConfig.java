package com.communication.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AccessoryConfig {

	public static String userID = "";
	private final static String mXmlFile = "accessory_config_xml";

	/* 读取一个整数 */
	public static int getIntValue(Context context, String key) {
		try {
			SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// 建立storexml.xml
			return store.getInt(key, 0);// 从codoon_config_xml中读取上次进度，存到string1中
		} catch (Exception e) {
			return 0;
		}
	}

	/* 读取一个整数 */
	public static int getIntValue(Context context, String key, int defaultValue) {
		try {
			SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// 建立storexml.xml
			return store.getInt(key, defaultValue);// 从codoon_config_xml中读取上次进度，存到string1中
		} catch (Exception e) {
			return 0;
		}
	}

	/* 设置一个整数 */
	public static void setIntValue(Context context, String key, int value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putInt(key, value);
		editor.commit();

	}

	/* 读取一个长整数 */
	public static Long getLongValue(Context context, String key, long defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// 建立storexml.xml
		return store.getLong(key, defalut);// 从codoon_config_xml中读取上次进度，存到string1中
	}

	/* 设置一个长整数 */
	public static void setLongValue(Context context, String key, long value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putLong(key, value);
		editor.commit();

	}

	/* 获取一个浮点数 */
	public static float getFloatValue(Context context, String key, float defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// 建立storexml.xml
		return store.getFloat(key, defalut);// 从codoon_config_xml中读取上次进度，存到string1中
	}

	/* 设置一个浮点数 */
	public static void setFloatValue(Context context, String key, float value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	/* 获取一个字符 */
	public static String getStringValue(Context context, String key) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);// 建立storexml.xml
		return store.getString(key, "");// 从codoon_config_xml中读取上次进度，存到string1中
	}

	/* 设置一个字符 */
	public static void setStringValue(Context context, String key, String value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putString(key, value);
		editor.commit();

	}

	/* 获取一个布尔值 */
	public static boolean getBooleanValue(Context context, String key,
			boolean defalut) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		return store.getBoolean(key, defalut);
	}

	/* 设置一个布尔值 */
	public static void setBooleanValue(Context context, String key,
			boolean value) {
		SharedPreferences store = context.getSharedPreferences(mXmlFile, 0);
		SharedPreferences.Editor editor = store.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}
