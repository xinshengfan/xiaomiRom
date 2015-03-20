package com.communication.provider;

public class SportTargetTB {
	public int ID;
	public String userID;
	public int targetType, targetValue;
	
	public enum eTartget{
		STEP,CALORIE,METER
	}
}
