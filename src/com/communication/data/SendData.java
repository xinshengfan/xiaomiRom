package com.communication.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.comunication.weight.Person;

import android.util.Log;

public class SendData {

	private static String TAG="SendData";
	
	
	 /**
     *0. mmand of bind the device
     * 
     * @return
     */
	public static int[] getPostBindOrder(){
		return new int[]{ 0xAA, 0x41, 0x00, 0xEB};
	}
	
    /**
     *1. command of connection the device
     * 
     * @return
     */
    public static int[] getPostConnection() {
        return new int[] { 0xAA, 0x01, 0x0, 0xAB };
    }

    /** 
     *2. command of get device type and version
     * 
     * @return
     */
    public static int[] getPostDeviceTypeVersion() {
        return new int[] { 0xAA, 0x02, 0x00, 0xAC };
    }

    /**
     *3. command of write device id ------------
     * @param type deviceID
     * @return
     */
    public static int[] getPostWriteDeviceID(int type, int[] deviceID) {
        int result = 0;
        int[] outData = new int[4 + deviceID.length + 1];
        outData[0] = 0xAA;
        outData[1] = 0x03;
        outData[2] = 0x0D;
        outData[3] = type;
        result += 0xAA + 0x03 + 0x0D + type;
        for (int i = 0; i < deviceID.length; i++) {
            outData[i + 4] = deviceID[i];
            result += deviceID[i];
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }

    /**
     *4. command of get device ID
     * 
     * @return
     */
    public static int[] getPostDeviceID() {
        return new int[] { 0xAA, 0x04, 0x00, 0xAE };
    }

    /**
     * command of update SportInfo(target)
     * @param sportinfo
     * see {@link getPostUpdateUserInfo}
     * 5. update user info
     */
    public static int[] getPostUpdateSportInfo(int[] sportinfo) {
        int result = 0;
        int[] outData = new int[3 + sportinfo.length + 1];
        outData[0] = 0xAA;
        outData[1] = 0x05;
        outData[2] = 0x0E;
        result += 0xAA + 0x05 + 0x0E;
        for (int i = 0; i < sportinfo.length; i++) {
            outData[i + 3] = sportinfo[i];
            result += sportinfo[i];
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }
    
    /**
     * *@param sportinfo
     * see {@link getPostUpdateUserInfo}
     * @param userInfo
     * @return
     */
    private static int[] getPostUpdateUserInfoAll(int[] userInfo){
        int result = 0;
        int[] outData = new int[3 + userInfo.length + 1];
        outData[0] = 0xAA;
        outData[1] = 0x05;
        outData[2] = 0x0E;
        result += 0xAA + 0x05 + 0x0E;
        for (int i = 0; i < userInfo.length; i++) {
            outData[i + 3] = userInfo[i];
            result += userInfo[i];
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }
    /**
     * @param sportinfo
     * see {@link getPostUpdateUserInfo}
     * 5. update user info
     */
    private static int[] getPostUpdateSportInfo(ArrayList<Integer> sportinfo) {
        int result = 0;
        int[] outData = new int[ sportinfo.size()];
        outData[0] = 0xAA;
        outData[1] = 0x05;
        outData[2] = 0x0E;
        result += 0xAA + 0x05 + 0x0E;
        int length=sportinfo.size()-1;
        for (int i = 3; i < length; i++) {
            result+= outData[i] = sportinfo.get(i);
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }

    /**
     *6. update user info ------------
     * 
     * @return
     */
    public static int[] getPostUpdateUserAlarm(int[] alarmInfo) {
        int result = 0;
        int[] outData = new int[3 + alarmInfo.length + 1];
        outData[0] = 0xAA;
        outData[1] = 0x06;
        outData[2] = 0x0A;
        result += 0xAA + 0x06 + 0x0A;
        for (int i = 0; i < alarmInfo.length; i++) {
            outData[i + 3] = alarmInfo[i];
            result += alarmInfo[i];
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }
    
    /**
     *6. update user info ------------
     * 
     * @return
     */
    public static int[] getPostUpdateUser2(ArrayList<Integer> userinfo) {
        int result = 0;
        int[] outData = new int[userinfo.size()];
        outData[0] = 0xAA;
        outData[1] = 0x06;
        outData[2] = 0x0A;
        result += 0xAA + 0x06 + 0x0A;
        int length=userinfo.size()-1;
        for (int i = 3; i < length; i++) {
            result+= outData[i] = userinfo.get(i);
        }
        outData[outData.length - 1] = result & 0xFF;
        return outData;
    }
    
	/**
	 * 6. update user info ------------
	 * 
	 * @return
	 */
	public static int[] getPostUpdateUser2(int[] userinfo) {
		int result = 0;
		int[] outData = new int[userinfo.length + 4];
		outData[0] = 0xAA;
		outData[1] = 0x06;
		outData[2] = 0x0D;
		result += 0xAA + 0x06 + 0x0D;
		int length = userinfo.length;
		for (int i = 0; i < length; i++) {
			result += outData[i + 3] = userinfo[i];
		}
		outData[outData.length - 1] = result & 0xFF;
		return outData;
	}

    /**
     * 7. get user info
     * 
     * @return
     */
    public static int[] getPostGetUserInfo() {
        return new int[] { 0xAA, 0x07, 0x00, 0xB1 };
    }

    /**
     *8. get device battery
     * 
     * @return
     */
    public static int[] getPostGetUserInfo2() {
        return new int[] { 0xAA, 0x08, 0x00, 0xB2 };
    }

    /**
     * 10. update time
     * @param currentTime
     */
    public static int[] getPostSyncTime(long currentTime) {
        int result = 0;
        int[] outData = new int[11];
        outData[0] = 0xAA;
        outData[1] = 0x0A;
        outData[2] = 0x07;
        result += 0xAA + 0x07 + 0x0A;

        SimpleDateFormat format = new SimpleDateFormat("yy MM dd HH mm ss");
        format.setTimeZone(TimeZone.getDefault());
        Date date=new Date(currentTime);
        String time = format.format(date);
         
        String[] times = time.split(" ");

        for (int i = 0; i < 6; i++) {
            outData[i + 3] = Integer.valueOf(times[i], 16);
            result += outData[i + 3];
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        outData[9] = Integer.valueOf(cal.get(Calendar.DAY_OF_WEEK));
        outData[outData.length - 1] = (outData[9] + result) & 0xFF;
        return outData;
    }

    /**
     * 11. get device time
     */
    public static int[] getPostDeviceTime() {
        return new int[] { 0xAA, 0x0B, 0x0, 0xB5 };
    }

    /**
     *12. get device frame count`
     * 
     * @return
     */
    public static int[] getPostSyncDataByFrame() {
        return new int[] { 0xAA, 0x0C, 0x0, 0xB6 };
    }
    
    /**
     * get the data of frame 
     * @param frame
     * @return
     */
    public static int[] getPostReadSportData(int frame){
        Log.d(TAG, "Frame:"+frame);
        int high=frame >> 8;
        int low=frame & 0xFF;
        return new int[]{0xAA, 0x11, 0x02, high, low, (0xBD + high + low)&0xFF};
    }

    /**
     * 
     * 20.command clear sport data
     */
    public static int[] getPostClearSportData() {
        return new int[] { 0xAA, 0x14, 0, 0xBE };
    }
    
    /**
     *command of update of userinfo
     * @param height
     * @param weight
     * @param age
     * @param gender
     * @param stepLength
     * @param runLength
     * @param sportType   ( STEP_TYPE = 0, CALORIE_TYPE = 1, DISTANCE_TYPE = 2)
     * @param goalValue
     * @return
     */
    public int[] getPostUpdateUserInfo(int height,int weight,int age ,int gender,int stepLength,int runLength,int sportType,
    		int goalValue) {
        // mMainActivityClass.TARGET_VALUE;
        int[] userinfo = new int[14];
        // height
        userinfo[0] = height;
        // weight
        userinfo[1] = weight;
        // age
        userinfo[2] = age;
        // sex
        userinfo[3] =  gender;
        // walk length
        userinfo[4] = stepLength;
        // run length
        userinfo[5] =runLength;
        // keep
        userinfo[6] = userinfo[4];
        // keep
        userinfo[7] = 0;
        // goal
        userinfo[8] = sportType;
        // goal value
        userinfo[9] = goalValue >> 8;
        userinfo[10] =goalValue & 0xFF;

        userinfo[11] = 0x2;
        // density
        userinfo[12] = 0;
        // time
        userinfo[13] = 0;

        return getPostUpdateUserInfoAll(userinfo);

    }
    
    /**
     * 
     * @param startActivityTimeBCD   the start time of action BCD_X
     * @param endActivityTimeBCD       the end time of action
     * @param remindSpaceMin            the space time of action remind
     * @param everyDayRemindOnOff      the turn on or off everday remind:" 0001111" week:" 7654321"  week7 6 is off
     * @param alarmClockHourBCD        the smart clock hour
     * @param alarmClockMinBCD         the smart clock minute
     * @param aheadOfCheckMin          the preAlarm minute
     * @param everyDayAlarmClockOnOff       the turn on or off everday remind:" 0001111" week:" 7654321"  week7 6 is off
     * @param everyAlarmClockAheadOfCheckTurn  always set 0x7f
     * @return
     */
    public int[] getPostUpateAlarmInfo(int startActivityTimeBCD,
			int endActivityTimeBCD, int remindSpaceMin, int everyDayRemindOnOff,
			int alarmClockHourBCD, int alarmClockMinBCD, int aheadOfCheckMin,
			int everyDayAlarmClockOnOff, int everyAlarmClockAheadOfCheckTurn){
    	int[] datas = new int[14];
    	
    	datas[0] = startActivityTimeBCD;
		datas[1] = endActivityTimeBCD;
		datas[2] = remindSpaceMin;
		datas[3] = everyDayRemindOnOff;
		// datas[4]=(everyDayTurn==0)?0:1;
		if (everyDayRemindOnOff == 0) {
			datas[4] = 0;
		} else {
			datas[4] = 0x7F;
		}
		// smart alarm
		datas[5] = alarmClockHourBCD;
		datas[6] = alarmClockMinBCD;
		datas[7] = aheadOfCheckMin;
		datas[8] = everyDayAlarmClockOnOff;
		datas[9] = everyAlarmClockAheadOfCheckTurn;
		// datas[10]=everyAlarmClockTurn==0?0:1;
		if (everyDayAlarmClockOnOff == 0) {
			datas[10] = 0;
		} else {
			datas[10] = 0x7F;
		}

		// other
		datas[11] = 0;
		datas[12] = 0;
    	return getPostUpdateUserAlarm(datas);
    }
    
    
    /**
     * 转换到boot模式
     * @return
     */
    public static int[] postBootMode(){
    	 int result = 0;
         int[] outData = new int[4];
         outData[0] = 0xAA;
         outData[1] = 0x70;
         outData[2] = 0x00;
         result += 0xAA + 0x70 + 0x00;
         outData[3] = result & 0x00FF;
         return outData;
    }
    
    /**
     * 连接到boot模式下的loader 
     * @return
     */
    public static int[] postConnectBootOrder(){
    	 int result = 0;
         int[] outData = new int[4];
         outData[0] = 0xAA;
         outData[1] = 0x71;
         outData[2] = 0x00;
         result += 0xAA + 0x71 + 0x00;
         outData[3] = result & 0x00FF;
         return outData;
    }
    
    

    /**
     * 连接到boot模式下的Version 
     * @return
     */
    public static int[] postConnectBootVersion(){
    	 int result = 0;
         int[] outData = new int[4];
         outData[0] = 0xAA;
         outData[1] = 0x72;
         outData[2] = 0x00;
         result += 0xAA + 0x72 + 0x00;
         outData[3] = result & 0x00FF;
         return outData;
    }
    
    /**
     * 传递升级的内容
     * @param data
     * @return
     */
    public static int[] postBootUploadData(int index, byte[] data){
    	 int result = 0;
         int[] outData = new int[6 + data.length];
         outData[0] = 0xAA;
         outData[1] = 0x73;
         outData[2] = 0x0e;
         outData[3] = index >> 8;
         outData[4] = index & 0x00FF;
         
         for(int i = 0; i < data.length; i++){
        	 outData[i + 5] = data[i] & 0x000000ff;
         }
         String str = "";
         for(int i =0; i < outData.length -1; i++){
        	 result += outData[i];
         }
         result &= 0x000000ff;
         outData[outData.length -1] = result;
         
         for(int i =0; i < outData.length; i++){
        	 result += outData[i];
        	 str += "," + Integer.toHexString(outData[i]);
         }
         
         Log.d("CodoonDeviceUpgradeManager", str + "   lenth:"+ outData.length);
         return outData; 
    }
    
    /**
     * 传递升级的内容
     * @param data
     * @return
     */
    public static int[] postBootUploadData(int index, byte[] data, int length){
    	 int result = 0;
         int[] outData = new int[6 + length];
         outData[0] = 0xAA;
         outData[1] = 0x73;
         outData[2] = 0x02 + length;
         outData[3] = (index >> 8) & 0xff;
         outData[4] = index & 0x00FF;
         
         for(int i = 0; i < length; i++){
        	 outData[i + 5] = data[i] & 0x000000ff;
         }
         for(int i =0; i < outData.length -1; i++){
        	 result += outData[i];
         }
         result &= 0x000000ff;
         outData[outData.length -1] = result;
         
         DataUtil.DebugPrint(outData);
         return outData; 
    }
    
    /**
     * 下发校验和
     * @param checkData
     * @return
     */
    public static int[] postBootEnd(int checkData){
        int[] outData = new int[6];
        outData[0] = 0xAA;
        outData[1] = 0x74;
        outData[2] = 0x02;
        outData[3] = checkData >> 8;
        outData[4] = checkData & 0x000000FF;
        for(int i = 0; i < outData.length -1; i++){
        	outData[5] += outData[i];
        }
        outData[5] = outData[5] & 0x000000FF;
        
        DataUtil.DebugPrint(outData);
        
        return outData;
    }
    
    
    public static int[] postWeightScaleConnect(){
    	int[] outData = new int[10];
    	outData[0] = 0x68;
    	outData[1] = 0x01;
    	outData[2] = 0x00;
   
    	for(int i = 1; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    	return outData;
    }
    
    public static int[] postWeightInfo(Person person){
    	if(null == person) return null;
    	
    	int[] outData = new int[10];
    	outData[0] = 0x68;
    	outData[1] = 0x05;
    	outData[2] = 0x0E;
    	outData[3] = person.group;
    	outData[4] = person.sex;
    	outData[5] = person.level;
    	outData[6] = person.height;
    	outData[7] = person.age;
    	outData[8] = 1;       // dan wei mo ren wei 1
    	
    	for(int i = 1; i < outData.length -1; i++){
        	outData[outData.length -1] += outData[i];
        }
    	outData[outData.length -1] = outData[outData.length -1] & 0x00FF;
    	return outData;
    }
}
