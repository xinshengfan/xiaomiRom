package com.communication.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class ActivityRemindDB extends DataBaseHelper {
	private static final String TAG = ActivityRemindDB.class.getName();
	public static final String DATABASE_TABLE = "activityremind";
	public static final String Column_ID = BaseColumns._ID;
	public static final String Column_UserID = "user_id";
	public static final String Column_Switch_State = "switch_state";
	public static final String Column_Interval = "interval";
	public static final String Column_Begin_Time = "begin_time";
	public static final String Column_End_Time = "end_time";
	public static final String Column_Week_Day = "week_day";

	public static final String createTableSql = "create table "

	+ " IF NOT EXISTS " + DATABASE_TABLE + "(" + Column_ID
			+ " integer primary key autoincrement," + Column_UserID
			+ "  NVARCHAR(100) not null," + Column_Switch_State
			+ " integer not null," + Column_Interval + " integer not null,"
			+ Column_Begin_Time + "   integer  not null," + Column_End_Time
			+ "   integer  not null," + Column_Week_Day + "  NVARCHAR(10))";
	public static final String[] dispColumns = { Column_ID, Column_UserID,
			Column_Switch_State, Column_Interval, Column_Begin_Time,
			Column_End_Time, Column_Week_Day };
	private SQLiteDatabase db;

	public ActivityRemindDB(Context context) {
		super(context);
	}

	@Override
	public synchronized void open() {
		if (db == null) {
			db = getDatabase();
		}
	}

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		db.setTransactionSuccessful();
	}

	public void endTransaction() {
		db.endTransaction();
	}

	/**
	 * 
	 * @param mod
	 * @return
	 */
	public long Insert(ActivityRemindObject mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Column_UserID, mod.user_id);
		initialValues.put(Column_Switch_State, mod.switch_state);
		initialValues.put(Column_Interval, mod.interval);
		initialValues.put(Column_Begin_Time, mod.begin_time);
		initialValues.put(Column_End_Time, mod.end_time);
		initialValues.put(Column_Week_Day, mod.week_day);
		long count = db.insert(DATABASE_TABLE, null, initialValues);
		close();
		Log.d(TAG, "insert() count:" + count);
		return count;
	}

	public long update(ActivityRemindObject mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Column_UserID, mod.user_id);
		initialValues.put(Column_Switch_State, mod.switch_state);
		initialValues.put(Column_Interval, mod.interval);
		initialValues.put(Column_Begin_Time, mod.begin_time);
		initialValues.put(Column_End_Time, mod.end_time);
		initialValues.put(Column_Week_Day, mod.week_day);
		int count = db.update(DATABASE_TABLE, initialValues, null, null);
		close();
		return count;
	}

	/**
	 * 
	 * @param userid
	 * @return
	 */
	public ActivityRemindObject get(String userid) {
		open();
		Cursor c = db.query(DATABASE_TABLE, dispColumns, Column_UserID + " ='"
				+ userid + "'", null, null, null, Column_UserID + " ASC");
		if (c == null) {
			Log.v(TAG, "record is 0");
			return null;
		}
		ActivityRemindObject activityRemindObject = null;
		if (c.moveToFirst()) {
			activityRemindObject = new ActivityRemindObject();
			activityRemindObject.user_id = c.getString(c
					.getColumnIndex(Column_UserID));
			activityRemindObject.switch_state = c.getInt(c
					.getColumnIndex(Column_Switch_State));
			activityRemindObject.interval = c.getInt(c
					.getColumnIndex(Column_Interval));
			activityRemindObject.begin_time = c.getInt(c
					.getColumnIndex(Column_Begin_Time));
			activityRemindObject.end_time = c.getInt(c
					.getColumnIndex(Column_End_Time));
			activityRemindObject.week_day = c.getString(c
					.getColumnIndex(Column_Week_Day));
		}
		close();
		return activityRemindObject;

	}

	public int deleteByUserId(String user_id) {
		open();
		int count = db.delete(DATABASE_TABLE, Column_UserID + "='" + user_id
				+ "'", null);
		close();
		return count;
	}

	public boolean deleteAll() {
		return db.delete(DATABASE_TABLE, null, null) > 0;
	}

	public synchronized void close() {
		// if (db != null) {
		// db.close();
		// }
		// db = null;
		closeDatabase();
		db = null;
	}
}
