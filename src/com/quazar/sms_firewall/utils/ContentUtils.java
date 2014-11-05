package com.quazar.sms_firewall.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

public class ContentUtils {
	public static final String NUMBER = "number", DATE="date",
			TEXT = "text", NAME = "name";

	public static String getPhoneNumber(Context activity, Uri contentUri) {
		ContentResolver cr = activity.getContentResolver();
		Cursor cur = cr.query(contentUri, null, null, null, null);
		while (cur.moveToNext()) {
			String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
			if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
				Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
				if(pCur.moveToNext()) {
					return pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[\\-\\s\\(\\)]", "");
				}
				pCur.close();
			}
		}
		cur.close();
		return null;
	}

	public static List<HashMap<String, Object>> getInboxSms(Context context) {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/inbox"),
				new String[] { "address", "body", "date" }, null, null, null);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		while (cursor.moveToNext()) {
			HashMap<String, Object> sms = new HashMap<String, Object>();
			String contact=cursor.getString(0);
			String contactName=DictionaryUtils.getInstance().getContactsName(contact);
			sms.put(NUMBER, contactName.equals(contact)?contact:(contactName+" / "+ contact));						
			sms.put(TEXT, cursor.getString(1));
			Long dateTime=cursor.getLong(2);
			sms.put(DATE, sdf.format(new Date(dateTime)));
			list.add(sms);
		}
		return list;
	}

	public static SimpleDateFormat getDateFormater() {
		Locale locale = Locale.getDefault();		
		if (locale.getCountry() == "RU") {
			return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		}
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public static String secondsToTime(int secs) {
		int hours = secs / 3600, remainder = secs % 3600, minutes = remainder / 60, seconds = remainder % 60;
		return String.format("%s:%s:%s", (hours < 10 ? "0" : "") + hours,
				(minutes < 10 ? "0" : "") + minutes, (seconds < 10 ? "0" : "")+ seconds);

	}

	public static List<HashMap<String, Object>> getIncomeCalls(Context context) {		
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		Cursor cursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				new String[] { "name", "number", "date", "duration" },
				"type=?",
				new String[] { String.valueOf(CallLog.Calls.INCOMING_TYPE) },
				null);
		while (cursor.moveToNext()) {
			HashMap<String, Object> calls = new HashMap<String, Object>();
			String name = cursor.getString(0), number = cursor.getString(1);
			calls.put(NAME, name == null ? number : name);
			calls.put(NUMBER, number);
			calls.put(DATE, getDateFormater().format(new Date(cursor.getLong(2))));
			calls.put(TEXT, secondsToTime(cursor.getInt(3)));
			list.add(calls);
		}
		cursor.close();
		return list;
	}
}
