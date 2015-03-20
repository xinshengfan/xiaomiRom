package com.communication.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.Size;
import android.text.TextUtils;
import android.util.Log;

import com.communication.data.SaveManager.eSaveType;
import com.communication.mancherster.FileManager;

public class LocalDecode {

	protected String TAG = "LocalDecode";

	protected final int STEP_TYPE = 0, CALORIE_TYPE = 1, DISTANCE_TYPE = 2;

	protected Context mContext;

	// position of array
	public final int STEP = 0, CALORIE = 1, DISTANCE = 2, SLEEPTIME = 3;

	public final static int DEEP_SLEEP = 3;
	public final static int LIGHT_SLEEP = 2;
	public final static int WAKE_SLEEP = 1;

	// 200s 时间内，活动睡眠阈值
	public final static int DEEP_Threshold = 4;
	public final static int LIGHT_Threshold = 17;

	protected static final int SPORTHEAD_STATE = 0;

	protected static final int SPORTDATE_STATE = 1;

	protected static final int SPORTDATA_STATE = 2;

	protected static final int SLEEPHEAD_STATE = 3;

	protected static final int SLEEPDATE_STATE = 4;

	protected static final int SLEEPDATA_STATE = 5;

	protected static final int INVALID_STATE = -1;

	// protected SaveManager mXml;

	protected SimpleDateFormat mFormat;

	protected Calendar mCalendar = Calendar.getInstance();

	protected final String PRE_NAME = "MyPrefsFile";
	protected String KEY_PRE_SLEEP = "lastSleepTime";
	protected final String KEY_BIND_PRODUCT_ID = "BindTypeId";

	public LocalDecode(Context context, eSaveType saveType) {
		mContext = context;
		// mXml = new SaveManager(mContext, saveType);
		mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		mFormat.setTimeZone(TimeZone.getDefault());

	}

	public LocalDecode(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		// mXml = new SaveManager(mContext, saveType);
		mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		mFormat.setTimeZone(TimeZone.getDefault());
	}

	public int _singleInt(int b) {
		return b;
	}

	public int _doubleInt(int bhigh, int blow, int type) {
		// if (bhigh >= 0xFD || blow >= 0xFD) {
		// return 0;
		// }
		int result = blow + ((bhigh << 8) & 0xFF00);

		// switch (type) {
		// case STEP_TYPE:
		// if (result > 6000)
		// return 0;
		// break;
		//
		// case CALORIE_TYPE:
		// if (result > 800)
		// return 0;
		// break;
		//
		// case DISTANCE_TYPE:
		// if (result > 3000)
		// return 0;
		// break;
		//
		// default:
		// break;
		// }
		return result;// blow + ((bhigh << 8) & 0xFF00);
	}

	/**
	 * 
	 * @param lists
	 * @return
	 */
	public long[] analysis(ArrayList<ArrayList<Integer>> lists) {
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
		// writeSD(mStr);
		Log.i(TAG, mStr);
		// return analysisResult(newLists);
		return analysisR(newLists);
	}

	/**
	 * 
	 * @param content
	 */
	protected void writeSD(String content) {
		FileManager fileManager = new FileManager();
		fileManager.saveContentSD(mContext, "hjack", content);
	}

	long curTime;

	/**
	 * the order of data: curday_StartTime, curday_During, cur_Steps,
	 * cur_Calories, cur_Metes, total_StartTime, total_During, total_Steps,
	 * total_Calories, total_Metes,deepSleepValue, lightSleepValue,
	 * wakeSleepValue,sleepTotaltime, endTime
	 * 
	 * @param lists
	 * @return
	 */
	protected long[] analysisR(ArrayList<ArrayList<Integer>> lists) {

		long total_Steps = 0;
		long total_Calories = 0;
		long total_Metes = 0;
		long total_During = 0;

		long cur_Steps = 0;
		long cur_Calories = 0;
		long cur_Metes = 0;
		long curday_During = 0;

		long curday_StartTime = 0;

		boolean isCurday = false;
		boolean isCurSleepDay = false;
		long total_StartTime = 0;
		long sleep_end_time = 0;
		long sleep_start_time = 0;
		long sleepTotaltime = 0;

		int state = -1;

		long lastTime = 0;

		long cur_SleepStartTime = 0, cur_SleepEndTime = 0;

		int deepSleepValue = 0;
		int lightSleepValue = 0;
		int wakeSleepValue = 0;
		// UserBaseInfo info =
		// UserData.GetInstance(mActivity).GetUserBaseInfo();
		SharedPreferences mSharedPreferences = mContext.getSharedPreferences(
				PRE_NAME, 0);
		String last_sleepKey = KEY_PRE_SLEEP.concat(mSharedPreferences
				.getString(KEY_BIND_PRODUCT_ID, "_00")); // yong id zuowei key
		lastTime = mSharedPreferences.getLong(last_sleepKey, 0);

		Editor editor = mSharedPreferences.edit();

		boolean isInvalid = false;

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
					cur_SleepStartTime = 0;
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
						if (total_StartTime == 0) {
							total_StartTime = curTime;
						}
						total_StartTime = (total_StartTime < curTime) ? total_StartTime
								: curTime;

						if (isToday(curTime)) {// today
							isCurday = true;
							if (curday_StartTime == 0) {

								curday_StartTime = curTime;
							} else {
								curday_StartTime = (curday_StartTime < curTime) ? curday_StartTime
										: curTime;
							}
						} else {
							// if(curday_StartTime < curTime){ //not the same
							// day, and curTime > lastTime so it is a new day
							//
							// curday_StartTime = curTime;
							//
							// Log.i(TAG, "begin a new day" );
							// }else{
							//
							// }
							isCurday = false;
						}
						state = SPORTDATA_STATE;
					} else {
						state = INVALID_STATE;
					}

