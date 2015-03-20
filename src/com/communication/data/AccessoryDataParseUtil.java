package com.communication.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.communication.provider.SleepDetailDB;
import com.communication.provider.SleepDetailTB;

public class AccessoryDataParseUtil extends LocalDecode {
	SleepDetailDB mSleepDetailDB = null;
	private int index = 0;
	private static int todaySleep;
	long startSleep = 0;
	long sleepEndtime = 0;
	public AccessoryDataParseUtil(Context context) {
		super(context);
		 mSleepDetailDB = new SleepDetailDB(mContext);
		
	}

	public HashMap<String, AccessoryValues> analysisDatas(
			ArrayList<ArrayList<Integer>> lists) {
		if (lists == null || lists.size() < 1) {
			return null;
		}
		String mStr = "";
		ArrayList<ArrayList<Integer>> newLists = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> data = new ArrayList<Integer>();

		int index = 0;

		for (ArrayList<Integer> arr : lists) {

			for (Integer i : arr) {
				data.add(i);

				mStr += i + "  ";
				index++;
				if (index == 6) {
					mStr += "\r\n";
					index = 0;
					newLists.add(data);
					data = new ArrayList<Integer>();
				}
			}
		}
		lists = null;
//		Log.i(TAG, mStr);
		SleepDataUtil.saveInfo2File(mStr);
		return _analysis(newLists);
	}

