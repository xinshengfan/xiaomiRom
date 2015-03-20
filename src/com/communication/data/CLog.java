package com.communication.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.codoon.xiaomiupdata.MyXiaomiApp;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * create a log file save to local when apk is debugable state codoon_log in
 * sdcard dir
 * 
 * @author workEnlong
 * 
 */
public class CLog {

	private static File logFile;
	public static boolean isDebug;

	public static synchronized void v(String tag, String msg) {

		if (!isDebug) {
			return;
		}

		Log.v(tag, msg);
		println(tag, msg);
	}

	public static synchronized void e(String tag, String msg) {

		if (!isDebug) {
			return;
		}

		Log.e(tag, msg);
		println(tag, msg);
	}

	public static synchronized void d(String tag, String msg) {

		if (!isDebug) {
			return;
		}

		Log.d(tag, msg);
		println(tag, msg);
	}

	public static synchronized void i(String tag, String msg) {

		if (!isDebug) {
			return;
		}

		Log.i(tag, msg);
		println(tag, msg);
	}

	private static void println(String tag, String msg) {
		String str = msg;
		if (TextUtils.isEmpty(msg)) {
			str = "null";
		}
		if (null == logFile) {
			boolean result = creatLogFile();
			if (result) {
				try {
					FileOutputStream os = new FileOutputStream(logFile, true);
					SimpleDateFormat formatMsg = new SimpleDateFormat(
							"MM-dd-HH-mm--SS");
					String content = formatMsg.format(new Date(System
							.currentTimeMillis()))
							+ " "
							+ tag
							+ ": "
							+ str
							+ "\n";
					os.write(content.getBytes());
					os.flush();
					os.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Log.i("info", "没有SD卡.....");
			}
		}

	}

	/**
	 * set your private log file
	 * 
	 * @param name
	 * @return
	 */
	public static boolean createLogFile(String name) {
		String SDCARD_PATH;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			SDCARD_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			SDCARD_PATH = MyXiaomiApp.getInstance().getApplicationContext()
					.getFilesDir().getAbsolutePath();
		}
		File fileDir = new File(SDCARD_PATH + File.separator
				+ "codoon_xiaomi_log");
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}

		SimpleDateFormat format = new SimpleDateFormat("MM-dd-HH-mm");
		logFile = new File(fileDir.getAbsolutePath() + File.separator + name
				+ ".txt");

		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;

	}

	private static boolean creatLogFile() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String SDCARD_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			File fileDir = new File(SDCARD_PATH + File.separator
					+ "codoon_xiaomi_log");
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}

			SimpleDateFormat format = new SimpleDateFormat("MM-dd-HH-mm");
			logFile = new File(fileDir.getAbsolutePath() + File.separator
					+ "log_"
					+ format.format(new Date(System.currentTimeMillis()))
					+ ".txt");

			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					Log.i("info", e.getMessage());
					e.printStackTrace();
					return false;
				}
			} else {
				Log.i("info", "当前日志已存在");
			}
		} else {
			return false;
		}
		return true;
	}
}
