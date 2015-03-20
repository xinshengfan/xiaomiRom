package com.codoon.xiaomiupdata.xmlUtil;

/**
 * <version>5</version> <date>2014-10-31 16:33:59</date>
 * <version_name>1.0</version_name> <description>1.0版本：; ·发现周围的帅哥美女，带手环找到好友
 * ·国内第一手环方案提供者，大幅度提高计步、睡眠的准确度 ·连接【咕咚】app，与两千万运动爱好者一起运动;9.4M</description>
 * <app_name>codoon_rom.apk</app_name> <size>9.4M</size>
 * <app_url>http://slbpkg.codoon.com/app/android/codoon_rom.apk</app_url>
 * 
 * @author FANFAN
 * 
 */
public class StateInfo {
	private int version;
	private String data_time;
	private String version_name;
	private String description;
	private String app_name;
	private String size;
	private String app_url;

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getData_time() {
		return data_time;
	}

	public void setData_time(String data_time) {
		this.data_time = data_time;
	}

	public String getVersion_name() {
		return version_name;
	}

	public void setVersion_name(String version_name) {
		this.version_name = version_name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApp_name() {
		return app_name;
	}

	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}

	public String getApp_url() {
		return app_url;
	}

	public void setApp_url(String app_url) {
		this.app_url = app_url;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "version=" + version + ", data_time=" + data_time
				+ ", version_name=" + version_name + ", description="
				+ description + ", app_name=" + app_name + ", size=" + size
				+ ", app_url=";
	}

}
