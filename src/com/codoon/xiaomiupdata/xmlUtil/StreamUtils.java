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
	// ����ʽ����
	public static final int METHOD_GET = 0;
	public static final int METHOD_POST = 1;
	private static int count = 0;

	/**
	 * �ӷ�������ȡʵ������
	 * 
	 * @param uri
	 *            ���ʵ�uri
	 * @param params
	 *            ����������ݵĲ���
	 * @param method
	 *            ����ʽ
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static HttpEntity getEntity(String uri, List<NameValuePair> params,
			int method) throws ClientProtocolException, IOException {
		count++;
		HttpEntity entity = null;
		// �����ͻ��˶���
		HttpClient client = new DefaultHttpClient();
		// �������ӳ�ʱ
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
		// �����������
		HttpUriRequest request = null;
		// ��������ʽ��װ�������
		switch (method) {
		case METHOD_GET:
			StringBuilder sb = new StringBuilder(uri);
			if (params != null && !params.isEmpty()) {
				sb.append("?");
				buildEntity(params, sb);
			}
			// System.out.println("����ĵ�ַ��"+sb.toString());
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
		// �ͻ���ִ�������ȡ��Ӧ
		HttpResponse response = client.execute(request);
		// ��ȡ��Ӧʵ��
		// ����Ӧ�����ж�
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			entity = response.getEntity();
		}
		CLog.i("info", "�������Ӧ" + response.getStatusLine().getStatusCode());
		CLog.i("info", "��������Ĵ���" + count);
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
	 * ��ȡʵ�����ݵĳ���
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
	 * ��ȡ������
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
