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
	private static final String[] createSql= {"CREATE TABLE filters (_id integer PRIMARY KEY AUTOINCREMENT, type integer, value text NOT NULL)",
			"CREATE TABLE logs(_id integer PRIMARY KEY AUTOINCREMENT, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, phone_name TEXT NOT NULL, body TEXT NOT NULL, status INTEGER DEFAULT 0)",
			"CREATE TABLE tops(_id integer PRIMARY KEY AUTOINCREMENT, pos integer NOT NULL, phone_name text NOT NULL, votes integer DEFAULT 0, type integer)"},
			dropSql= {"DROP TABLE IF EXISTS filters","DROP TABLE IF EXISTS logs","DROP IF EXISTS TABLE tops"};

	private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	// cache
	private static List<LogItem> logs=null;
	private static List<Filter> filters=null;
	private static List<TopItem> tops=null;

	public DataDao(Context context){
		super(context, DB_NAME, null, 1);		
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		for(String s:createSql)
			db.execSQL(s);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		for(String s:dropSql)
			db.execSQL(s);
		for(String s:createSql)
			db.execSQL(s);		
	}	
	// ------------------Filters-------------------------
	public List<Filter> getFilters(){
		if(filters!=null)
			return filters;
		filters=new ArrayList<Filter>();
		SQLiteDatabase dbase=getReadableDatabase();
		Cursor cursor=dbase.rawQuery("SELECT * FROM filters", null);
		int idIdx=cursor.getColumnIndex("_id"), valueIdx=cursor.getColumnIndex("value"), typeIdx=cursor.getColumnIndex("type");
		while(cursor.moveToNext()){
			filters.add(new Filter(cursor.getInt(idIdx), cursor.getString(valueIdx), cursor.getInt(typeIdx)));
		}		
		cursor.close();
		dbase.close();
		return filters;
	}
	public int insertFilter(FilterType type, String value){
		SQLiteDatabase dbase=getWritableDatabase();
		Cursor cursor=dbase.rawQuery("SELECT * FROM filters WHERE type=? AND value=?", new String[]{String.valueOf(type.ordinal()), value});		
		if(cursor.getCount()==0){
			ContentValues cv=new ContentValues();
			cv.put("value", value);
			cv.put("type", type.ordinal());
			int result=(int)dbase.insert("filters", null, cv);
			if(result>0&&filters!=null){
				filters.add(new Filter(result, value, type.ordinal()));
			}
			cursor.close();
			return result;
		}
		cursor.close();
		dbase.close();
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
		SQLiteDatabase dbase=getWritableDatabase();
		int result=dbase.delete("filters", "_id=?", new String[] {String.valueOf(id)});
		dbase.close();
		return result;
	}
	// ------------------Logs-----------------------------------
	public List<LogItem> getLogs(LogStatus status){
		if(logs!=null)
			return filterLogsByStatus(status);
		try{
			logs=new ArrayList<LogItem>();
			SQLiteDatabase dbase=getReadableDatabase();
			Cursor cursor=dbase.query("logs", null, null, null, null, null, null);
			int idIdx=cursor.getColumnIndex("_id"), phoneNameIdx=cursor.getColumnIndex("phone_name"), bodyIdx=cursor.getColumnIndex("body"), addTimeIdx=cursor.getColumnIndex("add_time"), statusIdx=cursor.getColumnIndex("status");
			while(cursor.moveToNext()){				
				logs.add(new LogItem(cursor.getInt(idIdx), cursor.getString(phoneNameIdx), cursor.getString(bodyIdx), sdf.parse(cursor.getString(addTimeIdx)), cursor.getInt(statusIdx)));
			}
			cursor.close();
			dbase.close();
		}catch(Exception ex){
			Log.e("logs loading", ex.toString());
			return null;
		}
		return filterLogsByStatus(status);
	}
	public int insertLog(String phoneName, String body, LogStatus status){
		ContentValues cv=new ContentValues();		
		cv.put("phone_name", phoneName);
		cv.put("body", body);
		cv.put("status", status.ordinal());
		SQLiteDatabase dbase=getWritableDatabase();
		int result=(int)dbase.insert("logs", null, cv);
		if(logs==null)
			getLogs(LogStatus.BLOCKED);
		logs.add(new LogItem(result, phoneName, body, new Date(), status.ordinal()));
		dbase.close();
		return result;
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
		SQLiteDatabase dbase=getReadableDatabase();
		tops=new ArrayList<TopItem>();
		Cursor cursor=dbase.rawQuery("SELECT * FROM tops", null);
		int idIdx=cursor.getColumnIndex("_id"), posIdx=cursor.getColumnIndex("pos"), votesIdx=cursor.getColumnIndex("votes"), phoneNameIdx=cursor.getColumnIndex("phone_name"), typeIdx=cursor.getColumnIndex("type");
		while(cursor.moveToNext()){			
			tops.add(new TopItem(cursor.getInt(idIdx), cursor.getInt(posIdx), cursor.getInt(votesIdx), cursor.getString(phoneNameIdx), cursor.getInt(typeIdx)));
		}
		cursor.close();
		dbase.close();
		return tops;
	}
	public void updateTop(List<TopItem> newTop){
		tops=newTop;
		SQLiteDatabase dbase=getWritableDatabase();
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
		dbase.close();
	}
}
