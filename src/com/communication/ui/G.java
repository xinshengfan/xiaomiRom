package com.communication.ui;

/**
 * 常用静态变量
 * 
 * @author Administrator
 * 
 */
public class G {
	public static final String ACTION_PLAY = "com.fanfan.action.MUSICPLAY";
	public static final String ACTION_PAUSE = "com.fanfan.action.PAUSE";
	public static final String ACTION_PREVIOUS = "com.fanfan.action.PREVIOUS";
	public static final String ACTION_NEXT = "com.fanfan.action.NEXT";
	public static final String ACTION_NEW_MUSIC = "com.fanfan.action.NEWMUSIC";
	public static final String ACTTION_WIDGET_PLAY = "com.fanfan.action.WIDGETPLAY";
	public static final String ACTION_WIDGET_ISPLAY = "com.fanfan,action.WIDGETISPLAY";

	public static final String ACTION_MODE_CHANGED = "com.fanfan.action.MODECHANGED";
	public static final String ACTION_EXIT = "com.fanfan.action.EXIT";

	public static final String ACTION_ACTIVITY_START = "com.fanfan.action.ACTIVITYSTART";
	public static final String ACTION_ACTIVITY_STOP = "com.fanfan.action.ACTIVITYSTOP";
	public static final String ACTION_SEEK_TO = "com.fanfan.action.SEEK_TO";
	public static final String ACTION_RESPONSE = "cpm.fanfan.action.RESPONSE";

	public static final String EXTRA_PLAY_MODE = "playMode";
	public static final String EXTRA_CURRENT_POSTION = "currentPostion";
	public static final String EXTRA_PLAY_STATE = "playState";

	public static final int STATE_PLAY = 3;
	public static final int STATE_PAUSE = 4;
	public static final int STATE_NORMAL = 5;
	public static final int MODE_LOOP = 1;
	public static final int MODE_RANDOM = 2;

	public static final String ACTION_CURRENT_MUSIC_CHANGED = "com.fanfan.action.CURRENTMUSICCHANGED";
	public static final String ACTION_PROGREE_UPDATA = "com.fanfan.action.PROGESSUPDATA";

	public static final String KEY_COD_MI = "keyCodMi";
	public static final String TYPE_COD_MI_NAME = "cod_mi";
	/*****************************************************************************************/
	public static final String KEY_CONNECT_BRACELECT = "keyConnectBracelect";

	public static final String ACTION_READ_CHARGE = "com.codoon.action.readcharge";
	public static final String KEY_BRACELET_CHARGE = "keyBraceletCharge";
	public static final String ACTION_BIND_SUCCESS = "com.codoon.action.bindSuccess";
	public static final String ACTION_TIME_OUT = "com.codoon.action.timeout";
	public static final String ACTION_FRAME_NUM = "com.codoon.action.framenumber";
	public static final String KEY_FRAME_NUM = "keyframenum";
	public static final String ACTION_UPGRADE_SUCCESS = "com.codoon.action.upgradeSuccess";
	public static final String ACTION_DEVICE_VERSION = "com.codoon.action.deviceversion";
	public static final String KEY_VERSION_HEIGHT = "keyversionheight";
	public static final String KEY_VERSION_LOW = "keyversionlow";
	public static final String KEY_VERSION_NAME = "keyversionname";
	public static final String ACTION_DOWNLOAD_OVER = "com.codoon.action.downloadover";
	public static final String KEY_DOWNLOAD_OVER = "keydownloadover";
	public static final String KEY_DOWNLOAD_ERROR_REASON = "keydownloaderrorreason";
	public static final String ACTION_CONNECTED_BRACELET = "com.codoon.action.connectedbracelet";
	public static final String ACTION_CHECK_ERROR = "com.codoon.action.checkerror";
	public static final String ACTION_HAD_CBOOT = "com.codoon.action.hadcboot";
}