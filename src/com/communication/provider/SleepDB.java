package com.communication.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class SleepDB extends DataBaseHelper {

	private String TAG = "SleepDB";
	public static final String DATABASE_TABLE = "sleep";
	public static final String COLUMN_ID = BaseColumns._ID,
			COLUMN_USERID = "userID", COLUMN_SLEEPS = "sleeps",
			COLUMN_TOTALMINUTES = "totalminutes",
			COLUMN_DEEPMINUTES = "deepminutes",
			COLUMN_LIGHTMINUTES="lightminutes",
			COLUMN_AWAKEMINUTES = "awakeminutes", 
			COLUMN_CURDATE = "curDate",
			COLUMN_ISUPDLOAD = "isUpload";

	public static final String createTableSql = "create table "
			+ " IF NOT EXISTS " + DATABASE_TABLE + "(" 
			+ COLUMN_ID + " integer primary key autoincrement,"
			+ COLUMN_USERID + "  NVARCHAR(100) not null,"
			+ COLUMN_ISUPDLOAD + " integer not null," 
			+ COLUMN_SLEEPS + "   NVARCHAR(800)  not null,"
			+ COLUMN_TOTALMINUTES + "   integer  not null," 
			+ COLUMN_DEEPMINUTES + "   integer  not null," 
			+ COLUMN_LIGHTMINUTES + "   integer  not null," 
			+ COLUMN_AWAKEMINUTES + "   integer  not null," 
			+ COLUMN_CURDATE + "   integer not null)";

	public static final String[] dispColumns = { COLUMN_ID, COLUMN_USERID,
			COLUMN_SLEEPS, COLUMN_TOTALMINUTES, COLUMN_DEEPMINUTES,COLUMN_LIGHTMINUTES,
			COLUMN_AWAKEMINUTES, COLUMN_CURDATE, COLUMN_ISUPDLOAD };

	private SQLiteDatabase db;

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransactionSuccessful() {
		db.setTransactionSuccessful();
	}

	public void endTransaction() {
		db.endTransaction();
	}

	public SleepDB(Context context) {
		super(context);
	}

	public long insert(SleepTB mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_SLEEPS, mod.sleeps);
		initialValues.put(COLUMN_TOTALMINUTES, mod.totalminutes);
		initialValues.put(COLUMN_DEEPMINUTES, mod.deepMinutes);
		initialValues.put(COLUMN_LIGHTMINUTES, mod.lightMinutes);
		initialValues.put(COLUMN_AWAKEMINUTES, mod.awakeminutes);
		initialValues.put(COLUMN_CURDATE, mod.curDate);
		initialValues.put(COLUMN_ISUPDLOAD, mod.isUpload);
		long count = db.insert(DATABASE_TABLE, null, initialValues);
		close();
		Log.d(TAG, "insert() count=" + count);
		return count;
	}

	public int update(SleepTB mod) {
		open();
		String where = COLUMN_USERID + " ='" + mod.userID + "' and "
				+ COLUMN_CURDATE + "=" + mod.curDate;
		Log.d(TAG, "update() where is:" + where);
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_SLEEPS, mod.sleeps);
		initialValues.put(COLUMN_TOTALMINUTES, mod.totalminutes);
		initialValues.put(COLUMN_DEEPMINUTES, mod.deepMinutes);
		initialValues.put(COLUMN_LIGHTMINUTES, mod.lightMinutes);
		initialValues.put(COLUMN_AWAKEMINUTES, mod.awakeminutes);
		initialValues.put(COLUMN_CURDATE, mod.curDate);
		initialValues.put(COLUMN_ISUPDLOAD, mod.isUpload);
		int count = db.update(DATABASE_TABLE, initialValues, where, null);
		Log.d(TAG, "update() count:" + count);
		close();
		return count;
	}

	public SleepTB get(String userid, int curDay) {
		open();
		String where = COLUMN_USERID + " ='" + userid + "' and "
				+ COLUMN_CURDATE + "=" + curDay;
		Log.d(TAG, "get where is:" + where);
		Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
				null, COLUMN_USERID + " ASC");
		if (c == null) {
			return null;
		}
		SleepTB mod = null;
		if (c.moveToFirst()) {
			mod = new SleepTB();
			mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
			mod.userID = c.getString(c.getColumnIndex(COLUMN_USERID));
			mod.sleeps = c.getString(c.getColumnIndex(COLUMN_SLEEPS));
			mod.totalminutes = c.getInt(c.getColumnIndex(COLUMN_TOTALMINUTES));
			mod.deepMinutes = c.getInt(c.getColumnIndex(COLUMN_DEEPMINUTES));
			mod.lightMinutes=c.getInt(c.getColumnIndex(COLUMN_LIGHTMINUTES));
			mod.awakeminutes = c.getInt(c.getColumnIndex(COLUMN_AWAKEMINUTES));
			mod.curDate = c.getInt(c.getColumnIndex(COLUMN_CURDATE));
			mod.isUpload = c.getInt(c.getColumnIndex(COLUMN_ISUPDLOAD));
		}
		close();
		return mod;

	}

	public boolean deleteByUserId(String user_id) {
		return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + user_id + "'",
				null) > 0;
	}

	public boolean deleteAll() {
		return db.delete(DATABASE_TABLE, null, null) > 0;
	}

	@Override
	public void open() {
		if (db == null) {
			db = getDatabase();
		}
	}

	@Override
	public void close() {
		closeDatabase();
		db = null;
	}

}
