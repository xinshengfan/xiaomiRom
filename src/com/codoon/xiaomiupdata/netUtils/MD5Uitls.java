package com.codoon.xiaomiupdata.netUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Uitls {

	public static String encode(String str) {
		// MessageDigest专门用于加密的类
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] result = messageDigest.digest(str.getBytes()); // 得到加密后的字符组数

			StringBuffer sb = new StringBuffer();
			for (byte b : result) {
				int num = b & 0xff; // 这里的是为了将原本是byte型的数向上提升为int型，从而使得原本的负数转为了正数
				String hex = Integer.toHexString(num); // 这里将int型的数直接转换成16进制表示
				// 16进制可能是为1的长度，这种情况下，需要在前面补0，
				if (hex.length() == 1) {
					sb.append(0);
				}
				sb.append(hex);
			}

			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String encode(byte[] datas) {
		// MessageDigest专门用于加密的类
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] result = messageDigest.digest(datas); // 得到加密后的字符组数

			StringBuffer sb = new StringBuffer();
			for (byte b : result) {
				int num = b & 0xff; // 这里的是为了将原本是byte型的数向上提升为int型，从而使得原本的负数转为了正数
				String hex = Integer.toHexString(num); // 这里将int型的数直接转换成16进制表示
				// 16进制可能是为1的长度，这种情况下，需要在前面补0，
				if (hex.length() == 1) {
					sb.append(0);
				}
				sb.append(hex);
			}

			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
	}

	/*
	 * 生成文件的md5校验值
	 * 
	 * @param file
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	public static String getFileMD5String(File file) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			InputStream fis;
			fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0) {
				messageDigest.update(buffer, 0, numRead);
			}
			fis.close();

			byte[] result = messageDigest.digest(); // 得到加密后的字符组数
			StringBuffer sb = new StringBuffer();
			for (byte b : result) {
				int num = b & 0x000000ff; // 这里的是为了将原本是byte型的数向上提升为int型，从而使得原本的负数转为了正数
				String hex = Integer.toHexString(num); // 这里将int型的数直接转换成16进制表示
				// 16进制可能是为1的长度，这种情况下，需要在前面补0，
				if (hex.length() == 1) {
					sb.append(0);
				}
				sb.append(hex);
			}
			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

}
