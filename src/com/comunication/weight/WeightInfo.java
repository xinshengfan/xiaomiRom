package com.comunication.weight;

public class WeightInfo extends Person{
	public float fatRate;   // %
	public float weight;   //kg
	public float boneRate ; // %   bones = bone * 0.1 /weight * 100%
	public float muscleRate;  // muscl * 0.1 %
	public int fatLevel;
	public float waterRate;  // water * 0.1%
	public float BMR; // calories 
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "fatRate£º" + fatRate + " weight:" + weight
				+ " boneRate:" +  boneRate 
				+ " muscleRate:" + muscleRate
				+ " fatLevel :" + fatLevel
				+ " waterRate:" + waterRate
				+ " BMR:" + BMR;
	}
	
	
}
