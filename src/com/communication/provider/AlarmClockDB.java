package com.communication.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class AlarmClockDB extends DataBaseHelper {
	private static final String TAG = AlarmClockDB.class.getName();
	public static final String DATABASE_TABLE = "alarmclock";
	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String Column_UserID = "user_id";
	public static final String Column_Switch_State = "switch_state";
	public static final String Column_Wakeup_Time = "wakeup_time";
	public static final String Column_Wakeup_Period = "wakeup_period";
	public static final String Column_Week_Day = "week_day";

	public static final String createTableSql = "create table "
			+ " IF NOT EXISTS " + DATABASE_TABLE + "(" 
			+ COLUMN_ID+ " integer primary key autoincrement," 
			+ Column_UserID+ "  NVARCHAR(100) not null," 
			+ Column_Switch_State+ " integer not null,"
			+ Column_Wakeup_Period+ " integer not null,"
			+ Column_Wakeup_Time+ "  integer  not null," 
			+ Column_Week_Day+ " NVARCHAR(10))";
	public static final String[] dispColumns = { COLUMN_ID, Column_UserID,
			Column_Switch_State, Column_Wakeup_Time, Column_Wakeup_Period,
			Column_Week_Day };
	private SQLiteDatabase db;

	public AlarmClockDB(Context context) {
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

	public long Insert(AlarmClockObject mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Column_UserID, mod.user_id);
		initialValues.put(Column_Switch_State, mod.switch_state);
		initialValues.put(Column_Wakeup_Time, mod.wakeup_time);
		initialValues.put(Column_Wakeup_Period, mod.wakeup_period);
		initialValues.put(Column_Week_Day, mod.week_day);
		long count = db.insert(DATABASE_TABLE, null, initialValues);
		close();
		Log.d(TAG, "insert() count:" + count);
		return count;
	}

	public int update(AlarmClockObject mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(Column_UserID, mod.user_id);
		initialValues.put(Column_Switch_State, mod.switch_state);
		initialValues.put(Column_Wakeup_Time, mod.wakeup_time);
		initialValues.put(Column_Wakeup_Period, mod.wakeup_period);
		initialValues.put(Column_Week_Day, mod.week_day);
		int count = db.update(DATABASE_TABLE, initialValues, null, null);
		close();
		return count;
	}

	public AlarmClockObject get(String userid) {
		open();
		Cursor c = db.query(DATABASE_TABLE, dispColumns, Column_UserID + " ='"
				+ userid + "'", null, null, null, Column_UserID + " ASC");
		if (c == null) {
			Log.v(TAG, "record is 0");
			return null;
		}
		AlarmClockObject alarmClockObject =null;
		if (c.moveToFirst()) {
			alarmClockObject=new AlarmClockObject();
			alarmClockObject.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
			alarmClockObject.user_id = c.getString(c
					.getColumnIndex(Column_UserID));
			alarmClockObject.switch_state = c.getInt(c
					.getColumnIndex(Column_Switch_State));
			alarmClockObject.wakeup_time = c.getInt(c
					.getColumnIndex(Column_Wakeup_Time));
			alarmClockObject.wakeup_period = c.getInt(c
					.getColumnIndex(Column_Wakeup_Period));
			alarmClockObject.week_day = c.getString(c
					.getColumnIndex(Column_Week_Day));
		}
		close();
		return alarmClockObject;

	}

	public int deleteByUserId(String user_id) {
		open();
		int count = db.delete(DATABASE_TABLE, Column_UserID + "='" + user_id + "'",
				null);
		close();
		return count;
	}

	public boolean deleteAll() {
		return db.delete(DATABASE_TABLE, null, null) > 0;
	}

	public void updateAnonymous(String userid) {
		String updateString = " update " + DATABASE_TABLE + " set "
				+ Column_UserID + " = '" + userid + "' where " + Column_UserID
				+ " = '" + KeyConstants.USERANONYMOUSID_STRING_KEY + "'";
		db.execSQL(updateString);
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
