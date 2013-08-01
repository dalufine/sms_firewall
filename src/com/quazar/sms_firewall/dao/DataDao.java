package com.quazar.sms_firewall.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.models.Request;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;
import com.quazar.sms_firewall.models.TopItem;
import com.quazar.sms_firewall.models.TopItem.TopCategory;
import com.quazar.sms_firewall.models.TopItem.TopType;
import com.quazar.sms_firewall.network.ApiClient.ApiMethods;

public class DataDao extends SQLiteOpenHelper{
	private static final String DB_NAME="sms_firewall";
	private static final String[] createSql= {"CREATE TABLE filters (_id INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER, value TEXT NOT NULL)",
			"CREATE TABLE logs(_id INTEGER PRIMARY KEY AUTOINCREMENT, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, phone_name TEXT NOT NULL, body TEXT NOT NULL, status INTEGER DEFAULT 0)",
			"CREATE TABLE tops(_id INTEGER PRIMARY KEY, pos INTEGER NOT NULL, value TEXT NOT NULL, example TEXT, votes INTEGER DEFAULT 0, type INTEGER, category INTEGER)",
			"CREATE TABLE requests(_id INTEGER PRIMARY KEY AUTOINCREMENT, method TEXT NOT NULL, data TEXT NOT NULL, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"},
			dropSql= {"DROP TABLE IF EXISTS filters","DROP TABLE IF EXISTS logs","DROP TABLE IF EXISTS tops","DROP TABLE IF EXISTS requests"};

	private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	// cache
	private static List<SmsLogItem> logs=null;
	private static List<Filter> filters=null;	

	public DataDao(Context context){
		super(context, DB_NAME, null, 4);		
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
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();
			cursor=dbase.rawQuery("SELECT * FROM filters ORDER BY _id DESC", null);
			int idIdx=cursor.getColumnIndex("_id"), valueIdx=cursor.getColumnIndex("value"), typeIdx=cursor.getColumnIndex("type");
			while(cursor.moveToNext()){
				filters.add(new Filter(cursor.getInt(idIdx), cursor.getString(valueIdx), cursor.getInt(typeIdx)));
			}	
		}finally{
			cursor.close();
			dbase.close();
		}
		return filters;
	}
	public int insertFilter(FilterType type, String value){
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getWritableDatabase();
			cursor=dbase.rawQuery("SELECT * FROM filters WHERE type=? AND value=?", new String[]{String.valueOf(type.ordinal()), value});		
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
		}finally{
			cursor.close();
			dbase.close();
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
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			return dbase.delete("filters", "_id=?", new String[] {String.valueOf(id)});			
		}finally{
			dbase.close();
		}		
	}
	// ------------------Logs-----------------------------------
	public List<SmsLogItem> getLogs(LogStatus status){
		if(logs!=null)
			return filterLogsByStatus(status);
		try{
			logs=new ArrayList<SmsLogItem>();
			SQLiteDatabase dbase=null;
			Cursor cursor=null;
			try{
				dbase=getReadableDatabase();
				cursor=dbase.query("logs", null, null, null, null, null, "_id DESC");
				int idIdx=cursor.getColumnIndex("_id"), phoneNameIdx=cursor.getColumnIndex("phone_name"), bodyIdx=cursor.getColumnIndex("body"), addTimeIdx=cursor.getColumnIndex("add_time"), statusIdx=cursor.getColumnIndex("status");
				while(cursor.moveToNext()){				
					logs.add(new SmsLogItem(cursor.getInt(idIdx), cursor.getString(phoneNameIdx), cursor.getString(bodyIdx), sdf.parse(cursor.getString(addTimeIdx)), cursor.getInt(statusIdx)));
				}
			}finally{
				cursor.close();
				dbase.close();
			}
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
		try{
			int result=(int)dbase.insert("logs", null, cv);
			if(logs==null)
				getLogs(LogStatus.BLOCKED);
			logs.add(new SmsLogItem(result, phoneName, body, new Date(), status.ordinal()));
			return result;
		}finally{
			dbase.close();
		}		
	}
	private List<SmsLogItem> filterLogsByStatus(LogStatus status){
		List<SmsLogItem> result=new ArrayList<SmsLogItem>();
		for(SmsLogItem li:logs){
			if(li.getStatus()==status)
				result.add(li);
		}
		return result;
	}
	// ------------------Tops--------------------------------
	public List<TopItem> getTop(TopType type, TopCategory category){
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();
			List<TopItem> tops=new ArrayList<TopItem>();
			String sql="SELECT * FROM tops WHERE";
			boolean hasCond=false;
			if(type!=TopType.GENERIC){
				sql+=" type="+type.ordinal();
				hasCond=true;
			}
			if(category!=TopCategory.GENERIC){
				if(hasCond)
					sql+=" AND";
				sql+=" category="+category.ordinal();
			}
			cursor=dbase.rawQuery(sql, null);
			int idIdx=cursor.getColumnIndex("_id"), posIdx=cursor.getColumnIndex("pos"), votesIdx=cursor.getColumnIndex("votes"), valueIdx=cursor.getColumnIndex("value"), 
					typeIdx=cursor.getColumnIndex("type"), categoryIdx=cursor.getColumnIndex("category"), exampleIdx=cursor.getColumnIndex("example");
			while(cursor.moveToNext()){			
				tops.add(new TopItem(cursor.getInt(idIdx), cursor.getInt(posIdx), cursor.getInt(votesIdx), cursor.getString(valueIdx), cursor.getString(exampleIdx), cursor.getInt(typeIdx), cursor.getInt(categoryIdx)));
			}
			return tops;
		}finally{
			cursor.close();
			dbase.close();
		}		
	}
	public void updateTop(Set<TopItem> newTop){		
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			dbase.beginTransaction();
			dbase.delete("tops", null, null);
			for(TopItem ti:newTop){
				ContentValues cv=new ContentValues();
				cv.put("_id", ti.getId());
				cv.put("pos", ti.getPos());
				cv.put("value", ti.getValue());
				cv.put("example", ti.getExample());
				cv.put("votes", ti.getVotes());
				cv.put("type", ti.getType().ordinal());
				cv.put("category", ti.getCategory().ordinal());
				dbase.insert("tops", null, cv);
			}
			dbase.setTransactionSuccessful();
			dbase.endTransaction();
		}finally{
			dbase.close();
		}
	}
	//-----------------Requests------------------------
	public List<Request> getRequests(){	
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();		
			cursor=dbase.rawQuery("SELECT * FROM requests ORDER BY add_time", null);
			int methodIdx=cursor.getColumnIndex("method"), dataIdx=cursor.getColumnIndex("data");
			List<Request> requets=new ArrayList<Request>();			
			while(cursor.moveToNext()){			
				requets.add(new Request(ApiMethods.valueOf(cursor.getString(methodIdx)), new JSONObject(cursor.getString(dataIdx))));
			}
			return requets;
		}catch(Exception ex){
			Log.e("dao", ex.toString());
		}
		finally{
			cursor.close();
			dbase.close();
		}	
		return null;
	}
	public boolean clearRequests(){		
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			dbase.beginTransaction();
			dbase.delete("requests", null, null);		
			dbase.setTransactionSuccessful();
			dbase.endTransaction();
		}finally{
			dbase.close();
		}
		return true;
	}
	public int insertRequest(ApiMethods method, JSONObject data){
		ContentValues cv=new ContentValues();		
		cv.put("method", method.name());
		cv.put("data", data.toString());		
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			return (int)dbase.insert("requests", null, cv);
		}finally{
			dbase.close();
		}		
	}
}
