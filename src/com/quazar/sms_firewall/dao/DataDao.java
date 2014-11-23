package com.quazar.sms_firewall.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.quazar.sms_firewall.models.LogFilter;
import com.quazar.sms_firewall.models.Request;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;
import com.quazar.sms_firewall.models.TopFilter;
import com.quazar.sms_firewall.models.TopFilter.TopCategory;
import com.quazar.sms_firewall.models.TopFilter.TopType;
import com.quazar.sms_firewall.models.UserFilter;
import com.quazar.sms_firewall.models.UserFilter.FilterType;
import com.quazar.sms_firewall.utils.DictionaryUtils;

public class DataDao extends SQLiteOpenHelper{
	private static final String DB_NAME="sms_firewall";
	private static final String[] createSql= { "CREATE TABLE user_filters (_id INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER, value TEXT NOT NULL)",
			"CREATE TABLE logs(_id INTEGER PRIMARY KEY AUTOINCREMENT, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, phone_name TEXT NOT NULL, body TEXT NOT NULL, status INTEGER DEFAULT 0)",
			"CREATE TABLE top_filters(_id INTEGER PRIMARY KEY, pos INTEGER NOT NULL, value TEXT NOT NULL, votes INTEGER DEFAULT 0, type INTEGER, category INTEGER)",
			"CREATE TABLE top_filter_examples(_id INTEGER PRIMARY KEY AUTOINCREMENT, filter_id INTEGER, example TEXT NOT NULL)",
			"CREATE TABLE requests(_id INTEGER PRIMARY KEY AUTOINCREMENT, method TEXT NOT NULL, data TEXT NOT NULL, add_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)" }, dropSql= { "DROP TABLE IF EXISTS user_filters",
			"DROP TABLE IF EXISTS logs", "DROP TABLE IF EXISTS filters", "DROP TABLE IF EXISTS filter_examples", "DROP TABLE IF EXISTS requests" };