	private HashMap<String, AccessoryValues> _analysis(
			ArrayList<ArrayList<Integer>> lists) {
		HashMap<String, AccessoryValues> dataMap = new HashMap<String, AccessoryValues>();
		mSleepDetailDB.open();
		mSleepDetailDB.beginTransaction();
		int state = -1;
		startSleep = sleepEndtime = 0;
		ArrayList<Integer> lastArr = null;

		boolean isInvalid = false;
		
		
		String key = "";
		// //////////////////////////////////////////////////////////////////////////////////////
		for (ArrayList<Integer> arr : lists) {
			
			int length = arr.size();
			if (length == 6) {
				// logic whether head or not
				int FE = 0;
				int FD = 0;
				for (int i : arr) {
					if (i == 0xFE) {
						FE++;
					} else if (i == 0xFD) {
						FD++;
					}
				}

				// reset state
				// state = FE == 6 ? SPORTHEAD_STATE : FD == 6 ? SLEEPHEAD_STATE
				// : state;
				if (FE == 6) {
					state = SPORTHEAD_STATE;
				} else if (FD == 6) {
					state = SLEEPHEAD_STATE;
				}

				switch (state) {
				case SPORTHEAD_STATE:// sport head
					state = SPORTDATE_STATE;
					break;

				case SPORTDATE_STATE:// sport date
					
					curTime = getTime(arr);
					
					isInvalid = isInvalidTime(curTime);

					if (!isInvalid) {

						key = getDateString(curTime);

						AccessoryValues value = null;
						if (!dataMap.containsKey(key)) {
							value = new AccessoryValues();
							dataMap.put(key, value);
						} else {
							value = dataMap.get(key);
						}

						value.time = key;
						if (value.start_sport_time == 0) {

							value.start_sport_time = curTime;
						} else {
							value.start_sport_time = (value.start_sport_time < curTime) ? value.start_sport_time
									: curTime;
						}

						state = SPORTDATA_STATE;
					} else {
						state = INVALID_STATE;
					}
					break;

				case SPORTDATA_STATE: {// sport data
					
					
					int[] sportValues = getSportData(arr);
					filterSportResult(sportValues);
					curTime += 600000; // update time next 10 min

					AccessoryValues value = null;
					if (!dataMap.containsKey(key)) {
						value = new AccessoryValues();
						dataMap.put(key, value);
					} else {
						value = dataMap.get(key);
					}

					if(sportValues[STEP_TYPE] != 0){
						
						value.sport_duration += 10;
					}
					value.steps += sportValues[STEP_TYPE];
					value.calories += sportValues[CALORIE_TYPE];
					value.distances += sportValues[DISTANCE_TYPE];
				}
					break;

				case SLEEPHEAD_STATE:// sleep head
					state = SLEEPDATE_STATE;
					
					break;

				case SLEEPDATE_STATE:// sleep date
					
					curTime = getTime(arr);
					isInvalid = isInvalidTime(curTime);
					if (!isInvalid) {
						
						key = getDateString(curTime);
						AccessoryValues value = null;
						if (!dataMap.containsKey(key)) {
							value = new AccessoryValues();
							dataMap.put(key, value);
						} else {
							value = dataMap.get(key);
						}

						value.time = key;
						
						if(startSleep  == 0){
							 startSleep = calStartTime(curTime);
						}
						
						sleepEndtime = (sleepEndtime > curTime) ? sleepEndtime : curTime;
						sleepEndtime = calcuEndtime(sleepEndtime);
						
						state = SLEEPDATA_STATE;
						
						value.tmpEndSleep =  (value.tmpEndSleep > curTime) ? value.tmpEndSleep : curTime;  //初始化最后开始时间
						
						long realNextStart = calcuEndtime(curTime); // 原始数据的endtime
						long curLastStart  = calStartTime(value.tmpEndSleep); // 计算最后一次数据的开始
						
						value.tmpEndSleep = curTime;
						String str = "";
						while(curLastStart >= realNextStart){  // 最后一次的开始时间
							try{
								Long time = Long.valueOf(realNextStart);
								int result = -1;
								if(value.sleepdetail.containsKey(time)){
									result = value.sleepdetail.remove(time);
								}
								str +="reduce " + time + " result:" + result + "\r\n";
//								Log.d(TAG, "err data have to reduce " + time + " result:" + result);
								
								realNextStart += 200 * 1000;
								 time = Long.valueOf(realNextStart );
								if(value.sleepdetail.containsKey(time)){
									result = value.sleepdetail.remove(time);
								}
								realNextStart += 200 * 1000;
								
//								Log.d(TAG, "err data have to reduce " + time + " result:" + result);
								str +="reduce " + time + " result:" + result + "\r\n";
								
								 time = Long.valueOf(realNextStart);
								if(value.sleepdetail.containsKey(time)){
									result = value.sleepdetail.remove(time);
								}
								realNextStart += 200 * 1000;
//								Log.d(TAG, "err data have to reduce " + time + " result:" + result);
								str +="reduce " + time + " result:" + result + "\r\n";
								SleepDataUtil.saveInfo2File(str);
							}catch(Exception e){
								e.printStackTrace();
							}
							
						}
						
					} else {
						state = INVALID_STATE;
					}
					break;

				case SLEEPDATA_STATE:
					lastArr = arr;
						AccessoryValues value = null;
						key = getDateString(curTime);
						if (!dataMap.containsKey(key)) {
							value = new AccessoryValues();
							dataMap.put(key, value);
						} else {
							value = dataMap.get(key);
						}
						
						int[] sleepValues = getSportData(arr);
						String str = "";
						long  basetime = calStartTime(curTime);  //上一次的结束，作为本次的开始
						for (int i = 0; i < sleepValues.length; i++) {

							Long time = basetime  + i* 200 * 1000;
							Integer sleepvalue = 0;
							if(value.sleepdetail.containsKey(time)){
								 sleepvalue = value.sleepdetail.get(time);
							}
							str += " add time " + time;
//							sleepvalue = (sleepvalue > sleepValues[i]) ? sleepvalue : sleepValues[i];
							sleepvalue +=  sleepValues[i];
							value.sleepdetail.put(time, sleepvalue);							
//							Log.d(TAG, "put data " + time );
						}
						SleepDataUtil.saveInfo2File(str);
						value.tmpEndSleep += 600000;
						curTime += 600000;
						
						
						
						sleepEndtime = (sleepEndtime > curTime) ? sleepEndtime : curTime;
						sleepEndtime = calcuEndtime(sleepEndtime);
					break;

				default:
					break;
				}
			}
			
			
		}
		calcSleepInTransacntion(dataMap);
		
		long dividetime = calStartTime(sleepEndtime);
		insetDB(dividetime, -1);
		dividetime +=  200 * 1000;
		
		insetDB(dividetime, -1);
		dividetime +=  200 * 1000;
		
		insetDB(dividetime, -1);
		dividetime +=  200 * 1000;
		sleepEndtime = dividetime;
		
		getSleepTotalIntrans(AccessoryConfig.userID, startSleep, sleepEndtime, dataMap);
		Log.d(TAG, "parse over" );
		mSleepDetailDB.setTransactionSuccessful();
		mSleepDetailDB.endTransaction();
		mSleepDetailDB.close();
		
		return dataMap;
	}
	
