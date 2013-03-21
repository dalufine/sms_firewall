package com.quazar.sms_firewall.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.popups.SelectSourcePopup;
import com.quazar.sms_firewall.popups.SelectSourcePopup.SelectSourceListener;
import com.quazar.sms_firewall.utils.ContentUtils;

public class MainActivity extends Activity {
	// private static DataDao dataDao;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ContentUtils.getInboxSms(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onPhoneNumberClick(View v) {
		AlertDialog sourceSelect = new SelectSourcePopup(this,
				new SelectSourceListener() {
					@Override
					public void recieveSelection(int selection) {
						switch (selection) {
						case SelectSourcePopup.FROM_CONTACTS:
							StateManager.getContact(MainActivity.this);
							break;
						default:
							break;
						}
					}
				});
		sourceSelect.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == StateManager.CONTACTS_REQUEST_ID
				&& resultCode == Activity.RESULT_OK) {
			String phoneNumber = ContentUtils.getPhoneNumber(this,
					data.getData());
			Toast.makeText(this, "Phone number: " + phoneNumber,
					Toast.LENGTH_LONG).show();
		}
	}

}
