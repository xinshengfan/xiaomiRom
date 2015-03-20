package com.comunication.weight;

public interface OnWeightListener {
	
	public void onGetWeightInfo(WeightInfo info);
	
	public void onGetDeiveId(int id);
	
	public Person onLoadPersonInfo();
	
	public void onTimeOut(int errinfo);
	
	public void onConnect();
}
