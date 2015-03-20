package com.comunication.weight;


public enum WeightScaleType {
	FAT(0xCF), WEIGHT(0xCE), BABAY(0xCB), KITCHEN(0xCA), NONE(0);

	WeightScaleType(int type) {
		
	}
	
	public static WeightScaleType getValue(int type_id) {
		WeightScaleType tmptype = WeightScaleType.NONE;
		switch (type_id) {
		case 0xCF:
			tmptype = FAT;
		break;
		case 0xCE:
			tmptype = WEIGHT;
		break;
		case 0xCB:
			tmptype = BABAY;
		break;
		case 0xCA:
			tmptype = KITCHEN;
		break;
		default:
			tmptype = NONE;
			break;
		}
		return tmptype;
	}
	
	
	public static float getResolutionByType(WeightScaleType type){
		float resolution = 0.1f;
		switch (type) {
		case FAT:
		case WEIGHT:
			resolution = 0.1f;
		break;
		case BABAY:
			resolution = 0.01f;
		break;
		case KITCHEN:
			resolution = 0.001f;
		break;
		default:
			
			break;
		}
		
		return resolution;
		
	}
}
