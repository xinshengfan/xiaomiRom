package com.codoon.xiaomiupdata.xmlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import com.communication.data.CLog;

/*
 * 
 */
public class StreamUtils {
	// 请求方式声明
	public static final int METHOD_GET = 0;
	public static final int METHOD_POST = 1;
	private static int count = 0;

	/**
	 * 从服务器获取实例数据
	 * 
	 * @param uri
	 *            访问的uri
	 * @param params
	 *            向服务器传递的参数
	 * @param method
	 *            请求方式
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static HttpEntity getEntity(String uri, List<NameValuePair> params,
			int method) throws ClientProtocolException, IOException {
		count++;
		HttpEntity entity = null;
		// 创建客户端对象
		HttpClient client = new DefaultHttpClient();
		// 设置连接超时
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
		// 创建请求对象
		HttpUriRequest request = null;
		// 根据请求方式封装请求参数
		switch (method) {
		case METHOD_GET:
			StringBuilder sb = new StringBuilder(uri);
			if (params != null && !params.isEmpty()) {
				sb.append("?");
				buildEntity(params, sb);
			}
			// System.out.println("请求的地址："+sb.toString());
			request = new HttpGet(sb.toString());
			break;
		case METHOD_POST:
			request = new HttpPost(uri);
			if (params != null && !params.isEmpty()) {
				UrlEncodedFormEntity reqentity = new UrlEncodedFormEntity(
						params, "utf-8");
				((HttpPost) request).setEntity(reqentity);
			}
			break;
		default:
			break;
		}
		// 客户端执行请求获取响应
		HttpResponse response = client.execute(request);
		// 获取响应实体
		// 对响应进行判断
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			entity = response.getEntity();
		}
		CLog.i("info", "请求的响应" + response.getStatusLine().getStatusCode());
		CLog.i("info", "访问网络的次数" + count);
		return entity;
	}

	private static void buildEntity(List<NameValuePair> params, StringBuilder sb) {
		for (NameValuePair pair : params) {
			sb.append(pair.getName()).append("=").append(pair.getValue())
					.append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
	}

	/**
	 * 获取实例数据的长度
	 * 
	 * @param entity
	 * @return
	 */
	public static long getLength(HttpEntity entity) {
		long len = 0;
		if (entity != null) {
			len = entity.getContentLength();
		}
		return len;
	}

	/**
	 * 获取输入流
	 * 
	 * @param entity
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static InputStream getStream(HttpEntity entity)
			throws IllegalStateException, IOException {
		InputStream in = null;
		if (entity != null) {
			in = entity.getContent();
		}
		return in;
	}
}
