package com.communication.data;

public class TransferStatus {
	public static final int RECEIVE_BINED_ID = 0xC1;
	public static final int RECEIVE_CONNECTION_ID = 0x81;
	public static final int RECEIVE_GETVERSION_ID = 0x82;
	public static final int RECEIVE_WRITE_ID = 0x83;
	public static final int RECEIVE_UPDATETIME_ID = 0x8A;
	public static final int RECEIVE_GETUSERINFO_ID = 0x87;
	public static final int RECEIVE_GETUSERINFO2_ID = 0x88;
	public static final int RECEIVE_UPDATEUSERINFO_SUCCESS_ID = 0x85;
	public static final int RECEIVE_UPDATEUSERINFO2_SUCCESS_ID = 0x86;
	public static final int RECEIVE_CLEARDATA_ID = 0x94;
	public static final int RECEIVE_READDATA_ID = 0x91;
	public static final int RECEIVE_FRAMECOUNT_ID = 0x8C;
	public static final int RECEIVE_DEVICE_ID = 0x84;
	public static final int RECEIVE_DEVICE_TIME_ID = 0x8B;

	public static final int RECEIVE_BOOT_STATE_ID = 0xF0;
	public static final int RECEIVE_BOOT_CONNECT_ID = 0xF1;
	public static final int RECEIVE_BOOT_VERSION_ID = 0xF2;
	public static final int RECEIVE_BOOT_UPGRADE_ID = 0xF3;
	public static final int RECEIVE_UPGRADE_OVER_ID = 0xF4;
	
}
