package com.codoon.xiaomiupdata.netUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.communication.data.CLog;
import com.communication.data.FileManager;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("HandlerLeak")
public class AsychDownLoadApp extends AsyncTask<String, Integer, Boolean> {
	private final static int CONNECTTIMEOUT = 30000;
	private final static int READTIMEOUT = 30000;
	private final static int DOWNLOAD_BUFFER_SIZE = 4096;
	private final static int MAX_PROGRESS = 100;
	public static final String downLoad_File = "codoon_rom.apk";
	private Context mContext;
	private FileManager fileManager;
	private NotificationManager manager;
	private Builder builder;
	private static final int NOTIFICTION_ID = 1;
	private Handler mHandler;
	private int what_hint = 2;
	private boolean isHadShow;

	public AsychDownLoadApp(final Context mContext) {
		super();
		this.mContext = mContext;
		fileManager = new FileManager();
		manager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new Builder(mContext);
		// builder.setLargeIcon(BitmapFactory.decodeResource(
		// mContext.getResources(), R.drawable.icon_cod_mi));
		builder.setSmallIcon(R.drawable.icon_cod_mi);
		builder.setContentTitle("下载咕咚Rom").setContentText("要运动，用咕咚")
				.setAutoCancel(false);
		builder.build().flags = Notification.FLAG_NO_CLEAR;
		isHadShow = false;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (!isHadShow) {
					Toast.makeText(mContext, "正在后台为你下载...", Toast.LENGTH_SHORT)
							.show();
					isHadShow = true;
				}
			}
		};
	}

	@Override
	protected Boolean doInBackground(String... params) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(params[0]);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(CONNECTTIMEOUT);
			conn.setReadTimeout(READTIMEOUT);
			CLog.i("info", "网络状态码：" + conn.getResponseCode());
			if (conn.getResponseCode() == 200) {
				mHandler.sendEmptyMessage(what_hint);
				long totalSize = conn.getContentLength();
				File file = new File(fileManager.getOtherPath(mContext)
						+ File.separator + new File(downLoad_File));
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
				int bufferLength = 0;
				long downloadedSize = 0;
				FileOutputStream fileOut = new FileOutputStream(file);
				InputStream in = conn.getInputStream();

				int percentum = 0;
				while ((bufferLength = in.read(buffer)) > 0) {
					fileOut.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
					if ((int) (downloadedSize * MAX_PROGRESS / totalSize) > percentum) {
						CLog.i("info", "下载的大小：" + percentum);
						percentum = (int) (downloadedSize * MAX_PROGRESS / totalSize);
						publishProgress(new Integer[] { percentum });
					}
				}
				fileOut.close();
				in.close();
				if (downloadedSize == totalSize) {
					CLog.i("info", "下载完成，大小一致");
					return true;
				}
			} else {
				Toast.makeText(mContext, "网络异常:" + conn.getResponseCode(),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			CLog.i("info", "下载完成，开始安装");
			builder.setContentTitle("下载成功");
			manager.notify(NOTIFICTION_ID, builder.build());
			// 取消通知
			manager.cancel(NOTIFICTION_ID);
			try {
				Uri uri = Uri.fromFile(new File(fileManager
						.getOtherPath(mContext)
						+ File.separator
						+ downLoad_File));

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(uri,
						"application/vnd.android.package-archive");
				mContext.startActivity(intent);

				MyXiaomiApp.getInstance().exitAll();
			} catch (Exception e) {
				Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			builder.setContentTitle("下载失败");
			manager.notify(NOTIFICTION_ID, builder.build());
			Toast.makeText(mContext, "下载失败,请检查网络重新下载", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		// 更新进度
		builder.setContentText("要运动，用咕咚！已为你下载：" + values[0] + "%");
		builder.setProgress(100, values[0], false);
		manager.notify(NOTIFICTION_ID, builder.build());
	}

}
