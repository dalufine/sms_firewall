package com.quazar.sms_firewall.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContentUtils {
	public static final int SMS_NUMBER = 0, SMS_TEXT = 1;

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

	public static List<HashMap<Integer, Object>> getInboxSms(Context context){
		List<HashMap<Integer, Object>> list=new ArrayList<HashMap<Integer, Object>>();
		Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);		
		while(cursor.moveToNext()){
			HashMap<Integer, Object> sms=new HashMap<Integer, Object>();
			sms.put(SMS_NUMBER, cursor.getString(cursor.getColumnIndex("address")));
			sms.put(SMS_TEXT, cursor.getString(cursor.getColumnIndex("body")));
			list.add(sms);
		}
		return list;
	}
}
