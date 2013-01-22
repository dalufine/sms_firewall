package com.quazar.sms_firewall.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.quazar.sms_firewall.models.FilterModel;
import com.quazar.sms_firewall.models.FilterModel.FilterType;
import com.quazar.sms_firewall.models.LogItemModel;
import com.quazar.sms_firewall.models.LogItemModel.LogStatus;
import com.quazar.sms_firewall.models.TopItemModel;
import com.quazar.sms_firewall.models.TopItemModel.TopTypes;

public class DataDao extends SQLiteOpenHelper {
	private static final String DB_NAME = "sms_firewall";
	private static final String createSql = "CREATE TABLE filters (_id integer PRIMARY KEY AUTOINCREMENT, type integer, value text NOT NULL);"
			+ "CREATE TABLE logs(_id integer PRIMARY KEY AUTOINCREMENT, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, phone_name TEXT NOT NULL, body TEXT NOT NULL, status INTEGER DEFAULT 0);"
			+ "CREATE TABLE tops(_id integer PRIMARY KEY AUTOINCREMENT, pos integer NOT NULL, phone_name text NOT NULL, votes integer DEFAULT 0, type integer);",
			dropSql = "DROP TABLE filters; DROP TABLE logs; DROP TABLE tops;";

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private SQLiteDatabase dbase;
	// cache
	private static List<LogItemModel> logs = null;
	private static List<FilterModel> filters = null;
	private static List<TopItemModel> tops = null;

	public DataDao(Context context) {
		super(context, DB_NAME, null, 2);
		dbase = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(dropSql + createSql);
	}

	public int insertFilter(FilterType type, String value) {
		Cursor cursor = dbase.rawQuery(
				"SELECT * FROM filters WHERE type=? AND value=?", new String[] {
						String.valueOf(type.ordinal()), value });
		if (cursor.getCount() == 0) {
			ContentValues cv = new ContentValues();
			cv.put("value", value);
			cv.put("type", type.ordinal());
			return (int) dbase.insert("filters", null, cv);
		}
		return 0;
	}

	public int insertLog(String phoneName, String body, LogStatus status) {
		ContentValues cv = new ContentValues();
		cv.put("add_time", sdf.format(new Date(System.currentTimeMillis())));
		cv.put("phone_name", phoneName);
		cv.put("status", status.ordinal());
		return (int) dbase.insert("logs", null, cv);
	}

	public List<FilterModel> getFilters() {
		if (filters != null)
			return filters;
		filters = new ArrayList<FilterModel>();
		Cursor cursor = dbase.rawQuery("SELECT * FROM filters", null);
		int idIdx = cursor.getColumnIndex("_id"), valueIdx = cursor
				.getColumnIndex("value"), typeIdx = cursor
				.getColumnIndex("type");
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.move(i);
			filters.add(new FilterModel(cursor.getInt(idIdx), cursor
					.getString(valueIdx), cursor.getInt(typeIdx)));
		}
		return filters;
	}

	public List<LogItemModel> getLogs(LogStatus status) {
		if (logs != null)
			return filterLogsByStatus(status);
		try {
			logs = new ArrayList<LogItemModel>();
			Cursor cursor = dbase.rawQuery("SELECT * FROM logs", null);
			int idIdx = cursor.getColumnIndex("_id"), phoneNameIdx = cursor
					.getColumnIndex("phone_name"), bodyIdx = cursor
					.getColumnIndex("body"), addTimeIdx = cursor
					.getColumnIndex("addTime"), statusIdx = cursor
					.getColumnIndex("body");
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.move(i);
				logs.add(new LogItemModel(cursor.getInt(idIdx), cursor.getString(phoneNameIdx), 
						cursor.getString(bodyIdx), sdf.parse(cursor
						.getString(addTimeIdx)), cursor.getInt(statusIdx)));
			}
		} catch (Exception ex) {
			Log.e("logs loading", ex.toString());
		}
		return filterLogsByStatus(status);
	}

	private List<LogItemModel> filterLogsByStatus(LogStatus status) {
		List<LogItemModel> result = new ArrayList<LogItemModel>();
		for (LogItemModel li : logs) {
			if (li.getStatus() == status)
				result.add(li);
		}
		return result;
	}

	public List<TopItemModel> getTop(TopTypes type) {
		if (tops != null)
			return tops;
		tops = new ArrayList<TopItemModel>();
		Cursor cursor = dbase.rawQuery("SELECT * FROM tops", null);
		int idIdx = cursor.getColumnIndex("_id"), posIdx = cursor
				.getColumnIndex("pos"), votesIdx = cursor
				.getColumnIndex("votes"), phoneNameIdx = cursor
				.getColumnIndex("phone_name"), typeIdx = cursor
				.getColumnIndex("type");
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.move(i);
			tops.add(new TopItemModel(cursor.getInt(idIdx), cursor.getInt(posIdx), cursor.getInt(votesIdx), 
					cursor.getString(phoneNameIdx), cursor.getInt(typeIdx)));
		}
		return tops;
	}
	public void updateTop(List<TopItemModel> newTop){
		tops=newTop;
		dbase.beginTransaction();
		dbase.delete("tops", null, null);
		for(TopItemModel ti:tops){
			ContentValues cv=new ContentValues();
			cv.put("pos", ti.getPos());
			cv.put("phone_name", ti.getPhoneName());
			cv.put("votes", ti.getVotes());
			cv.put("type", ti.getType().ordinal());
			dbase.insert("tops", null, cv);
		}
		dbase.setTransactionSuccessful();
		dbase.endTransaction();
	}
}
