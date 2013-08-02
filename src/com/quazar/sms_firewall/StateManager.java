package com.quazar.sms_firewall;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;

import com.quazar.sms_firewall.activities.FiltersActivity;
import com.quazar.sms_firewall.activities.LogsActivity;
import com.quazar.sms_firewall.activities.SettingsActivity;
import com.quazar.sms_firewall.activities.TopsActivity;

public class StateManager {
	public static final int CONTACTS_REQUEST_ID=1;
	public static void getContact(Activity activity){
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);  
		activity.startActivityForResult(intent, CONTACTS_REQUEST_ID);
	}
	public static void showFilters(Activity activity){
		Intent intent = new Intent(activity, FiltersActivity.class);		
		activity.startActivity(intent);
	}
	public static void showLogs(final Activity activity){
		//TODO for test time
		/*PasswordDialog pd=new PasswordDialog(activity, (String)Param.LOGS_PASSWORD.getValue(), new DialogListener<String>() {			
			@Override
			public void ok(String value) {
				Intent intent = new Intent(activity, LogsActivity.class);		
				activity.startActivity(intent);
			}			
			@Override
			public void cancel() {
			}
		});
		pd.show();*/
		Intent intent = new Intent(activity, LogsActivity.class);		
		activity.startActivity(intent);
	}
	public static void showTops(Activity activity){
		Intent intent = new Intent(activity, TopsActivity.class);		
		activity.startActivity(intent);
	}	
	public static void showSettings(Activity activity){
		Intent intent = new Intent(activity, SettingsActivity.class);		
		activity.startActivity(intent);
	}	
}
