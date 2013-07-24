package com.quazar.sms_firewall.activities;

import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.network.ApiClient;
import com.quazar.sms_firewall.popups.CallsSelectPopup;
import com.quazar.sms_firewall.popups.EnterValuePopup;
import com.quazar.sms_firewall.popups.SelectListener;
import com.quazar.sms_firewall.popups.SelectSourcePopup;
import com.quazar.sms_firewall.popups.SmsSelectPopup;
import com.quazar.sms_firewall.utils.ContentUtils;
import com.quazar.sms_firewall.utils.DictionaryUtils;

public class MainActivity extends Activity{
	private static DataDao dataDao;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		dataDao=new DataDao(this);
		DictionaryUtils.createInstance(this);		
		Param.load(this);
		ApiClient api=new ApiClient();
		api.sync(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);		
		updateStatisticsViews();
	}
	@Override
	protected void onResume() {		
		super.onResume();
		updateStatisticsViews();
	}
	private void updateStatisticsViews(){
		TextView tv=(TextView)findViewById(R.id.stat_blocked);
		tv.setText(String.format(tv.getText().toString().replaceAll("\\d", "%d"), (Integer)Param.BLOCKED_SMS_CNT.getValue()));
		tv=(TextView)findViewById(R.id.stat_recieved);
		tv.setText(String.format(tv.getText().toString().replaceAll("\\d", "%d"), (Integer)Param.RECIEVED_SMS_CNT.getValue()));
		tv=(TextView)findViewById(R.id.stat_suspicious);
		tv.setText(String.format(tv.getText().toString().replaceAll("\\d", "%d"), (Integer)Param.SUSPICIOUS_SMS_CNT.getValue()));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onPhoneNumberClick(View v){
		AlertDialog sourceSelect=new SelectSourcePopup(this, new SelectListener<Integer>(){
			@Override
			public void recieveSelection(Integer selection){
				switch(selection){
					case SelectSourcePopup.FROM_CONTACTS:
						StateManager.getContact(MainActivity.this);
						break;
					case SelectSourcePopup.FROM_INBOX_SMS:
						SmsSelectPopup smsPopup=new SmsSelectPopup(MainActivity.this, new SelectListener<HashMap<String, Object>>(){
							@Override
							public void recieveSelection(HashMap<String, Object> selection){
								dataDao.insertFilter(FilterType.PHONE_NAME, (String)selection.get(ContentUtils.SMS_NUMBER));
							}
						});
						smsPopup.show();
						break;
					case SelectSourcePopup.FROM_INCOME_CALLS:
						CallsSelectPopup callsPopup=new CallsSelectPopup(MainActivity.this, new SelectListener<HashMap<String, Object>>(){
							@Override
							public void recieveSelection(HashMap<String, Object> selection){
								dataDao.insertFilter(FilterType.PHONE_NAME, (String)selection.get(ContentUtils.CALLS_NUMBER));
							}
						});
						callsPopup.show();
						break;
					default:
						break;
				}
			}
		});
		sourceSelect.show();
	}
	public void onShowFilters(View v){
		StateManager.showFilters(this);
	}
	public void onShowLogs(View v){
		StateManager.showLogs(this);
	}
	public void onShowTops(View v){
		StateManager.showTops(this);
	}
	public void onShowSettings(View v){
		StateManager.showSettings(this);
	}
	public void onWordClick(View v){
		EnterValuePopup popup=new EnterValuePopup(this, getResources().getString(R.string.enter_word), new SelectListener<String>(){
			@Override
			public void recieveSelection(String selection){
				dataDao.insertFilter(FilterType.WORD, selection);
			}
		});
		popup.show();
		/*PasswordPopup popup=new PasswordPopup(this, "¬ведите пароль дл€ доступа к журналу", new DialogListener<String>(){
			
			@Override
			public void ok(String value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void cancel(){
				// TODO Auto-generated method stub
				
			}
		});
		popup.show();*/
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==StateManager.CONTACTS_REQUEST_ID&&resultCode==Activity.RESULT_OK){
			String phoneNumber=ContentUtils.getPhoneNumber(this, data.getData());
			dataDao.insertFilter(FilterType.PHONE_NAME, phoneNumber);
		}
	}

}
