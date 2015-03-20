package com.communication.provider;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class SleepDetailDB extends DataBaseHelper {

	public static final String DATABASE_TABLE = "sleepdetail";
	public static final String COLUMN_ID = BaseColumns._ID,
			COLUMN_USERID = "userid", COLUMN_TIME = "time",
			COLUMN_SLEEP = "sleepvalue",
			COLUMN_TYPE = "type";
	public static final String createTableSql = "create table "
			+ " IF NOT EXISTS " + DATABASE_TABLE + "(" + COLUMN_ID
			+ " integer primary key autoincrement not null," + COLUMN_USERID
			+ " NVARCHAR(100) not null," + COLUMN_TIME
			+ " integer  not null,"  + COLUMN_SLEEP 
			+ " integer  not null, " + COLUMN_TYPE + " integer )";

	public static final String[] dispColumns = { COLUMN_ID, COLUMN_USERID,
			COLUMN_TIME, COLUMN_SLEEP, COLUMN_TYPE };

	public SleepDetailDB(Context context) {
		super(context);
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

	public long insert(SleepDetailTB mod) {
		long count = 0;
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userid);
		initialValues.put(COLUMN_TIME, mod.time);
		initialValues.put(COLUMN_TYPE, mod.type);
		initialValues.put(COLUMN_SLEEP, mod.sleepvalue);
		count = db.insert(DATABASE_TABLE, null, initialValues);
		return count;
	}
	

	public List<SleepDetailTB> get(String userid, long start, long end) {
		String where = COLUMN_USERID + " ='" + userid + "' and " + COLUMN_TIME
				+ " < " + end + " and " + COLUMN_TIME + " >= " + start;
		Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
				null, COLUMN_TIME + " ASC");
		if (c == null ) {
			return null;
		}
		List<SleepDetailTB> list = null;
		try {
			if (c.moveToFirst()) {
				list = new ArrayList<SleepDetailTB>(c.getCount());
				do {
					SleepDetailTB mod = new SleepDetailTB();

					mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
					mod.userid = c.getString(c.getColumnIndex(COLUMN_USERID));
					mod.time = c.getLong(c.getColumnIndex(COLUMN_TIME));
					mod.type = c.getInt(c.getColumnIndex(COLUMN_TYPE));
					mod.sleepvalue = c.getInt(c.getColumnIndex(COLUMN_SLEEP));
					list.add(mod);
				} while (c.moveToNext());
			}
		} catch (IllegalStateException e) {

		} finally {
			c.close();
		}
		return list;
	}

	public SleepDetailTB get(String userid, long time) {
		String where = COLUMN_USERID + " ='" + userid + "' and " + COLUMN_TIME
				+ " = " + time;
		Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
				null, COLUMN_TIME + " ASC");
		if (c == null) {
			return null;
		}
		SleepDetailTB mod = null;
		try {
			if (c.moveToFirst()) {
				mod = new SleepDetailTB();
				mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
				mod.userid = c.getString(c.getColumnIndex(COLUMN_USERID));
				mod.time = c.getLong(c.getColumnIndex(COLUMN_TIME));
				mod.type = c.getInt(c.getColumnIndex(COLUMN_TYPE));
				mod.sleepvalue = c.getInt(c.getColumnIndex(COLUMN_SLEEP));
			}
		} catch (IllegalStateException e) {

		} finally {
			c.close();
		}
		return mod;
	}

	public int update(SleepDetailTB mod) {
		String where = COLUMN_TIME + " = " + mod.time;
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userid);
		initialValues.put(COLUMN_TIME, mod.time);
		initialValues.put(COLUMN_TYPE, mod.type);
		initialValues.put(COLUMN_SLEEP, mod.sleepvalue);
		int count = db.update(DATABASE_TABLE, initialValues, where, null);
		return count;
	}

	public boolean deleteByUserId(String user_id) {
		return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + user_id + "'",
				null) > 0;
	}
	
	public int deleteBetweenTime(String user_id, long start, long end){
		return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + user_id + "' and "
				+ COLUMN_TIME + " >= " + start + " and " + COLUMN_TIME + " < " + end,
				null);
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

	public int deleteByUserId(String userID, long time) {
		// TODO Auto-generated method stub
		return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + userID + "' and " +
				COLUMN_TIME + " = " + time, null) ;
	}

}
