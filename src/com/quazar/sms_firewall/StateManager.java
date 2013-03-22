package com.quazar.sms_firewall;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;

public class StateManager {
	public static final int CONTACTS_REQUEST_ID=1;
	public static void getContact(Activity activity){
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);  
		activity.startActivityForResult(intent, CONTACTS_REQUEST_ID);
	}	
}
