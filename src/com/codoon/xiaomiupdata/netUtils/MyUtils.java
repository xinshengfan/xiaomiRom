package com.codoon.xiaomiupdata.netUtils;

public class MyUtils {
	public static int[] stringToInts(String ids) {
		int len = ids.length();
		int[] datas = new int[len / 2];
		int j = 0;
		for (int i = 0; i < ids.length() - 1; i += 2) {
			String str = ids.substring(i, i + 2);
			System.out.print(str + " ");
			datas[j++] = Integer.parseInt(str, 16);
		}
		return datas;
	}
}
