package com.communication.provider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;

import android.content.Context;
import android.util.Log;

public class DeviceDataToDB {
	private Context mContext;
	private String TAG = "DeviceDataToDB";

	private int lastDay = 0;

	private SimpleDateFormat mDayFormat = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HHmm");
	private Hashtable<Integer, ArrayList<SportData>> mHashSport = new Hashtable<Integer, ArrayList<SportData>>();
	private ArrayList<SportData> mListSport=new ArrayList<DeviceDataToDB.SportData>();;

	private Hashtable<Integer, ArrayList<SleepData>> mHashSleep = new Hashtable<Integer, ArrayList<SleepData>>();
	private ArrayList<SleepData> mListSleep=new ArrayList<DeviceDataToDB.SleepData>();

	private static String nullData = "-1";
	private static final int timeLength = 144;
	private final int timeUnit = 10;
	static {
		for (int i = 1; i < timeLength; i++) {
			nullData += ",-1";
		}
	}

	/**
	 * 
	 * @param context
	 */
	public DeviceDataToDB(Context context) {
		mContext = context;
		lastDay=0;
		mDayFormat.setTimeZone(TimeZone.getDefault());
		mTimeFormat.setTimeZone(TimeZone.getDefault());
	}

	/**
	 * 
	 * @param step
	 * @param calorie
	 * @param meter
	 * @param time
	 */
	public void addASportData(int step, int calorie, int meter, long time) {
		int curDay = Integer.valueOf(mDayFormat.format(new Date(time)));
		int curTime = Integer.valueOf(mTimeFormat.format(new Date(time)));
		Log.d(TAG, "addASportData() step:" + step + ",calorie:" + calorie
				+ ",meter:" + meter + ",day:" + curDay + ",time:" + curTime);
		if (curDay != lastDay) {
			lastDay = curDay;
			mListSport = new ArrayList<DeviceDataToDB.SportData>();
		}
		SportData mod = new SportData();
		mod.step = step;
		mod.calories = calorie;
		mod.meters = meter;
		mod.position = getPosition(curTime);
		mListSport.add(mod);
		mHashSport.put(curDay, mListSport);
	}

	/**
	 * 
	 * @param level
	 * @param time
	 */
	public void addASleepData(int level, long time) {
		int curDay = Integer.valueOf(mDayFormat.format(new Date(time)));
		int curTime = Integer.valueOf(mTimeFormat.format(new Date(time)));
		Log.d(TAG, "addASleepData() level:" + level + ",day:" + curDay
				+ ",time:" + curTime);
		if (curDay != lastDay) {
			lastDay = curDay;
			mListSleep = new ArrayList<DeviceDataToDB.SleepData>();
		}
		SleepData mod = new SleepData();
		mod.level = level;
		mod.position = getPosition(curTime);
		mListSleep.add(mod);
		mHashSleep.put(curDay, mListSleep);
	}

	/**
	 * 
	 * @param time
	 * @return
	 */
	private int getPosition(int time) {
		Log.d(TAG, "getPostion() time:" + time);
		int position = ((time / 100) * 6) + (time % 100 / 10);
		Log.d(TAG, "getPostion() postion:" + time);
		return position;

	}

	/**
	 * save data to db
	 */
	public void save() {
		saveSportData();
		saveSleepData();
	}

	private void saveSportData() {
		if (mHashSport == null || mHashSport.size() < 1) {
			Log.d(TAG, "saveSportData() data is null");
			return;
		}

		Iterator iter = mHashSport.entrySet().iterator();
		SportsDB db = new SportsDB(mContext);
		db.open();
		while (iter.hasNext()) {
			java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
			int curDay = (Integer) entry.getKey();
			SportTB mod = db.get(KeyConstants.getUserID(), curDay);
			String steps = nullData;
			String calories = nullData;
			String meters = nullData;
			if (mod != null) {
				steps = mod.steps;
				calories = mod.calories;
				meters = mod.meters;
			}
			Log.d(TAG, "orgial steps:" + steps);
			Log.d(TAG, "orgial cal:" + calories);
			Log.d(TAG, "orgial meters:" + meters);

			if (mod == null) {
				mod = new SportTB();
				mod.curDate = curDay;
				mod.userID = KeyConstants.getUserID();
				mod.ID = -1;
			}

			mod = comNewSportData(mHashSport.get(curDay), mod, steps, calories,
					meters);
			if (mod.ID == -1) {
				db.insert(mod);
			} else {
				db.update(mod);
			}
		}
		db.close();
	}

