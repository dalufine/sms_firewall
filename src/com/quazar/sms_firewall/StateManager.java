package com.quazar.sms_firewall;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.quazar.sms_firewall.activities.FiltersActivity;
import com.quazar.sms_firewall.activities.LogsActivity;
import com.quazar.sms_firewall.activities.SettingsActivity;
import com.quazar.sms_firewall.activities.TopsActivity;
import com.quazar.sms_firewall.dialogs.EnterPasswordDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;

public class StateManager{
	public static final int CONTACTS_REQUEST_ID=1;

	public static void getContact(Activity activity){
		Intent intent=new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		intent.setType(Phone.CONTENT_TYPE);
		activity.startActivityForResult(intent, CONTACTS_REQUEST_ID);
	}
	public static void showFilters(Activity activity){
		Intent intent=new Intent(activity, FiltersActivity.class);
		activity.startActivity(intent);
	}
	// 
	public static void showLogs(final Activity activity){
		EnterPasswordDialog dialog=new EnterPasswordDialog(activity, (String)Param.PASSWORD.getValue(), new DialogListener<String>(){
			@Override
			public void ok(String value){				
				Intent intent=new Intent(activity, LogsActivity.class);
				activity.startActivity(intent);
			}
			@Override
			public void cancel(){}
		});
		dialog.show();
	}
	public static void showTops(Activity activity){
		Intent intent=new Intent(activity, TopsActivity.class);
		activity.startActivity(intent);
	}
	public static void showSettings(Activity activity){
		Intent intent=new Intent(activity, SettingsActivity.class);
		activity.startActivity(intent);
	}
}