					break;

				case SPORTDATA_STATE:// sport data

					int[] sportValues = getSportData(arr);
					filterSportResult(sportValues);
					total_Steps += sportValues[STEP_TYPE];
					total_Calories += sportValues[CALORIE_TYPE];
					total_Metes += sportValues[DISTANCE_TYPE];
					total_During += 600000;

					if (isCurday) {

						curday_During += 10;
						cur_Steps += sportValues[STEP_TYPE];
						cur_Calories += sportValues[CALORIE_TYPE];
						cur_Metes += sportValues[DISTANCE_TYPE];
						sleep_end_time = curTime + 600000;
					}

					curTime += 600000; // update time next 10 min
					sleep_end_time = curTime;

					break;

				case SLEEPHEAD_STATE:// sleep head
					state = SLEEPDATE_STATE;
					break;

				case SLEEPDATE_STATE:// sleep date
					curTime = getTime(arr);
					isInvalid = isInvalidTime(curTime);
					lastTime = mSharedPreferences.getLong(last_sleepKey, 0);
					if (!isInvalid) {

						state = SLEEPDATA_STATE;

						if (isTodaySleep(curTime)) {// same day
							isCurSleepDay = true;
							if (cur_SleepStartTime == 0) {

								cur_SleepStartTime = curTime;
							}

							cur_SleepStartTime = (cur_SleepStartTime < curTime) ? cur_SleepStartTime
									: curTime;

						} else {
							// if(cur_SleepStartTime < curTime){ //not the same
							// day, and curTime > lastTime so it is a new day
							//
							// cur_SleepStartTime = curTime;
							// Log.d(TAG, "begin a new sleep day" );
							// }else{
							//
							// }
							//
							isCurSleepDay = false;

						}

					} else {
						state = INVALID_STATE;
					}
					break;

				case SLEEPDATA_STATE:
					if (isTodaySleep(curTime)) {

						if (lastTime / 600000 + 1 <= curTime / 600000) { // 在记录的上次睡眠时间的10分钟后(同一个10
																			// min内去重)

							int[] sleepValues = getSportData(arr);
							for (int i = 0; i < sleepValues.length; i++) {

								int level = getSleepLevelByTime(sleepValues[i]);

								if (level == DEEP_SLEEP) {
									deepSleepValue += 200;
								} else if (level == LIGHT_SLEEP) {
									lightSleepValue += 200;
								} else {
									wakeSleepValue += 200;
								}
							}
							sleepTotaltime += 10;
							cur_SleepEndTime = curTime + 600000;

						} else {
							Log.e(TAG, "time has calculated");

							Log.i(TAG,
									"last sleep time is:"
											+ mFormat
													.format(new Date(lastTime))
											+ " and cur:"
											+ mFormat.format(new Date(curTime)));
						}

						editor.putLong(last_sleepKey, curTime);
						editor.commit();
					}

					curTime += 600000;
					isCurSleepDay = isTodaySleep(curTime);

					break;

