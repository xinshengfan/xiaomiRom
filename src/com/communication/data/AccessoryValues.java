package com.communication.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AccessoryValues {
	public String time;
	public int steps, calories, distances;

	public int deepSleep, light_sleep, wake_time, sleepmins;

	public long sleep_startTime, sleep_endTime, tmpEndSleep; // 睡眠开始时间， 睡眠结束时间

	public int sport_duration; // 运动持续时间 min
	public long start_sport_time; // 运动开始时间

	public int sport_mode; // 0: codoon_accessory 1: phone_accessory

	public HashMap<Long, Integer> sleepdetail = new HashMap<Long, Integer>();
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return time + " sport_start:"
				+ format.format(new Date(start_sport_time))
				+ " sport_duration: " + sport_duration 
				+ " steps: " + steps
				+ " calories: " + calories
				+ " distances: " + distances
				+ " sleep_startTime: " + format.format(new Date(sleep_startTime))
				+ " sleep_endTime: " + format.format(new Date(sleep_endTime))
				+ " sleepmins: " + sleepmins
				+ " deepSleep: " + deepSleep
				+ " light_sleep: " + light_sleep
				+ " wake_time: " + wake_time;
	}
}
