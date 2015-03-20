package com.codoon.xiaomiupdata.netUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.codoon.xiaomiupdata.MyXiaomiApp;
import com.communication.data.CLog;
import com.communication.data.FileManager;
import com.communication.ui.G;

public class NetUtil {
	public static final String ID_URL = "http://www.codoon.com/agent/get_rom_product_id?type_id=19&mac=";
	public static final String VSERSION_URL = "http://static.codoon.com/hardware/xiaomi_rom.json";
	// public static final String BIN_URL =
	// "http://static.codoon.com/hardware/CBL_wx_3.18.1.bin";
	private static ConnectivityManager connmanager;
	private static int TimeOut_Count = 30000;
	private static UrlParameterCollection mUrlParameterCollection;
	private static int bin_length;
	static {
		connmanager = (ConnectivityManager) MyXiaomiApp.getInstance()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mUrlParameterCollection = new UrlParameterCollection();
	}

	@SuppressWarnings("unchecked")
	public static String getVersion(Context context) {
		Object downloadFile = downloadVersionFile(context, VSERSION_URL);
		CLog.i("info", "���յ����ݣ�" + downloadFile.toString());
		if (null != downloadFile && downloadFile instanceof List<?>) {
			List<AccessoryInfo> infos = (List<AccessoryInfo>) downloadFile;
			AccessoryInfo info = infos.get(0);
			MyXiaomiApp.getInstance().setCurrentInfo(info);
			return info.version_name;
		} else if (downloadFile != null && downloadFile instanceof String) {
			return (String) downloadFile;
		}
		return "";
	}

	public static void downloadLastBin(Context context, AccessoryInfo info) {
		RequestResult mRequestResult = null;
		File file = null;
		boolean result = false;
		String error_reson = "";
		FileManager fileManager = new FileManager();
		try {
			mRequestResult = getRequestResult(context, info.binary_url);
			if (mRequestResult == null) {
				error_reson = "�����쳣��û�з���ֵ";
			} else if (mRequestResult.getStatusCode() == 200) {
				long totalSize = bin_length;
				file = new File(fileManager.getOtherPath(context)
						+ File.separator + MyXiaomiApp.CODOONFILENAME);
				if (file.exists()) {
					CLog.i("info", "�ļ��Ѵ�����ɾ��");
					file.delete();
				}
				file.createNewFile();
				byte[] buffer = new byte[1024];
				int bufferLength = 0;
				long downloadedSize = 0;
				FileOutputStream fileOut = new FileOutputStream(file);
				InputStream in = mRequestResult.asStream();

				while ((bufferLength = in.read(buffer)) > 0) {
					fileOut.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
				}
				fileOut.close();
				in.close();
				CLog.i("info", "downloadedSize:" + downloadedSize
						+ ";totalSize:" + totalSize);
				if (downloadedSize == totalSize) {
					String MD5 = MD5Uitls.getFileMD5String(file);
					CLog.i("info", "MD5:" + MD5 + ";info.checksum:"
							+ info.checksum);
					if (MD5 != null && MD5.equalsIgnoreCase(info.checksum)) {
						result = true;
					} else {
						CLog.i("info", "���ع̼�MD5�������");
						error_reson = "ROM����ʧ�ܣ����Ժ�����";
					}
				} else {
					CLog.i("info", "���ع̼��ļ�����");
					error_reson = "�̼�����ʧ�ܣ����Ժ�����";
				}
			} else {
				// error_reson = "�����쳣��" + mRequestResult.getStatusCode();
				error_reson = "�����쳣,���Ժ�����";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mRequestResult != null) {
				mRequestResult.DisConnect();
			}
		}
		// ���سɹ������͹㲥
		Intent download_over_intent = new Intent(G.ACTION_DOWNLOAD_OVER);
		download_over_intent.putExtra(G.KEY_DOWNLOAD_OVER, result);
		download_over_intent.putExtra(G.KEY_DOWNLOAD_ERROR_REASON, error_reson);
		context.sendBroadcast(download_over_intent);
	}

