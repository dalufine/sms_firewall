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
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.R;

public class ContentUtils{

	public static final String NUMBER = "number", PROC_NUMBER = "proc_number", DATE = "date", TEXT = "text",
			NAME = "name";

	public static String getPhoneNumber(Context activity, Uri contactUri){
		String[] projection = {Phone.NUMBER };
		Cursor cursor = null;
		try {
			cursor = activity.getContentResolver().query(contactUri, projection, null, null, null);
			cursor.moveToFirst();
			String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
			if (number != null) {
				return number.replaceAll("[\\-\\s\\(\\)]", "");
			}
			return null;
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static String getFormatedPhoneNumber(Context context, String value){
		try {
			if (value.replaceAll("\\D", "").length() < 9) {
				return value;
			}
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			String region = Locale.getDefault().getCountry();
			if (region == null || region.trim().length() == 0) {
				region = ((Locale) Param.DEFAULT_LOCALE.getValue()).getCountry();
			}
			PhoneNumber phoneNumber = phoneUtil.parse(value, region);
			value = phoneUtil.format(phoneNumber, PhoneNumberFormat.E164);
		}
		catch(Exception ex) {
			LogUtil.error(context, "getFormatedPhoneNumber: " + value, ex);
		}
		return value;
	}

	public void defaineRegion(String value){
		value = value.trim();
		if (value.length() >= 9 && value.charAt(0) != '+' && value.contains("(")) {
			int pos = value.lastIndexOf('(');
			int country = Integer.valueOf(value.substring(0, pos).trim());
			value = "+" + (country - 1) + value.substring(pos);
		}
	}

	public static List<HashMap<String, Object>> getInboxSms(Context context) throws Exception{
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		Cursor cursor =
				context.getContentResolver().query(Uri.parse("content://sms/inbox"),
						new String[] {"address", "body", "date" }, null, null, null);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		while(cursor.moveToNext()) {
			HashMap<String, Object> sms = new HashMap<String, Object>();
			String number = cursor.getString(0);
			String name = DictionaryUtils.getInstance().getContactsName(number);
			sms.put(NAME, name != null?name:number);
			sms.put(NUMBER, getFormatedPhoneNumber(context, name != null && !name.equalsIgnoreCase(number)?number + " "
					:""));
			sms.put(PROC_NUMBER, number);
			sms.put(TEXT, cursor.getString(1));
			Long dateTime = cursor.getLong(2);
			sms.put(DATE, sdf.format(new Date(dateTime)));
			list.add(sms);
		}
		return list;
	}

	public static SimpleDateFormat getDateFormater(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	}

	public static String secondsToTime(int secs){
		int hours = secs / 3600, remainder = secs % 3600, minutes = remainder / 60, seconds = remainder % 60;
		return String.format("%s:%s:%s", (hours < 10?"0":"") + hours, (minutes < 10?"0":"") + minutes, (seconds < 10
				?"0":"")
				+ seconds);
	}

	public static List<HashMap<String, Object>> getIncomeCalls(Context context) throws Exception{
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		Cursor cursor =
				context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
						new String[] {"name", "number", "date", "duration" }, "type=?",
						new String[] {String.valueOf(CallLog.Calls.INCOMING_TYPE) }, null);
		SimpleDateFormat sdf =
				new SimpleDateFormat(context.getString(R.string.default_datetime_format), Locale.getDefault());
		while(cursor.moveToNext()) {
			HashMap<String, Object> calls = new HashMap<String, Object>();
			String name = cursor.getString(0);
			String number = cursor.getString(1);
			calls.put(NAME, name != null?name:number);
			calls.put(NUMBER, getFormatedPhoneNumber(context, name != null && !name.equalsIgnoreCase(number)?number
					+ " ":""));
			calls.put(PROC_NUMBER, number);
			calls.put(DATE, sdf.format(new Date(cursor.getLong(2))));
			calls.put(TEXT, secondsToTime(cursor.getInt(3)));
			list.add(calls);
		}
		cursor.close();
		return list;
	}
}
