package com.quazar.sms_firewall.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataDao extends SQLiteOpenHelper {
	private static final String DB_NAME="sms_firewall";
	private SQLiteDatabase dbase;
	public enum FilterType{
		PHONE_NAME(0), WORD(1), REGEXP(2);
		private int value;
		FilterType(int value){
			this.value=value;
		}
		public int getValue() {
			return value;
		}		
	};
	public DataDao(Context context) {
		super(context, DB_NAME, null, 1);
		dbase=getWritableDatabase();
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="CREATE TABLE filters (_id integer PRIMARY KEY AUTOINCREMENT, type integer, value text NOT NULL);" +
				"CREATE TABLE logs(_id integer PRIMARY KEY AUTOINCREMENT, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, phone_name TEXT NOT NULL, body TEXT NOT NULL, status INTEGER DEFAULT 0);" +
				"CREATE TABLE tops(_id integer PRIMARY KEY AUTOINCREMENT, pos INTEGER NOT NULL, type INTEGER NOT NULL, votes INTEGER DEFAULT 0);";
		db.execSQL(sql);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	public int insertFilter(FilterType type, String value){
		Cursor cursor=dbase.rawQuery("SELECT * FROM filters WHERE type=? AND value=?", new String[]{String.valueOf(type.getValue()), value});
		if(cursor.getCount()==0){
			ContentValues cv=new ContentValues();
			cv.put("value", value);
			cv.put("type", type.getValue());
			return (int)dbase.insert("filters", null, cv);
		}
		return 0;
	}
}