	public void getSleepTotalIntrans(String userid, long start, long end, HashMap< String, AccessoryValues> data){
		List<SleepDetailTB> list = mSleepDetailDB.get(userid, start, end);
		
		if(null != list && list.size() > 0){
			int size = list.size();
			
			// 更据详情组合了不同的起始时间段
			List<AccessoryValues> values = new ArrayList<AccessoryValues>(); 
			AccessoryValues mv = new AccessoryValues();
			mv.sleep_startTime = start;
			mv.sleep_endTime = end;
			
			for(int i = 0; i< size; i++){
				SleepDetailTB tb = list.get(i);
				if(mv.sleep_startTime  == 0 && tb.time != 0){
					mv.sleep_startTime = mv.sleep_endTime = tb.time;
				}else if(tb.type != -1){
					mv.sleep_endTime = (mv.sleep_endTime < tb.time) ? tb.time : mv.sleep_endTime;
				}else if(tb.type == -1){
					mv.sleep_endTime = (mv.sleep_endTime < tb.time) ? tb.time : mv.sleep_endTime;
					if(0 != mv.sleep_startTime){
						
						values.add(mv);
						mv = new AccessoryValues();
					}
				}
			}
			
			if(values.size() == 0){
				values.add(mv);
			}
			
			String str1 = " find start begin from:" + start + " end:" + end;
			Log.d(TAG, str1);
			// 聚合起始时间段， 按照结束时间来排
			
			String str = "";
			for(int i = 0; i < values.size(); i++){
				
				mv = values.get(i);
				str += mv.sleep_startTime + " , "+ mv.sleep_endTime + " ; ";

				String key = getDateString(mv.sleep_endTime);
				AccessoryValues sd = null;
				if (!data.containsKey(key)) {
					sd = new AccessoryValues();
					data.put(key, sd);
				} else {
					sd = data.get(key);
				}
				if(sd.sleep_startTime != 0){
					
					sd.sleep_startTime =( mv.sleep_startTime < sd.sleep_startTime) ? mv.sleep_startTime : sd.sleep_startTime;
				}else{
					sd.sleep_startTime = mv.sleep_startTime;
				}
				sd.sleep_endTime = (sd.sleep_endTime > mv.sleep_endTime) ? sd.sleep_endTime : mv.sleep_endTime;
			}
//			SleepDataUtil.saveInfo2File(str);
			
			if(values.size() == 0){
				Log.e(TAG, "not find start or end");
			}
		}else{
			Log.e(TAG, "not find detail between " + start +  " end " + end);
		}
		
		return ;
	}
	

