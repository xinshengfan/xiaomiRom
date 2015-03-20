package com.communication.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DataBaseHelper extends SQLiteOpenHelper {

	public static DataBaseHelper mDataBaseHelper;
	private final static String DATABASE_NAME = "Codoon_Accessory.db";
	public final static int DATABASE_VERSION = 3;
	static SQLiteDatabase db;

	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	protected synchronized SQLiteDatabase getDatabase() {

		if (db != null && db.isOpen()) {
			return db;
		} else {
			db = getWritableDatabase();
		}
		return db;
	}

	protected synchronized void closeDatabase() {

		if (db != null && db.isOpen() && !db.isDbLockedByCurrentThread()
				&& !db.isDbLockedByOtherThreads()) {
			db.close();
			db = null;
		}

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SleepDetailDB.createTableSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(!isTableExist(SleepDetailDB.DATABASE_TABLE)){
			
			db.execSQL(SleepDetailDB.createTableSql);
		}
		
		if(!isColumnExist(db, SleepDetailDB.DATABASE_TABLE, SleepDetailDB.COLUMN_SLEEP)){
			db.execSQL(" ALTER TABLE " + SleepDetailDB.DATABASE_TABLE
					+ " ADD Column " + SleepDetailDB.COLUMN_SLEEP
					+ " integer NOT NULL default 0 ");
		}
	}

	/* 锟叫讹拷锟斤拷锟斤拷锟角凤拷锟斤拷锟� */
	public boolean isColumnExist(SQLiteDatabase db, String tableName,
			String columnName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}

		try {
			Cursor cursor = null;
			String sql = "select count(1) as c from sqlite_master where type ='table' and name ='"
					+ tableName.trim()
					+ "' and sql like '%"
					+ columnName.trim() + "%'";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}

			cursor.close();
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 锟叫讹拷某锟脚憋拷锟角凤拷锟斤拷锟�
	 * 
	 * @param tabName
	 *            锟斤拷锟斤拷
	 * @return
	 */
	public boolean isTableExist(String tableName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}

		try {
			Cursor cursor = null;
			String sql = "select count(1) as c from sqlite_master where type ='table' and name ='"
					+ tableName.trim() + "'";
			cursor = getDatabase().rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}

			cursor.close();
		} catch (Exception e) {
		}
		return result;
	}

	public abstract void open();

	public abstract void close();

}
