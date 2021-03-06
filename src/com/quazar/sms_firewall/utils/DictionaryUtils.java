package com.quazar.sms_firewall.utils;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class DictionaryUtils {
	private HashMap<String, String> contactsMap;
	private static DictionaryUtils instance;
	public static void createInstance(Context context){
		if(instance==null){
			instance=new DictionaryUtils();			
			Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null, null);
			instance.contactsMap=new HashMap<String, String>();
			while (cursor.moveToNext()) {
				String name =cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				if(name.equals("�� ��������")){
					Log.i("", "");
				}				
				String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				instance.contactsMap.put(ContentUtils.getFormatedPhoneNumber(context, phoneNumber), name);
			}			
		}		
	}
	
	public static DictionaryUtils getInstance() {
		return instance;
	}

	public String getContactsName(String phoneName){		
		if(contactsMap!=null){
			String name=contactsMap.get(phoneName);
			if(name!=null)
				return name;
		}
		return phoneName;
	}
}