				default:
					break;
				}
			}
		}

		// change seconds to min
		deepSleepValue = deepSleepValue / 60;
		lightSleepValue = lightSleepValue / 60;
		wakeSleepValue = (int) (sleepTotaltime - deepSleepValue - lightSleepValue);

		// sleepTotaltime = deepSleepValue + lightSleepValue + wakeSleepValue;

		Log.d(TAG, "current day steps:" + cur_Steps + ", calories:"
				+ cur_Calories + ", distances:" + cur_Metes
				+ ", sleepTotaltime:" + sleepTotaltime + " deepSleepValue:"
				+ deepSleepValue + " lightSleepValue:" + lightSleepValue
				+ " wakeSleepValue " + wakeSleepValue);

		return new long[] { curday_StartTime, curday_During, cur_Steps,
				cur_Calories, cur_Metes, total_StartTime, total_During,
				total_Steps, total_Calories, total_Metes, deepSleepValue,
				lightSleepValue, wakeSleepValue, sleepTotaltime,
				cur_SleepStartTime, sleep_end_time };
	}

	/**
	 * check time is curDay or not
	 * 
	 * @param curTime2
	 * @return
	 */
	protected boolean isToday(long curTime2) {
		// TODO Auto-generated method stub
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String des = format.format(new Date(curTime2));
		String systemDay = format.format(new Date(System.currentTimeMillis()));

		Log.d(TAG, "des:  " + des + " systemDay:" + systemDay);

		return systemDay.equals(des);
	}

	/**
	 * check time is curSleepday or not
	 * 
	 * @param curTime2
	 * @return
	 */
	protected boolean isTodaySleep(long curTime2) {
		// TODO Auto-generated method stub

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String des = format.format(new Date(curTime2));

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getDefault());

		int hour_now = calendar.get(Calendar.AM_PM); // time_now

		int year = calendar.get(Calendar.YEAR);
		int mounth = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		if (hour_now == Calendar.AM) {
			// Log.d(TAG, "systime is am");

			calendar.add(Calendar.DATE, -1);

		} else if (Calendar.PM == hour_now) {

		}

		year = calendar.get(Calendar.YEAR);
		mounth = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, mounth, day, 12, 0);

		String src = format.format(calendar.getTime());

		calendar.add(Calendar.DATE, 1);
		year = calendar.get(Calendar.YEAR);
		mounth = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, mounth, day, 12, 0);

		String src2 = format.format(calendar.getTime());

		boolean isSleepToday = (des.compareToIgnoreCase(src) >= 0 && des
				.compareToIgnoreCase(src2) < 0) ? true : false;