	/**
	 * 
	 * @param mods
	 * @param steps
	 * @param calories
	 * @param meters
	 */
	private SportTB comNewSportData(ArrayList<SportData> mods,
			SportTB modSport, String steps, String calories, String meters) {
		String[] arrSteps = steps.split(",");
		String[] arrCal = calories.split(",");
		String[] arrMeters = meters.split(",");

		int size = mods.size();
		for (int i = 0; i < size; i++) {
			SportData mod = mods.get(i);

			int position = mod.position;// >timeLength?timeLength:;
			int curStep = Integer.valueOf(arrSteps[position]);
			arrSteps[position] = String
					.valueOf(curStep > 0 ? (mod.step + curStep) : mod.step);

			int curCal = Integer.valueOf(arrCal[position]);
			arrCal[position] = String
					.valueOf(curCal > 0 ? (mod.calories + curCal)
							: mod.calories);

			int curMeters = Integer.valueOf(arrMeters[position]);
			arrMeters[position] = String
					.valueOf(curMeters > 0 ? (mod.meters + curMeters)
							: mod.meters);
		}

		steps = "";
		calories = "";
		meters = "";
		modSport.totalSteps=0;
		modSport.totalCalories=0;
		modSport.totalDistance=0;
		modSport.sportDuration=0;
		
		for (int i = 0; i < timeLength; i++) {
			int curStep = Integer.valueOf(arrSteps[i]);
			if (curStep > 0) {
				modSport.totalSteps += curStep;
				modSport.sportDuration += timeUnit;
			}

			int curCal = Integer.valueOf(arrCal[i]);
			if (curCal > 0) {
				modSport.totalCalories += curCal;
			}

			int curMeters = Integer.valueOf(arrMeters[i]);
			if (curMeters > 0) {
				modSport.totalDistance += curMeters;
			}

			steps += "," + arrSteps[i];
			calories += "," + arrCal[i];
			meters += "," + arrMeters[i];
		}

		modSport.steps = steps.substring(1);
		modSport.calories = calories.substring(1);
		modSport.meters = meters.substring(1);

		Log.d(TAG, "comNewSportData steps:" + modSport.steps);
		Log.d(TAG, "comNewSportData cal:" + modSport.calories);
		Log.d(TAG, "comNewSportData meters:" + modSport.meters);

		return modSport;

	}

	/**
	 * 
	 * @param mods
	 * @param modSleep
	 * @param sleeps
	 * @return
	 */
	private SleepTB comNewSleepData(ArrayList<SleepData> mods,
			SleepTB modSleep, String sleeps) {
		String[] arrSleep = sleeps.split(",");

		int size = mods.size();
		for (int i = 0; i < size; i++) {
			SleepData mod = mods.get(i);
			int position = mod.position;// >timeLength?timeLength:
			arrSleep[position] = String.valueOf(mod.level);
		}

		sleeps = "";
		modSleep.totalminutes =0;
		modSleep.deepMinutes=0;
		modSleep.lightMinutes=0;
		modSleep.awakeminutes=0;
		for (int i = 0; i < timeLength; i++) {
			int curlevel = Integer.valueOf(arrSleep[i]);
			if (curlevel >= 0) {
				modSleep.totalminutes += timeUnit;
				if(curlevel<=KeyConstants.DEEPSLEEP_VALUE){
					modSleep.deepMinutes+=timeUnit;
				}else if(curlevel<=KeyConstants.LIGHTSLEEP_VALUE){
					modSleep.lightMinutes+=timeUnit;
				}else {
					modSleep.awakeminutes+=timeUnit;
				}
			}

			sleeps += "," + arrSleep[i];
		}

		modSleep.sleeps = sleeps.substring(1);

		Log.d(TAG, "comNewSleepData sleeps:" + modSleep.sleeps);

		return modSleep;
	}

	/**
	 * 
	 */
	private void saveSleepData() {
		if (mHashSleep == null || mHashSleep.size() < 1) {
			Log.d(TAG, "saveSleepData() data is null");
			return;
		}

		Iterator iter = mHashSleep.entrySet().iterator();
		SleepDB db = new SleepDB(mContext);
		db.open();
		while (iter.hasNext()) {
			java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
			int curDay = (Integer) entry.getKey();
			SleepTB mod = db.get(KeyConstants.getUserID(), curDay);
			String sleeps = nullData;

			if (mod != null) {
				sleeps = mod.sleeps;
			}
			Log.d(TAG, "orgial sleeps:" + sleeps);

			if (mod == null) {
				mod = new SleepTB();
				mod.curDate = curDay;
				mod.userID = KeyConstants.getUserID();
				mod.ID = -1;
			}

			mod = comNewSleepData(mHashSleep.get(curDay), mod, sleeps);
			if (mod.ID == -1) {
				db.insert(mod);
			} else {
				db.update(mod);
			}
		}
		db.close();
	}

	private class SportData {
		public int step;
		public int calories;
		public int meters;
		public int position;
	}

	private class SleepData {
		public int level;
		public int position;
	}
}
