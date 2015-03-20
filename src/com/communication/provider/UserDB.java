package com.communication.provider;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class UserDB extends DataBaseHelper {

	private String TAG = "UserDB";

	public static final String DATABASE_TABLE = "user";
	public static final String COLUMN_ID = BaseColumns._ID,
			COLUMN_USERID = "userid", COLUMN_NAME = "username",
			COLUMN_AGE = "age", COLUMN_HEIGHT = "height",
			COLUMN_WEIGHT = "weight", COLUMN_SEX = "sex";

	public static final String CreateTableSql = "create table"
			+ " IF NOT EXISTS " + DATABASE_TABLE + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_USERID
			+ " NVARCHAR(100) not null, " + COLUMN_NAME
			+ " NVARCHAR(100) not null, " + COLUMN_SEX + " integer not null ,"
			+ COLUMN_AGE + " integer not null ," + COLUMN_HEIGHT
			+ " integer not null ," + COLUMN_WEIGHT + " integer )";

	public static final String[] dispColumns = { COLUMN_ID, COLUMN_USERID,
			COLUMN_NAME, COLUMN_AGE, COLUMN_HEIGHT, COLUMN_WEIGHT, COLUMN_SEX };

	private SQLiteDatabase db;

	public UserDB(Context context) {
		super(context);
	}

	public void beginTransaction() {
		db.beginTransaction();
	}

	public void setTransaction() {
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
	public long insert(UserInfoObject mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_NAME, mod.userName);
		initialValues.put(COLUMN_AGE, mod.age);
		initialValues.put(COLUMN_HEIGHT, mod.height);
		initialValues.put(COLUMN_WEIGHT, mod.weight);
		initialValues.put(COLUMN_SEX, mod.sex);
		long count = db.insert(DATABASE_TABLE, null, initialValues);
		close();
		Log.d(TAG, "insert() count:" + count);
		return count;
	}

	/**
	 * 
	 * @param mod
	 * @return
	 */
	public int update(UserInfoObject mod) {
		open();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_USERID, mod.userID);
		initialValues.put(COLUMN_NAME, mod.userName);
		initialValues.put(COLUMN_AGE, mod.age);
		initialValues.put(COLUMN_HEIGHT, mod.height);
		initialValues.put(COLUMN_WEIGHT, mod.weight);
		initialValues.put(COLUMN_SEX, mod.sex);
		int count = db.update(DATABASE_TABLE, initialValues, null, null);
		close();
		return count;
	}

	/**
	 * 
	 * @param userid
	 * @return
	 */
	public UserInfoObject get(String userid) {
		open();
		Cursor c = db.query(DATABASE_TABLE, dispColumns, COLUMN_USERID + "='"
				+ userid + "'", null, null, null, null);
		if (c == null) {
			close();
			Log.v(TAG, "record is 0");
			return null;
		}

		UserInfoObject userObject =null;
		if (c.moveToFirst()) {
			userObject=new UserInfoObject();
			userObject.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
			userObject.userID = c.getString(c.getColumnIndex(COLUMN_USERID));
			userObject.userName = c.getString(c.getColumnIndex(COLUMN_NAME));
			userObject.age = c.getInt(c.getColumnIndex(COLUMN_AGE));
			userObject.height = c.getInt(c.getColumnIndex(COLUMN_HEIGHT));
			userObject.weight = c.getInt(c.getColumnIndex(COLUMN_WEIGHT));
			userObject.sex = c.getInt(c.getColumnIndex(COLUMN_SEX));
		}
		close();
		return userObject;
	}

	public int delete(String userid) {
		open();
		String where = COLUMN_USERID + "='" + userid + "'";
		Log.d(TAG, "delete() where:" + where);
		int count = db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + userid
				+ "'", null);
		Log.d(TAG, "delete() count:" + count);
		close();
		return count;
	}

	public boolean deleteAll() {
		return db.delete(DATABASE_TABLE, null, null) > 0;
	}

	public void updateAnonymous(String userid) {
		String updateString = "update" + DATABASE_TABLE + "set" + COLUMN_USERID
				+ "='" + userid + "'where" + COLUMN_USERID + "='"
				+ KeyConstants.USERANONYMOUSID_STRING_KEY + "'";
		db.execSQL(updateString);
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
