package com.codoon.xiaomiupdata.netUtils;

import java.util.Comparator;

public class UrlParameter implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;
	public String value;

	public UrlParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public UrlParameter(String name, double value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public UrlParameter(String name, int value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public static class ComparatorUrlParameter implements Comparator {

		public int compare(Object arg0, Object arg1) {
			UrlParameter user0 = (UrlParameter) arg0;
			UrlParameter user1 = (UrlParameter) arg1;

			// sort by character

			int flag = user0.name.compareTo(user1.name);

			return flag;
		}
	}
}
