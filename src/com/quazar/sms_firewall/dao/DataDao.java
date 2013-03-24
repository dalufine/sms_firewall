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
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.models.LogItem;
import com.quazar.sms_firewall.models.LogItem.LogStatus;
import com.quazar.sms_firewall.models.TopItem;
import com.quazar.sms_firewall.models.TopItem.TopTypes;

public class DataDao extends SQLiteOpenHelper{
	private static final String DB_NAME="sms_firewall";
	private static final String createSql="CREATE TABLE filters (_id integer PRIMARY KEY AUTOINCREMENT, type integer, value text NOT NULL);"
			+"CREATE TABLE logs(_id integer PRIMARY KEY AUTOINCREMENT, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, phone_name TEXT NOT NULL, body TEXT NOT NULL, status INTEGER DEFAULT 0);"
			+"CREATE TABLE tops(_id integer PRIMARY KEY AUTOINCREMENT, pos integer NOT NULL, phone_name text NOT NULL, votes integer DEFAULT 0, type integer);",
			dropSql="DROP TABLE filters; DROP TABLE logs; DROP TABLE tops;";

	private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SQLiteDatabase dbase;
	// cache
	private static List<LogItem> logs=null;
	private static List<Filter> filters=null;
	private static List<TopItem> tops=null;

	public DataDao(Context context){
		super(context, DB_NAME, null, 2);
		dbase=getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL(createSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL(dropSql+createSql);
	}
	public int insertLog(String phoneName, String body, LogStatus status){
		ContentValues cv=new ContentValues();
		cv.put("add_time", sdf.format(new Date(System.currentTimeMillis())));
		cv.put("phone_name", phoneName);
		cv.put("status", status.ordinal());
		return (int)dbase.insert("logs", null, cv);
	}
	// ------------------Filters-------------------------
	public List<Filter> getFilters(){
		if(filters!=null)
			return filters;
		filters=new ArrayList<Filter>();
		Cursor cursor=dbase.rawQuery("SELECT * FROM filters", null);
		int idIdx=cursor.getColumnIndex("_id"), valueIdx=cursor.getColumnIndex("value"), typeIdx=cursor.getColumnIndex("type");
		while(cursor.moveToNext()){
			filters.add(new Filter(cursor.getInt(idIdx), cursor.getString(valueIdx), cursor.getInt(typeIdx)));
		}
		return filters;
	}
	public int insertFilter(FilterType type, String value){
		Cursor cursor=dbase.rawQuery("SELECT * FROM filters WHERE type=? AND value=?", new String[]{String.valueOf(type.ordinal()), value});
		if(cursor.getCount()==0){
			ContentValues cv=new ContentValues();
			cv.put("value", value);
			cv.put("type", type.ordinal());
			int result=(int)dbase.insert("filters", null, cv);
			if(result>0&&filters!=null){
				filters.add(new Filter(result, value, type.ordinal()));
			}
			return result;
		}
		return 0;
	}
	public int deleteFilter(int id){
		if(filters!=null) {
			for(Filter f:filters) {
				if(f.getId()==id) {
					filters.remove(f);
					break;
				}
			}
		}
		return dbase.delete("filters", "_id=?", new String[] {String.valueOf(id)});
	}
	// ------------------Logs-----------------------------------
	public List<LogItem> getLogs(LogStatus status){
		if(logs!=null)
			return filterLogsByStatus(status);
		try{
			logs=new ArrayList<LogItem>();
			Cursor cursor=dbase.rawQuery("SELECT * FROM logs", null);
			int idIdx=cursor.getColumnIndex("_id"), phoneNameIdx=cursor.getColumnIndex("phone_name"), bodyIdx=cursor.getColumnIndex("body"), addTimeIdx=cursor.getColumnIndex("addTime"), statusIdx=cursor.getColumnIndex("body");
			for(int i=0; i<cursor.getCount(); i++){
				cursor.move(i);
				logs.add(new LogItem(cursor.getInt(idIdx), cursor.getString(phoneNameIdx), cursor.getString(bodyIdx), sdf.parse(cursor.getString(addTimeIdx)), cursor.getInt(statusIdx)));
			}
		}catch(Exception ex){
			Log.e("logs loading", ex.toString());
		}
		return filterLogsByStatus(status);
	}
	private List<LogItem> filterLogsByStatus(LogStatus status){
		List<LogItem> result=new ArrayList<LogItem>();
		for(LogItem li:logs){
			if(li.getStatus()==status)
				result.add(li);
		}
		return result;
	}
	// ------------------Tops--------------------------------
	public List<TopItem> getTop(TopTypes type){
		if(tops!=null)
			return tops;
		tops=new ArrayList<TopItem>();
		Cursor cursor=dbase.rawQuery("SELECT * FROM tops", null);
		int idIdx=cursor.getColumnIndex("_id"), posIdx=cursor.getColumnIndex("pos"), votesIdx=cursor.getColumnIndex("votes"), phoneNameIdx=cursor.getColumnIndex("phone_name"), typeIdx=cursor.getColumnIndex("type");
		for(int i=0; i<cursor.getCount(); i++){
			cursor.move(i);
			tops.add(new TopItem(cursor.getInt(idIdx), cursor.getInt(posIdx), cursor.getInt(votesIdx), cursor.getString(phoneNameIdx), cursor.getInt(typeIdx)));
		}
		return tops;
	}
	public void updateTop(List<TopItem> newTop){
		tops=newTop;
		dbase.beginTransaction();
		dbase.delete("tops", null, null);
		for(TopItem ti:tops){
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
