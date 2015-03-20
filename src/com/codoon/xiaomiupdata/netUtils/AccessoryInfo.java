package com.codoon.xiaomiupdata.netUtils;

import java.io.Serializable;

public class AccessoryInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String description;
	public String checksum;
	public int version_code;
	public String device_name;
	public int device_type;
	public String date;
	public String version_name;
	public long size;
	public String binary_url;
	public int popup = 0;
}
