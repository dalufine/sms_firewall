package com.quazar.sms_firewall.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

public class ContentUtils {
	public static final String SMS_NUMBER = "sms_number",
			SMS_TEXT = "sms_text";

	public static String getPhoneNumber(Context activity, Uri contentUri) {
		ContentResolver cr = activity.getContentResolver();
		Cursor cur = cr.query(contentUri, null, null, null, null);
		while (cur.moveToNext()) {
			String id = cur.getString(cur
					.getColumnIndex(ContactsContract.Contacts._ID));
			if (Integer
					.parseInt(cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
				Cursor pCur = cr.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { id }, null);
				while (pCur.moveToNext()) {
					return pCur
							.getString(
									pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
							.replaceAll("[\\-\\s\\(\\)]", "");
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
				Uri.parse("content://sms/inbox"), null, null, null, null);
		while (cursor.moveToNext()) {
			HashMap<String, Object> sms = new HashMap<String, Object>();
			sms.put(SMS_NUMBER,
					cursor.getString(cursor.getColumnIndex("address")));
			sms.put(SMS_TEXT, cursor.getString(cursor.getColumnIndex("body")));
			list.add(sms);
		}
		return list;
	}

	private void getCallDetails(Context context) {

		StringBuffer sb = new StringBuffer();
		Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, 
				new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION},
				"type=?", new String[]{String.valueOf(CallLog.Calls.INCOMING_TYPE)}, null);		
		sb.append("Call Details :");
		while (managedCursor.moveToNext()) {
			String phNumber = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.NUMBER));
			String callType = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.TYPE));
			String callDate = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DATE));
			Date callDayTime = new Date(Long.valueOf(callDate));
			String callDuration = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DURATION));
			String dir = null;
			int dircode = Integer.parseInt(callType);
			switch (dircode) {
			case CallLog.Calls.OUTGOING_TYPE:
				dir = "OUTGOING";
				break;

			case CallLog.Calls.INCOMING_TYPE:
				dir = "INCOMING";
				break;

			case CallLog.Calls.MISSED_TYPE:
				dir = "MISSED";
				break;
			}
			sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
					+ dir + " \nCall Date:--- " + callDayTime
					+ " \nCall duration in sec :--- " + callDuration);
			sb.append("\n----------------------------------");
		}
		managedCursor.close();
		call.setText(sb);
	}
}