	private static final SimpleDateFormat sqlFromFormat=new SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.US), sqlToFormat=new SimpleDateFormat("yyyy-MM-dd 23:59:59", Locale.US);

	public DataDao(Context context){
		super(context, DB_NAME, null, 5);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.beginTransaction();
		for(String s:createSql)
			db.execSQL(s);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.beginTransaction();
		for(String s:dropSql)
			db.execSQL(s);
		for(String s:createSql)
			db.execSQL(s);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	// ------------------Filters-------------------------
	public List<UserFilter> getUserFilters(){
		List<UserFilter> filters=new ArrayList<UserFilter>();
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();
			cursor=dbase.rawQuery("SELECT * FROM user_filters ORDER BY _id DESC", null);
			int idIdx=cursor.getColumnIndex("_id"), valueIdx=cursor.getColumnIndex("value"), typeIdx=cursor.getColumnIndex("type");
			while(cursor.moveToNext()){
				filters.add(new UserFilter(cursor.getLong(idIdx), cursor.getString(valueIdx), cursor.getInt(typeIdx)));
			}
		}finally{
			cursor.close();
			dbase.close();
		}
		return filters;
	}

	public int insertUserFilter(FilterType type, String value){		
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getWritableDatabase();
			cursor=dbase.rawQuery("SELECT * FROM user_filters WHERE type=? AND value=?", new String[] { String.valueOf(type.ordinal()), value });
			if(cursor.getCount()==0){
				ContentValues cv=new ContentValues();
				cv.put("value", value);
				cv.put("type", type.ordinal());
				int result=(int)dbase.insert("user_filters", null, cv);
				cursor.close();
				return result;
			}
		}finally{
			cursor.close();
			dbase.close();
		}
		return 0;
	}

	public int deleteUserFilter(long id){
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			return dbase.delete("user_filters", "_id=?", new String[] { String.valueOf(id) });
		}finally{
			dbase.close();
		}
	}

	public int clearUserFilters(FilterType type){
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			return dbase.delete("user_filters", "type=?", new String[] { String.valueOf(type.ordinal()) });
		}finally{
			dbase.close();
		}
	}

	public void addAllToUserFilters(TopType type, TopCategory category){
		try{
			List<TopFilter> tops=getTopFilters(type, category);
			FilterType ftype=FilterType.PHONE_NAME;
			if(type==TopType.WORD){
				ftype=FilterType.WORD;
			}
			for(TopFilter item:tops){
				insertUserFilter(ftype, item.getValue());
			}
		}catch(Exception ex){
			Log.e("db", "add all top filter to user filters error", ex);
		}
	}
	// ------------------Logs-----------------------------------
	public List<SmsLogItem> getLogs(LogStatus status, LogFilter filter, Integer offset, Integer limit){
		try{
			List<SmsLogItem> logs=new ArrayList<SmsLogItem>();
			SQLiteDatabase dbase=null;
			Cursor cursor=null;
			try{
				dbase=getReadableDatabase();
				String where="status="+status.ordinal();
				if(filter!=null){
					where=addWhereFilter(where, "body LIKE '%%%s%%'", filter.getBodyLike());
					where=addWhereFilter(where, "phone_name='%s'", filter.getPhoneName());
					where=addWhereFilter(where, "add_time>='%s'", (filter.getFrom()==null)?null:sqlFromFormat.format(filter.getFrom()));
					where=addWhereFilter(where, "add_time<='%s'", (filter.getTo()==null)?null:sqlToFormat.format(filter.getTo()));
				}
				if(offset==null)
					offset=0;
				if(limit==null)
					limit=100;
				cursor=dbase.rawQuery("SELECT * FROM logs WHERE "+where+" ORDER BY _id DESC LIMIT ?,?", new String[] { ""+offset, ""+limit });
				int idIdx=cursor.getColumnIndex("_id"), phoneNameIdx=cursor.getColumnIndex("phone_name"), bodyIdx=cursor.getColumnIndex("body"), addTimeIdx=cursor.getColumnIndex("add_time"), statusIdx=
						cursor.getColumnIndex("status");
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
				while(cursor.moveToNext()){
					SmsLogItem sms=new SmsLogItem();
					sms.setId(cursor.getLong(idIdx));
					String number=cursor.getString(phoneNameIdx);
					String name=DictionaryUtils.getInstance().getContactsName(number);					
					sms.setName(name != null ? name: number);
					sms.setNumber(name != null && !name.equalsIgnoreCase(number) ? number
							+ " " : "");
					sms.setBody(cursor.getString(bodyIdx));
					sms.setDate(sdf.parse(cursor.getString(addTimeIdx)));
					sms.setStatus(cursor.getInt(statusIdx));
					logs.add(sms);
				}
				return logs;
			}finally{
				cursor.close();
				dbase.close();
			}
		}catch(Exception ex){
			Log.e("logs loading", ex.toString());
			return null;
		}
	}

	//Util
	private static String addWhereFilter(String where, String expression, String substitute){
		if(substitute==null)
			return where;
		if(where==null||where.trim().length()==0){
			where=String.format(expression, substitute);
		}else{
			where+=(" AND "+String.format(expression, substitute));
		}
		return where;
	}

	public int insertLog(String phoneName, String body, LogStatus status){
		ContentValues cv=new ContentValues();
		cv.put("phone_name", phoneName);
		cv.put("body", body);
		cv.put("status", status.ordinal());
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			int result=(int)dbase.insert("logs", null, cv);
			return result;
		}finally{
			dbase.close();
		}
	}

	public int clearLogs(LogStatus status){
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			int result=(int)dbase.delete("logs", "status=?", new String[] { ""+status.ordinal() });
			return result;
		}finally{
			dbase.close();
		}
	}

	// ------------------Tops--------------------------------
	public List<TopFilter> getTopFilters(TopType type, TopCategory category){
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();
			List<TopFilter> tops=new ArrayList<TopFilter>();
			String sql="SELECT * FROM top_filters WHERE";
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
			sql+=" ORDER BY pos";
			cursor=dbase.rawQuery(sql, null);
			int idIdx=cursor.getColumnIndex("_id"), posIdx=cursor.getColumnIndex("pos"), votesIdx=cursor.getColumnIndex("votes"), valueIdx=cursor.getColumnIndex("value"), typeIdx=cursor.getColumnIndex("type"), categoryIdx=
					cursor.getColumnIndex("category");
			while(cursor.moveToNext()){
				long id=cursor.getLong(idIdx);
				tops.add(new TopFilter(id, cursor.getInt(posIdx), cursor.getInt(votesIdx), cursor.getString(valueIdx), cursor.getInt(typeIdx), cursor.getInt(categoryIdx)));
			}
			return tops;
		}finally{
			cursor.close();
			dbase.close();
		}
	}

	public List<String> getTopFilterExamples(long filterId){
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		List<String> examples=new ArrayList<String>();
		try{
			dbase=getReadableDatabase();
			cursor=dbase.rawQuery("SELECT example FROM top_filter_examples WHERE filter_id="+filterId, null);
			int exampleIdx=cursor.getColumnIndex("example");
			while(cursor.moveToNext()){
				examples.add(cursor.getString(exampleIdx));
			}
		}finally{
			cursor.close();
			dbase.close();
		}
		return examples;
	}

	public List<TopFilter> getAllTopFilters(){
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();
			List<TopFilter> tops=new ArrayList<TopFilter>();
			String sql="SELECT * FROM top_filters";
			cursor=dbase.rawQuery(sql, null);
			int idIdx=cursor.getColumnIndex("_id"), posIdx=cursor.getColumnIndex("pos"), votesIdx=cursor.getColumnIndex("votes"), valueIdx=cursor.getColumnIndex("value"), typeIdx=cursor.getColumnIndex("type"), categoryIdx=
					cursor.getColumnIndex("category");
			while(cursor.moveToNext()){
				long id=cursor.getLong(idIdx);
				tops.add(new TopFilter(id, cursor.getInt(posIdx), cursor.getInt(votesIdx), cursor.getString(valueIdx), cursor.getInt(typeIdx), cursor.getInt(categoryIdx)));
			}
			return tops;
		}finally{
			cursor.close();
			dbase.close();
		}
	}

	public void updateTopFilters(Set<TopFilter> newTop, Map<Long, List<String>> examples){
		if(newTop.isEmpty())
			return;
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			dbase.beginTransaction();
			dbase.delete("top_filters", null, null);
			dbase.delete("top_filter_examples", null, null);
			List<Long> exclude=new ArrayList<Long>();
			for(TopFilter ti:newTop){
				ContentValues cv=new ContentValues();
				if(exclude.contains(ti.getId()))
					continue;
				exclude.add(ti.getId());
				cv.put("_id", ti.getId());
				cv.put("pos", ti.getPos());
				cv.put("value", ti.getValue());
				cv.put("votes", ti.getVotes());
				cv.put("type", ti.getType().ordinal());
				cv.put("category", ti.getCategory().ordinal());
				dbase.insert("top_filters", null, cv);
				List<String> examplesList=examples.get(ti.getId());
				if(examplesList!=null&&!examplesList.isEmpty()){
					for(String example:examplesList){
						cv=new ContentValues();
						cv.put("filter_id", ti.getId());
						cv.put("example", example);
						dbase.insert("top_filter_examples", null, cv);
					}
				}
			}
			dbase.setTransactionSuccessful();
			dbase.endTransaction();
		}finally{
			dbase.close();
		}
	}

	public void updateTopFilterVotes(long id, Integer votes){
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			ContentValues cv=new ContentValues();
			cv.put("votes", votes);
			dbase.update("top_filters", cv, "_id=?", new String[] { Long.toString(id) });
		}finally{
			dbase.close();
		}
	}
	// -----------------Requests------------------------
	public List<Request> getRequests(){
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();
			cursor=dbase.rawQuery("SELECT * FROM requests ORDER BY add_time", null);
			int methodIdx=cursor.getColumnIndex("method"), dataIdx=cursor.getColumnIndex("data"), idIdx=cursor.getColumnIndex("_id");
			List<Request> requets=new ArrayList<Request>();
			while(cursor.moveToNext()){
				requets.add(new Request(cursor.getLong(idIdx), cursor.getString(methodIdx), new JSONObject(cursor.getString(dataIdx))));
			}
			return requets;
		}catch(Exception ex){
			Log.e("dao", ex.toString());
		}finally{
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

	public int deleteRequest(long requestId){
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			return dbase.delete("requests", "_id=?", new String[] { String.valueOf(requestId) });
		}finally{
			dbase.close();
		}
	}

	public int insertRequest(String serviceUrl, JSONObject data){
		ContentValues cv=new ContentValues();
		cv.put("method", serviceUrl);
		cv.put("data", data.toString());
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			return (int)dbase.insert("requests", null, cv);
		}finally{
			dbase.close();
		}
	}

	public int insertUserFilters(List<UserFilter> filters){
		SQLiteDatabase dbase=getWritableDatabase();
		try{
			dbase.beginTransaction();
			for(UserFilter f:filters){
				ContentValues cv=new ContentValues();
				cv.put("type", f.getType().ordinal());
				cv.put("value", f.getValue());
				dbase.insert("user_filters", null, cv);
			}
			dbase.setTransactionSuccessful();
			dbase.endTransaction();
			return filters.size();
		}catch(Exception ex){
			Log.e("insert user_filters error", ex.toString());
			return 0;
		}finally{
			dbase.close();
		}
	}

	public List<String> getLogSenders(){
		SQLiteDatabase dbase=null;
		Cursor cursor=null;
		try{
			dbase=getReadableDatabase();
			cursor=dbase.rawQuery("SELECT DISTINCT phone_name FROM logs", null);
			List<String> senders=new ArrayList<String>();
			while(cursor.moveToNext()){
				senders.add(cursor.getString(0));
			}
			return senders;
		}catch(Exception ex){
			Log.e("dao", ex.toString());
		}finally{
			cursor.close();
			dbase.close();
		}
		return null;
	}	
}
