package com.communication.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class SportTargetDB extends DataBaseHelper {

	// public String time;
	// public int steps,calories,distances,sleepmins;

	public static final String DATABASE_TABLE = "sporttarget";
	public static final String COLUMN_ID = BaseColumns._ID,
			COLUMN_USERID = "userID", COLUMN_TARGET = "targetType",
			COLUMN_TARGETVALUE = "targetValue";

	public static final String[] dispColumns = { COLUMN_ID, COLUMN_USERID,
			COLUMN_TARGET, COLUMN_TARGETVALUE };

	public static final String CreateTableSql;

	private SQLiteDatabase db;

	static {
		StringBuilder strSql = new StringBuilder();
		strSql.append("create table " + " IF NOT EXISTS " + DATABASE_TABLE
				+ "(");
		strSql.append(COLUMN_ID + " integer primary key autoincrement,");
		strSql.append(COLUMN_USERID + " NVARCHAR(50) not null , ");
		strSql.append(COLUMN_TARGET + " integer not null  ,");
		strSql.append(COLUMN_TARGETVALUE + " integer  )");

		CreateTableSql = strSql.toString();

	}

	public SportTargetDB(Context context) {
		super(context);
	}

	public long insert(SportTargetTB mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_TARGET, mod.targetType);
		initialValues.put(COLUMN_TARGETVALUE, mod.targetValue);

		long count = db.insert(DATABASE_TABLE, null, initialValues);
		close();
		return count;
	}

	public SportTargetTB getSportTarget(String userid) {
		open();
		Cursor c = db.query(DATABASE_TABLE, dispColumns, COLUMN_USERID + " ='"
				+ userid + "'", null, null, null, null);

		SportTargetTB mod = null;
		if (c == null) {
			return null;
		} else {
			if (c.moveToFirst()) {
				mod = new SportTargetTB();
				mod.ID= c.getInt(c.getColumnIndex(COLUMN_ID));
				mod.userID = c.getString(c.getColumnIndex(COLUMN_USERID));
				mod.targetType = c.getInt(c.getColumnIndex(COLUMN_TARGET));
				mod.targetValue = c.getInt(c.getColumnIndex(COLUMN_TARGETVALUE));
			}

		}
		close();

		return mod;
	}

	public int update(SportTargetTB mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_TARGET, mod.targetType);
		initialValues.put(COLUMN_TARGETVALUE, mod.targetValue);
		int count = db.update(DATABASE_TABLE, initialValues, null, null);
		close();
		return count;
	}

	@Override
	public synchronized void close() {
		closeDatabase();
		db = null;
	}

	@Override
	public synchronized void open() {
		if (db == null) {
			db = getDatabase();
		}
	}
}