	private void calcSleepInTransacntion(HashMap<String, AccessoryValues> dataMap){
		if(null == dataMap || dataMap.size() == 0){
			return;
		}
		long systime = calcuEndtime(System.currentTimeMillis());
		Set<String> keyset = dataMap.keySet();
		Iterator<String> iter = keyset.iterator();
		Log.d(TAG, "parse begin" );
		todaySleep = 0;
		while (iter.hasNext()) {
			String keydata= iter.next();
			AccessoryValues values = dataMap.get(keydata);
		
			values.sleep_startTime =values.sleep_endTime = 0;
			
			
			long dividetime = calcuEndtime(values.start_sport_time);
			insetDB(dividetime, -1);
			dividetime +=  200 * 1000;
			insetDB(dividetime, -1);
			dividetime +=  200 * 1000;
			insetDB(dividetime, -1);
			dividetime +=  200 * 1000;
			
			Set<Long> keylong =values.sleepdetail.keySet();
			Iterator<Long> it = keylong.iterator();
			while (it.hasNext()) {
				Long st = it.next();
				if(st < systime){
					int sleepValue = values.sleepdetail.get(st);
					insetDB(st,sleepValue);
					if(isTodaySleep(st)){
						todaySleep += 200;
					}
					
				}
			}
			
		}
		
	}

	private long calcuEndtime(long time){
		int add = 600000;
		if(time % add == 0){
			add = 0;
		}
		return time / 600000 * 600000 + add;
	}
	
	
	private long calStartTime(long time){
		return time / 600000 * 600000;
	}
	
	
	public SleepDetailTB insetDB(long time,int sleepValue){
//		Log.d(TAG, " insetDB begin");
		SleepDetailTB table = null;
		table =	mSleepDetailDB.get( AccessoryConfig.userID, time);
		if(null == table){
			table = new SleepDetailTB();
			table.time = time;
			table.userid = AccessoryConfig.userID;
			table.sleepvalue  = sleepValue;
			table.type = getSleepLevelByTime(table.sleepvalue);
			
			 mSleepDetailDB.insert(table);
		}else{
			table.time = time;
			table.userid = AccessoryConfig.userID;
			if(sleepValue != -1){
				
				table.sleepvalue = (table.sleepvalue > sleepValue) ? table.sleepvalue : sleepValue;               //直接覆盖
			}else{
				table.sleepvalue  = -1;
			}
			table.type = getSleepLevelByTime(table.sleepvalue);
			
			 mSleepDetailDB.update(table);
		}
		
		return table;
	}
	
	private String getDateString(long curTime2) {
		// TODO Auto-generated method stub
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date(curTime2));
	}

	/**
	 * 0 curday_StartTime,1 curday_During,2 cur_Steps, 3 cur_Calories, 4
	 * cur_Metes, 5 total_StartTime,6 total_During, 7 total_Steps,8
	 * total_Calories,9 total_Metes,10 deepSleepValue, 11 lightSleepValue, 12
	 * wakeSleepValue, 13 sleepTotaltime, 14 cur_SleepStartTime, 15
	 * sleep_end_time, 16 total_sleep
	 **/
	public static long[] getCurrentData(HashMap<String, AccessoryValues> data_map) {
		if (null != data_map && data_map.size() >0) {
			long[] data = new long[17];
			for (int i = 0; i < data.length; i++) {
				data[i] = 0;
			}
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(
					System.currentTimeMillis()));
			if (data_map.containsKey(date)) {
				AccessoryValues today = data_map.get(date);
				data[0] = today.start_sport_time;
				data[1] = today.sport_duration;
				data[2] = today.steps;
				data[3] = today.calories;
				data[4] = today.distances;
				data[10] = today.deepSleep;
				data[11] = today.light_sleep;
				data[12] = today.wake_time;
				data[13] = todaySleep / 60;
				data[14] = today.sleep_startTime;
				data[15] = today.sleep_endTime;
			}
			Set<String> keyset = data_map.keySet();
			Iterator<String> iter = keyset.iterator();
			while (iter.hasNext()) {
				AccessoryValues values = data_map.get(iter.next());
				if (data[5] == 0) {
					data[5] = values.start_sport_time;
				} else {
					data[5] = (data[5] < values.start_sport_time) ? data[5]
							: values.start_sport_time;
				}

				data[6] += values.sport_duration;
				data[7] += values.steps;
				data[8] += values.calories;
				data[9] += values.distances;
				data[16] += values.sleepmins;
			}
			todaySleep = 0;
			return data;
		}

		return null;
	}

	
	
}
