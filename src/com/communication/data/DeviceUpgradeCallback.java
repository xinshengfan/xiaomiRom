package com.communication.data;

public interface DeviceUpgradeCallback {

	public void onChangeToBootMode();
	
	public void onGetBootVersion(String version);
	
	public void onConnectBootSuccess();
	
	public void onWriteFrame(int frame, int total);
	
	public void onCheckBootResult(boolean isSuccess);
	
	public void onTimeOut();
	
	public void onBindDivce(boolean isBind);
	
}
