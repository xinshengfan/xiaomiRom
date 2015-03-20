package com.codoon.xiaomiupdata.netUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.codoon.xiaomiupdata.R;
import com.communication.data.FileManager;

public class AsychDownLoadFile extends AsyncTask<String, Integer, Boolean> {
	private final static int CONNECTTIMEOUT = 30000;
	private final static int READTIMEOUT = 30000;
	private final static int DOWNLOAD_BUFFER_SIZE = 4096;
	private final static int MAX_PROGRESS = 100;
	public static final String downLoad_File = "CodoonSports.apk";
	private Context mContext;
	private Button button;
	private FileManager fileManager;

	public AsychDownLoadFile(Context mContext, Button button) {
		super();
		this.mContext = mContext;
		this.button = button;
		fileManager = new FileManager();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(params[0]);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(CONNECTTIMEOUT);
			conn.setReadTimeout(READTIMEOUT);

			if (conn.getResponseCode() == 200) {
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

				while ((bufferLength = in.read(buffer)) > 0) {
					fileOut.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
					publishProgress(new Integer[] { (int) (downloadedSize
							* MAX_PROGRESS / totalSize) });
				}
				fileOut.close();
				in.close();
				if (downloadedSize == totalSize) {
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
			button.setText("下载完成，正在安装...");
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
				if (MyXiaomiApp.getInstance().getCurrentModel() != MyXiaomiApp.UPGRADE_XIAOMI_CODOON) {
					MyXiaomiApp.getInstance().exitAll();
				}
			} catch (Exception e) {
				Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			Toast.makeText(mContext, "下载失败,请检查网络重新下载", Toast.LENGTH_SHORT)
					.show();
			button.setEnabled(true);
			button.setText(mContext.getResources().getString(
					R.string.ui_download_codoon));
			button.setBackgroundResource(R.drawable.btn_bg_selector);
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		button.setText("已下载" + values[0] + "%");

	}

}
