package com.codoon.xiaomiupdata.netUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

import android.util.Log;

import com.sina.weibo.sdk.exception.WeiboException;

public class RequestResult {
	private int statusCode;
	private String responseAsString = null;
	private InputStream is;
	private HttpURLConnection con;

	public RequestResult(HttpURLConnection con) throws IOException {
		this.con = con;
		this.statusCode = con.getResponseCode();
		if (null == (is = con.getErrorStream())) {
			is = con.getInputStream();
		}
		if (null != is && "gzip".equals(con.getContentEncoding())) {
			// the response is gzipped
			is = new GZIPInputStream(is);
		}
	}

	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns the response stream.<br>
	 * This method cannot be called after calling asString() or asDcoument()<br>
	 * It is suggested to call disconnect() after consuming the stream.
	 * 
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body stream
	 * @throws WeiboException
	 * @see #disconnect()
	 */
	public InputStream asStream() {
		// if (streamConsumed) {
		// throw new IllegalStateException("Stream has already been consumed.");
		// }
		return is;
	}

	public byte[] asByte() throws IOException {
		byte[] buff = new byte[1024];
		int rc = 0;
		int length = 0;
		InputStream stream = asStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while ((rc = stream.read(buff, 0, 1024)) != -1) {
				baos.write(buff, 0, rc);
				length += rc;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stream.close();
		con.disconnect();
		return baos.toByteArray();
	}

	/**
	 * Returns the response body as string.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body
	 */
	public String asString() throws Exception {
		if (null == responseAsString) {
			BufferedReader br;
			try {
				InputStream stream = asStream();

				if (null == stream) {
					return null;
				}

				br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
				StringBuffer buf = new StringBuffer();
				String line;

				while (null != (line = br.readLine())) {

					if (buf != null) {
						try {
							buf.append(line);
							line = null;
							// buf.append("\n");
						} catch (Exception e) {
							// TODO: handle exception
							this.responseAsString = "";
							break;
						}

					}

				}
				this.responseAsString = buf.toString();
				buf.delete(0, buf.length());
				buf = null;
				br.close();
				br = null;
				stream.close();
				con.disconnect();
				stream = null;

				System.gc();

			} catch (NullPointerException npe) {
				// don't remember in which case npe can be thrown

			} catch (IOException ioe) {
				Log.v("error IOException", ioe.toString());
			}
		}
		return responseAsString;
	}

	public void asFile(String filepath) throws IOException {
		File file = new File(filepath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// if (de.contentLength == 14) {
		// String content = new String(asString());
		// if (content.equals("authorize fail")) {
		// content =
		// "<?xml version=\"1.0\" encoding=\"utf-8\" ?><rsp stat=\"fail\"><err msg=\"authorize fail\" code=\"002\" detail=\"authorize fail\" /></rsp>";
		// de.contentLength = content.length();
		// de.content = content.getBytes();
		// }
		// }
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(asByte());
		fos.flush();
		fos.close();
		fos = null;
		file = null;
	}

	public void DisConnect() {
		con.disconnect();
	}
}
