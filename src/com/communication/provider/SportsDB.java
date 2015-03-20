package com.communication.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class SportsDB extends DataBaseHelper {

	private String TAG = "SportsDB";

	public static final String DATABASE_TABLE = "sport";
	public static final String COLUMN_ID = BaseColumns._ID,
			COLUMN_USERID = "userid", COLUMN_METERS = "meters",
			COLUMN_CALORIES = "calories", COLUMN_TOTALSTEPS = "totalSteps",
			COLUMN_TOTALDISTANCE = "totalDistance",
			COLUMN_TOTALCALORIES = "totalCalories",
			COLUMN_SPORTDURATION = "sportDuration", COLUMN_STEPS = "steps",
			COLUMN_CURDATE = "curDate", COLUMN_ISUPDLOAD = "isUpload";
	public static final String createTableSql = "create table "
			+ " IF NOT EXISTS "
			+ DATABASE_TABLE
			+ "("
			+ COLUMN_ID
			+ " integer primary key autoincrement not null,"
			+ COLUMN_USERID
			+ "  NVARCHAR(100) not null,"
			+ COLUMN_ISUPDLOAD
			+ " integer not null,"
			+ COLUMN_METERS
			+ "   NVARCHAR(800)  not null,"
			+ COLUMN_CALORIES
			+ "   NVARCHAR(800)  not null,"
			+ COLUMN_TOTALSTEPS
			+ "   integer  not null,"
			+ COLUMN_TOTALDISTANCE
			+ "   Ninteger  not null,"
			+ COLUMN_TOTALCALORIES
			+ "   integer  not null,"
			+ COLUMN_SPORTDURATION
			+ "   integer  not null,"
			+ COLUMN_STEPS
			+ "   NVARCHAR(800)  not null," + COLUMN_CURDATE + "   integer )";

	public static final String[] dispColumns = { COLUMN_ID, COLUMN_USERID,
			COLUMN_METERS, COLUMN_CALORIES, COLUMN_TOTALSTEPS,
			COLUMN_TOTALDISTANCE, COLUMN_TOTALCALORIES, COLUMN_SPORTDURATION,
			COLUMN_STEPS, COLUMN_CURDATE, COLUMN_ISUPDLOAD };

	private SQLiteDatabase db;

	public SportsDB(Context context) {
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

	public long insert(SportTB mod) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_METERS, mod.meters);
		initialValues.put(COLUMN_CALORIES, mod.calories);
		initialValues.put(COLUMN_TOTALSTEPS, mod.totalSteps);
		initialValues.put(COLUMN_TOTALDISTANCE, mod.totalDistance);
		initialValues.put(COLUMN_TOTALCALORIES, mod.totalCalories);
		initialValues.put(COLUMN_SPORTDURATION, mod.sportDuration);
		initialValues.put(COLUMN_STEPS, mod.steps);
		initialValues.put(COLUMN_CURDATE, mod.curDate);
		initialValues.put(COLUMN_ISUPDLOAD, mod.isUpload);
		long count = db.insert(DATABASE_TABLE, null, initialValues);
		Log.d(TAG, "insert() count:" + count);
		return count;
	}

	public SportTB get(String userid, int curDay) {
		String where = COLUMN_USERID + " ='" + userid + "' and "
				+ COLUMN_CURDATE + " =" + curDay;
		Log.d(TAG, "where is:" + where);
		Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
				null, COLUMN_CURDATE + " ASC");
		if (c == null) {
			return null;
		}
		SportTB mod = null;
		if (c.moveToFirst()) {
			mod = new SportTB();
			mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
			mod.userID = c.getString(c.getColumnIndex(COLUMN_USERID));
			mod.steps = c.getString(c.getColumnIndex(COLUMN_STEPS));
			mod.meters = c.getString(c.getColumnIndex(COLUMN_METERS));
			mod.calories = c.getString(c.getColumnIndex(COLUMN_CALORIES));
			mod.totalSteps = c.getInt(c.getColumnIndex(COLUMN_TOTALSTEPS));
			mod.totalCalories = c
					.getInt(c.getColumnIndex(COLUMN_TOTALCALORIES));
			mod.totalDistance = c
					.getInt(c.getColumnIndex(COLUMN_TOTALDISTANCE));
			mod.sportDuration = c
					.getInt(c.getColumnIndex(COLUMN_SPORTDURATION));
			mod.curDate = c.getInt(c.getColumnIndex(COLUMN_CURDATE));
			mod.isUpload = c.getInt(c.getColumnIndex(COLUMN_ISUPDLOAD));
		}
		return mod;
	}

	public int update(SportTB mod) {
		String where = COLUMN_USERID + " ='" + mod.userID + "' and "
				+ COLUMN_CURDATE + " =" + mod.curDate;
		Log.d(TAG, "update() where is:" + where);
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_METERS, mod.meters);
		initialValues.put(COLUMN_CALORIES, mod.calories);
		initialValues.put(COLUMN_TOTALSTEPS, mod.totalSteps);
		initialValues.put(COLUMN_TOTALDISTANCE, mod.totalDistance);
		initialValues.put(COLUMN_TOTALCALORIES, mod.totalCalories);
		initialValues.put(COLUMN_SPORTDURATION, mod.sportDuration);
		initialValues.put(COLUMN_STEPS, mod.steps);
		initialValues.put(COLUMN_CURDATE, mod.curDate);
		initialValues.put(COLUMN_ISUPDLOAD, mod.isUpload);
		int count = db.update(DATABASE_TABLE, initialValues, where, null);
		Log.d(TAG, "update() count=" + count);
		return count;
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
