package com.communication.data;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.communication.provider.SleepDetailDB;
import com.communication.provider.SleepDetailTB;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class SleepDataUtil {

	private Context mContext;
	private int index;
	SleepDetailDB mSleepDetailDB = null;
	public SleepDataUtil(Context mContext){
		this.mContext = mContext;
		 mSleepDetailDB = new SleepDetailDB(mContext);
	}
	
	public AccessoryValues getSleepTotal(String userid, long start, long end){
		mSleepDetailDB.open();
		List<SleepDetailTB> list = mSleepDetailDB.get(userid, start, end);
		mSleepDetailDB.close();
		String str = "";
		int i = 0;
		if(null != list && list.size() > 0){
			Log.d("enlong", "read size " + list.size());
			
			AccessoryValues values = new AccessoryValues();
			for(SleepDetailTB tb : list){
				if(tb.type == LocalDecode.DEEP_SLEEP){
					values.deepSleep += 200;
				}else if(tb.type == LocalDecode.LIGHT_SLEEP){
					values.light_sleep += 200;
				}else if(tb.type == LocalDecode.WAKE_SLEEP){
					values.wake_time += 200;
				}
				
				if(i % 3 ==0){
					str +="\n";
				}
				i ++;
				str += tb.sleepvalue + " ";
				
			}
			
			saveInfo2File(str);
			values.sleep_startTime = start;
			values.sleep_endTime = end;
			values.sleepmins += ( values.deepSleep + values.light_sleep + values.wake_time) / 60;
			values.deepSleep /= 60;
			values.light_sleep /= 60;
			values.wake_time = values.sleepmins - values.deepSleep - values.light_sleep;
			return values;
		}
		
		return null;
	}
	
	public String getSleepDetail(String userid, long start, long end){

		mSleepDetailDB.open();
		List<SleepDetailTB> list = mSleepDetailDB.get(userid, start, end);
		mSleepDetailDB.close();
		String detail = "";
		if(null != list && list.size() > 0){
			index = 0;
			
			for(SleepDetailTB tb : list){
				tb.type = Math.abs(tb.type);
			}
			do{
				combineSame(list);
			}
			while(index + 1 < list.size());
			
			for(SleepDetailTB tb : list){
				if(TextUtils.isEmpty(detail)){
					
					detail += "[" + tb.time / 1000 + "," + tb.type + "]";
				}else{
					detail += ",[" + tb.time / 1000+ "," + tb.type + "]";

				}
			}
//			saveInfo2File(detail);
			return detail;
		}
		return null;
	
	}
	
	
	public int updateSleepDetail(String userID, long start, long end, int[] detail){
		mSleepDetailDB.open();
		mSleepDetailDB.deleteBetweenTime(userID, start, end);
		if(null != detail){
			
			for(int i= 0; i< detail.length; i++){
				SleepDetailTB tb = new SleepDetailTB();
				tb.time = start + i * 200 * 1000;
				tb.sleepvalue = detail[i];
				tb.type = LocalDecode.getSleepLevelByTime(tb.sleepvalue);
				tb.userid = userID;
				mSleepDetailDB.insert(tb);
			}
		}
		mSleepDetailDB.close();
		
		return 0;
	}
	
	public void combineSame(List<SleepDetailTB> list ){
		if(index + 1 < list.size()){
			SleepDetailTB tb1 = list.get(index);
			SleepDetailTB tb2 = list.get(index +1);
			if(tb1.type == tb2.type){
				boolean result = list.remove(tb2);
			}else{
				
				index++;
			}
			
		}
	}
	
	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	public static String saveInfo2File(String info) {

//		StringBuffer sb = new StringBuffer();
//		sb.append(info);
//		try {
//			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//			long timestamp = System.currentTimeMillis();
//			String time = formatter.format(new Date());
//			String fileName = "qqhealth-sleep" + time + "-" + timestamp + ".log";
//			if (Environment.getExternalStorageState().equals(
//					Environment.MEDIA_MOUNTED)) {
//				String path = "/sdcard/qqhealth/";
//				File dir = new File(path);
//				if (!dir.exists()) {
//					dir.mkdirs();
//				}
//				FileOutputStream fos = new FileOutputStream(path + fileName);
//				fos.write(sb.toString().getBytes());
//				fos.close();
//			}
//			return fileName;
//		} catch (Exception e) {
//			Log.e("zeng", "an error occured while writing file...", e);
//		}
		return null;
	}
	
}