//		Log.d(TAG, "base time:" + src + " curtime:" + des + " isTodaySleep"
//				+ isSleepToday);
		return isSleepToday;
	}

	/**
	 * 
	 * @param arr
	 * @return
	 */
	protected int[] getSportData(ArrayList<Integer> arr) {
		int[] result = new int[3];
		int a1 = arr.get(0);
		int a2 = arr.get(1);
		if (a1 != 0 || a2 != 0) {
			result[STEP_TYPE] = _doubleInt(a1, a2, STEP_TYPE);
		}

		a1 = arr.get(2);
		a2 = arr.get(3);
		if (a1 != 0 || a2 != 0) {
			result[CALORIE_TYPE] = _doubleInt(a1, a2, CALORIE_TYPE);
		}

		a1 = arr.get(4);
		a2 = arr.get(5);
		if (a1 != 0 || a2 != 0) {
			result[DISTANCE_TYPE] = _doubleInt(a1, a2, DISTANCE_TYPE);
		}

		return result;
	}

	/**
	 * IOS model + (BOOL) isSportsItemValid: (NSDictionary *)sportsItem{ int
	 * steps = [sportsItem[@"steps"] intValue]; int meters =
	 * [sportsItem[@"meters"] intValue]; float calories =
	 * [sportsItem[@"calories"] floatValue]; if (isDeviceCandy) { if (steps > 0)
	 * { if (calories >= steps) { return NO; }else if (((calories*10) == steps)
	 * && steps==meters){ return NO; } }else{ if (calories>steps) { return NO; }
	 * } }
	 * 
	 * if (steps > 3000) { return NO; }else if(steps == 0 && meters >= 2){
	 * return NO; }else if (steps * 1.8 < meters){ return NO; } return YES; }
	 * 
	 * @param result
	 */

	protected void filterSportResult(int[] result) {
		if (null == result || result.length < 3) {
			return;
		}

		SharedPreferences mPres = mContext.getSharedPreferences(PRE_NAME, 0);
		String deviceName = mPres.getString("BindTypeName", "");
		if (null != deviceName && deviceName.equals("CANDY")) { // 咕咚糖果
			if (result[STEP_TYPE] > 0) {
				if (result[CALORIE_TYPE] > result[STEP_TYPE]) {
					result[CALORIE_TYPE] = 0;
					result[STEP_TYPE] = 0;
					result[DISTANCE_TYPE] = 0;
				} else if ((result[CALORIE_TYPE] * 10 == result[STEP_TYPE])
						&& (result[STEP_TYPE] == result[DISTANCE_TYPE])) {
					result[CALORIE_TYPE] = 0;
					result[STEP_TYPE] = 0;
					result[DISTANCE_TYPE] = 0;
				}
			} else {
				if (result[CALORIE_TYPE] > result[STEP_TYPE]) {
					result[CALORIE_TYPE] = 0;
					result[STEP_TYPE] = 0;
					result[DISTANCE_TYPE] = 0;
				}
			}
		}

		if (result[STEP_TYPE] > 3000) {
			result[CALORIE_TYPE] = 0;
			result[STEP_TYPE] = 0;
			result[DISTANCE_TYPE] = 0;

		} else if (result[STEP_TYPE] == 0 && result[DISTANCE_TYPE] >= 2) {
			result[STEP_TYPE] = 0;
			result[CALORIE_TYPE] = 0;
			result[DISTANCE_TYPE] = 0;

		} else if ((result[STEP_TYPE] * 1.8) < result[DISTANCE_TYPE]) {
			result[STEP_TYPE] = 0;
			result[CALORIE_TYPE] = 0;
			result[DISTANCE_TYPE] = 0;
		}

	}

	/**
	 * 
	 * @return
	 */
	protected long getSystemTime() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getDefault());

		int minute = cal.get(Calendar.MINUTE);
		if (minute % 10 > 0) {
			minute += (10 - minute % 10);
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
				minute);

		Log.d("debug",
				"system time is " + cal.get(Calendar.YEAR) + "-"
						+ cal.get(Calendar.MONTH) + "-"
						+ cal.get(Calendar.DAY_OF_MONTH) + "-"
						+ cal.get(Calendar.HOUR_OF_DAY) + "-"
						+ cal.get(Calendar.MINUTE));
		return (cal.getTimeInMillis() / 600000) * 10;
	}

	/**
	 * 
	 * @param list
	 * @param isEnd
	 * @return
	 */
	protected long createTime(ArrayList<Integer> list, boolean isEnd) {
		int year = Integer.parseInt(Integer.toHexString(list.get(0))) * 100
				+ Integer.parseInt(Integer.toHexString(list.get(1)));
		int month = Integer.parseInt(Integer.toHexString(list.get(2)));
		int day = Integer.parseInt(Integer.toHexString(list.get(3)));
		int hour = Integer.parseInt(Integer.toHexString(list.get(4)));

		int iMin = Integer.parseInt(Integer.toHexString(list.get(5)));
		if (isEnd && iMin % 10 > 0) {
			iMin = ((iMin / 10) + 1) * 10;
		}
		int minute = (iMin / 10) * 10;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getDefault());
		calendar.set(year, month, day, hour, minute);

		Log.d("debug", "time is " + year + "-" + month + "-" + day + "-" + hour
				+ "-" + minute);

		return (calendar.getTimeInMillis() / 600000) * 10;
	}

	/**
	 * decode time by list data, -1 is err time
	 * 
	 * @param list
	 * @return
	 */
	protected long getTime(ArrayList<Integer> list) {
		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int iMin = 0;
		try {

			year = Integer.parseInt(Integer.toHexString(list.get(0))) * 100
					+ Integer.parseInt(Integer.toHexString(list.get(1)));
			month = Integer.parseInt(Integer.toHexString(list.get(2))) - 1;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return -1;
		}

		try {
			day = Integer.parseInt(Integer.toHexString(list.get(3)));
			hour = Integer.parseInt(Integer.toHexString(list.get(4)));

			iMin = Integer.parseInt(Integer.toHexString(list.get(5)));
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return -1;
		}
		mCalendar.setTimeZone(TimeZone.getDefault());
		mCalendar.set(year, month, day, hour, iMin);
		return mCalendar.getTimeInMillis();
	}

	/**
	 * get the SleepLevel(deep light wake) by sleepValue
	 * 
	 * @param time
	 * @return
	 */
	public static int getSleepLevelByTime(int time) {
		int level = -1;
		if (time < 0) {
			level = -1;
		} else if (time < DEEP_Threshold) {
			level = DEEP_SLEEP;
		} else if (time < LIGHT_Threshold) {
			level = LIGHT_SLEEP;
		} else {
			level = WAKE_SLEEP;
		}
		return level;
	}

	
	/**
	 * check time isInvalid <0 true
	 * 
	 * @param time
	 * @return
	 */
	protected boolean isInvalidTime(long time) {
		return (time < 0) ? true : false;
	}
}
