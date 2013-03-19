package com.quazar.sms_firewall.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.popups.SelectSourcePopup;

public class MainActivity extends Activity {
	//private static DataDao dataDao;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/*dataDao=new DataDao(getApplicationContext());
		int result=dataDao.insertFilter(FilterType.WORD, "���");
		Log.i("sql", "result: "+result);
		Cursor cursor = getContentResolver().query(
				Uri.parse("content://sms/inbox"), null, null, null, null);
		cursor.moveToFirst();
		List<SmsModel> smsList = new ArrayList<SmsModel>();
		for (int i = 0; i < cursor.getCount(); i++) {
			smsList.add(new SmsModel(cursor.getInt(0), cursor.getString(2), cursor.getString(11), 
					cursor.getInt(15) == 1 ? true : false, new Date(cursor.getLong(4))));
			cursor.moveToNext();
			Log.i("sms", smsList.get(smsList.size()-1).toString());
		}*/		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	public void onPhoneNumberClick(View v){
		AlertDialog sourceSelect=new SelectSourcePopup(this);
		sourceSelect.show();
	}

}
