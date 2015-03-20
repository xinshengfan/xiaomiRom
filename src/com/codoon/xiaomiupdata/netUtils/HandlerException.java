package com.codoon.xiaomiupdata.netUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import com.communication.data.CLog;

/**
 * 处理全局未知异常
 * 
 * @author FANFAN
 * 
 */
public class HandlerException implements UncaughtExceptionHandler {
	private static final String TAG = "UnCaugthtException";
	private static HandlerException instance;
	private Context context;
	private UncaughtExceptionHandler defaultExceptionHandler;

	/**
	 * 私有化构造方法
	 */
	private HandlerException() {
	}

	public synchronized static HandlerException getInstance() {
		if (instance == null) {
			instance = new HandlerException();
		}
		return instance;
	}

	public void initVariable(Context context) {
		defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		this.context = context;
		Thread.setDefaultUncaughtExceptionHandler(getInstance());
	}

	@Override
	public void uncaughtException(final Thread thread, final Throwable ex) {

		if (defaultExceptionHandler != null) {
			handleExceptionByUser(thread, ex);
			new Thread() {
				public void run() {
					Looper.prepare();
					CLog.i("info", "******* 1 ********");
					Toast.makeText(context, "程序崩溃了，请重新启动", Toast.LENGTH_LONG)
							.show();
					Intent startMain = new Intent(Intent.ACTION_MAIN);
					startMain.addCategory(Intent.CATEGORY_HOME);
					startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(startMain);
					try {
						Thread.sleep(3000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(0);
					Looper.loop();
				};
			}.start();
		}
	}

	/**
	 * 自定义错误处理
	 * 
	 * @param thread
	 * 
	 * @param ex
	 * @return
	 */
	private boolean handleExceptionByUser(final Thread thread,
			final Throwable ex) {
		if (ex == null) {
			return true;
		}
		// 异常退出，将信息存入 SD卡
		new Thread() {
			public void run() {
				// 创建文件名
				String fileName = "COD_MI_LOG_" + getCurrentData();
				CLog.i("info", "创建异常信息文件");
				File file = createLogFile(fileName);
				try {
					FileOutputStream fos = new FileOutputStream(file, true);
					fos.write(formatStackTrace(ex).getBytes());
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
					defaultExceptionHandler.uncaughtException(thread, ex);
				}

			};
		}.start();
		return true;
	}

	/**
	 * 格式化异常堆栈信息
	 * 
	 * @param ex
	 * @return
	 */
	protected String formatStackTrace(Throwable ex) {
		if (ex == null) {
			return "";
		}
		String message = "";
		StringBuilder sb = new StringBuilder();
		try {
			// 获取当前应用的版本号，版本信息
			PackageManager pageManager = context.getPackageManager();
			PackageInfo info = pageManager.getPackageInfo(
					context.getPackageName(), 0);
			String versionname = info.versionName;
			sb.append("版本号：" + versionname);
			sb.append("\n");

			sb.append("时间:" + getCurrentData() + "\n");
			// 获取手机的操作系统，硬件信息
			Field[] fields = Build.class.getFields();
			for (Field field : fields) {
				field.setAccessible(true);
				sb.append(field.getName());
				sb.append("=");
				sb.append(field.get(null).toString());
				sb.append("\n");
			}
			String rtn = ex.getStackTrace().toString();
			sb.append("\n" + rtn);
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ex.printStackTrace(printWriter);
			printWriter.flush();
			writer.flush();
			// CLog.i("info", "writer.toString():" + writer.toString());
			message = writer.toString();
			printWriter.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString() + "\n" + message;
	}

	protected File createLogFile(String fileName) {
		CLog.i("info", "创建日志文件");
		File logFile = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String SDCARD_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			File fileDir = new File(SDCARD_PATH + File.separator
					+ "codoon_xiaomi_log");
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}

			logFile = new File(fileDir.getAbsolutePath() + File.separator
					+ fileName + ".txt");

			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					CLog.i(TAG, e.getMessage());
					e.printStackTrace();
				}
			} else {
				CLog.i(TAG, "当前日志已存在");
			}
		}
		return logFile;
	}

	/**
	 * 得到当前日期
	 * 
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	protected String getCurrentData() {
		SimpleDateFormat formatMsg = new SimpleDateFormat("MM-dd-HH-mm--SS");

		return formatMsg.format(new Date(System.currentTimeMillis()));
	}

}