	/**
	 * ����URL�����ļ�,ǰ��������ļ����е��������ı�,�����ķ���ֵ�����ı����е����� <br/>
	 * 1.����һ��URL���� <br/>
	 * 2.ͨ��URL����,����һ��HttpURLConnection���� <br/>
	 * 3.�õ�InputStream <br/>
	 * 4.��InputStream���ж�ȡ����
	 * 
	 * @param urlStr
	 * @return
	 */
	/**
	 * HttpЭ�鷵��ֵ�������� 200 Ϊ�ɹ���<br/>
	 * 400Ϊ�������<br/>
	 * 404Ϊδ�ҵ���<br/>
	 * 500Ϊ�������ڲ�����<br/>
	 * 403��Ȩ�鿴��<br/>
	 * 302Ϊ�ض���
	 */
	public static Object downloadVersionFile(Context context, String url) {
		Object object = null;
		try {
			RequestResult requestResult = getRequestResult(context, url);
			CLog.i("info", "requestResult is null?:" + (requestResult == null)
					+ "code:" + requestResult.getStatusCode());
			if (requestResult != null && requestResult.getStatusCode() == 200) {
				// ���ʳɹ�
				object = parseJson(requestResult.asStream());
			} else if (requestResult != null
					&& requestResult.getStatusCode() == 404) {
				CLog.i("info", "404����");
				return "404����";
			} else if (requestResult != null
					&& requestResult.getStatusCode() == 500) {
				CLog.i("info", "500�������ڲ�����");
				return "500�������ڲ�����";
			} else if (requestResult != null
					&& requestResult.getStatusCode() == 403) {
				CLog.i("info", "403��Ȩ�鿴");
				return "403��Ȩ�鿴";
			} else if (requestResult != null
					&& requestResult.getStatusCode() == 302) {
				CLog.i("info", "302�ض���");
				return "302�ض���";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}

	private static List<AccessoryInfo> parseJson(InputStream inStream) {
		// TODO Auto-generated method stub

		if (null == inStream) {
			Log.e("info", "input stream is null");
			return null;
		}
		List<AccessoryInfo> infos = null;

		byte[] buf = new byte[512];
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		int len = 0;

		try {
			while ((len = inStream.read(buf)) != -1) {
				outStream.write(buf, 0, len);
			}
			inStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String json = "";
		try {
			json = new String(outStream.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		json = json.replaceAll(" ", "").replaceAll("��", "");
		// String json = outStream.toString().replaceAll(" ", "").replace("\\n",
		// "").replace("\\r\\n", "");
		Log.d("info", json);
		try {

			JSONArray jsonObjs = new JSONArray(json);
			infos = new ArrayList<AccessoryInfo>();
			for (int i = 0; i < jsonObjs.length(); i++) {
				JSONObject obj = jsonObjs.optJSONObject(i);
				if (null != obj) {
					AccessoryInfo info = new AccessoryInfo();
					info.description = obj.getString("description");
					info.checksum = obj.getString("checksum");
					info.binary_url = obj.getString("binary_url");
					info.version_code = obj.getInt("version_code");
					info.device_name = obj.getString("device_name");
					info.device_type = obj.getInt("device_type");
					info.date = obj.getString("date");
					info.version_name = obj.getString("version_name");
					info.size = obj.getLong("size");
					info.popup = obj.getInt("popup");
					infos.add(info);
				}

			}

		} catch (Exception e) {
			infos = null;
		}

		try {
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return infos;
	}

	public static RequestResult getRequestResult(Context context, String urlPath)
			throws IOException {
		StringBuilder tmpurl = new StringBuilder();
		tmpurl.append(urlPath);
		if (urlPath.indexOf("?") < 0) {
			tmpurl.append("?");
		}
		tmpurl.append(encodeParameters(mUrlParameterCollection));

		String urlt = tmpurl.toString();
		String pUrl = "http://10.0.0.172";
		urlt = urlt.replace("http://", "");
		pUrl = pUrl + urlt.substring(urlt.indexOf("/"));
		urlt = urlt.substring(0, urlt.indexOf("/"));

		// �½�һ��URL����
		URL url;
		HttpURLConnection urlConn;// ��һ��HttpURLConnection����
		// �����wap����
		if (NetUtil.isCmwap(context)) {
			url = new URL(pUrl);
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestProperty("X-Online-Host", urlt);

		} else {
			url = new URL(tmpurl.toString());
			Log.v("info", "requestByGet://" + tmpurl.toString());
			urlConn = (HttpURLConnection) url.openConnection();
		}

		// �������ӳ�ʱʱ��
		urlConn.setConnectTimeout(TimeOut_Count);
		urlConn.setReadTimeout(TimeOut_Count);
		// ��ʼ����
		urlConn.connect();
		bin_length = urlConn.getContentLength();
		// �ж������Ƿ�ɹ�
		return new RequestResult(urlConn);
	}

	private static String encodeParameters(UrlParameterCollection urlParameters) {
		List<UrlParameter> parameters = urlParameters.Parameters();
		StringBuffer buf = new StringBuffer();

		for (int j = 0; j < parameters.size(); j++) {
			if (j != 0) {
				buf.append("&");
			}
			try {
				/*
				 * if (parameters.get(j).name.equals("")) {
				 * buf.append(URLEncoder.encode(parameters.get(j).value,
				 * "UTF-8")); } else {
				 */
				buf.append(URLEncoder.encode(parameters.get(j).name, "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(parameters.get(j).value,
								"UTF-8"));
				/* } */

			} catch (java.io.UnsupportedEncodingException neverHappen) {
			}
		}
		return buf.toString();
	}

	/**
	 * �жϵ�ǰ�����Ƿ�ΪCWmap
	 * 
	 * @param context
	 * @return
	 */
	private static boolean isCmwap(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		String extraInfo = info.getExtraInfo();
		// Constant.debug("extraInfo ---> " + extraInfo);
		// �����࣬�ж��Ƿ�Ϊ�ռ�null
		if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3)) {
			return false;
		}
		if (extraInfo.toLowerCase().indexOf("wap") > 0) {
			return true;
		}
		return false;
	}

	public static String getID(Context context) {
		String id = "";
		// ȡ��msc��
		String mac = getMsc(context);
		CLog.i("info", "��ȡ��DeviceID:" + mac);
		// ƴ������·��
		String url = ID_URL + mac;
		CLog.i("info", "�����·��:" + url);
		// ��������,��ȡjson
		JSONObject json = getIdFromNet(context, url);
		// ��ȡid
		id = anayleJSON(json);
		CLog.i("info", "��ȡ��ID:" + id);
		return id;
	}

	/**
	 * ��json�����н�����idֵ
	 * 
	 * @param json
	 * @return
	 */
	private static String anayleJSON(JSONObject json) {
		String id = "";
		if (json != null) {
			CLog.i("info", "���ص�json:" + json.toString());
			try {
				id = json.getString("product_id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			CLog.i("info", "json is null");
		}
		return id;
	}

	/**
	 * ��������ID
	 * 
	 * @param url
	 * @return
	 */
	private static JSONObject getIdFromNet(Context context, String url) {
		JSONObject obj = null;
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);// ���ó�ʱ;
		HttpGet get = new HttpGet(url);
		// ����get����
		HttpResponse response = null;
		try {
			CLog.i("info", " client.execute(get)");
			response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// ����ɹ�;
				HttpEntity entity = response.getEntity();
				String res = EntityUtils.toString(entity);
				CLog.i("info", "��ȡ��entity:" + res);
				obj = new JSONObject(res);
			} else {
				CLog.i("info", "������ȡIDʧ��:����ֵ��"
						+ response.getStatusLine().getStatusCode());
				// Toast.makeText(context, "������ȡIDʧ��",
				// Toast.LENGTH_SHORT).show();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// ��ȡ��Ӧʵ��
		return obj;
	}

	/**
	 * ��ȡ�豸������
	 * 
	 * @return
	 */
	private static String getMsc(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public static void openWifiOrGPRS() {
		if (!openWifi()) {
			// wifi�����ã���gprs
			if (isMobileConnected()) {
				CLog.i("info", "�ƶ��������");
				Class<?> gprs = connmanager.getClass();
				Class<?>[] argClasses = new Class[1];
				argClasses[0] = boolean.class;

				// ����ConnectivityManager��hide�ķ���setMobileDataEnabled�����Կ����͹ر�GPRS����
				Method method;
				try {
					method = gprs.getMethod("setMobileDataEnabled", argClasses);
					method.invoke(connmanager, true);
				} catch (NoSuchMethodException e) {
					CLog.i("info", "û�д˷���");
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					CLog.i("info", "�Ƿ����IllegalAccessException");
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					CLog.i("info",
							"�쳣��ϢIllegalArgumentException��" + e.getMessage());
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					CLog.i("info",
							"�쳣��ϢInvocationTargetException:" + e.getMessage());
					e.printStackTrace();
				}
			} else {
				Toast.makeText(MyXiaomiApp.getInstance(), "�ƶ����粻����",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	/**
	 * ��wifi
	 * 
	 * @return
	 */
	private static boolean openWifi() {
		WifiManager wifi = (WifiManager) MyXiaomiApp.getInstance()
				.getSystemService(Context.WIFI_SERVICE);
		return wifi.setWifiEnabled(true);
	}

	private static boolean isMobileConnected() {
		NetworkInfo info = connmanager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (info != null) {
			return info.isConnected();
		}
		return false;
	}

}
