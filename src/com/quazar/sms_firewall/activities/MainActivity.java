package com.quazar.sms_firewall.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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
import com.quazar.sms_firewall.dialogs.RegistrationDialog;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.network.ApiClient;
import com.quazar.sms_firewall.utils.ContentUtils;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.DictionaryUtils;

public class MainActivity extends Activity{
	private static DataDao dataDao;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		dataDao=new DataDao(this);
		DictionaryUtils.createInstance(this);		
		Param.load(this);
		if((Boolean)Param.USE_SYNC.getValue()&&(System.currentTimeMillis()-(Long)Param.LAST_SYNC.getValue())/86400000L>2){
			ApiClient api=new ApiClient(this);
			api.sync();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);		
		updateStatisticsViews();
		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(777);
		if((Boolean)Param.IS_NEW.getValue()){
			RegistrationDialog rd=new RegistrationDialog(this);
			rd.setOnCancelListener(new DialogInterface.OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
			rd.show();
		}
	}
	@Override
	protected void onResume() {		
		super.onResume();
		updateStatisticsViews();
	}
	private void updateStatisticsViews(){
		TextView tv=(TextView)findViewById(R.id.stat_blocked);
		tv.setText(String.format(getResources().getString(R.string.stat_blocked), (Integer)Param.BLOCKED_SMS_CNT.getValue()));
		tv=(TextView)findViewById(R.id.stat_recieved);
		tv.setText(String.format(getResources().getString(R.string.stat_recieved), (Integer)Param.RECIEVED_SMS_CNT.getValue()));
		tv=(TextView)findViewById(R.id.stat_suspicious);
		tv.setText(String.format(getResources().getString(R.string.stat_suspicious), (Integer)Param.SUSPICIOUS_SMS_CNT.getValue()));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu){		
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onPhoneNumberClick(View v){
		DialogUtils.showSourceSelectPopup(this, null);
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
		DialogUtils.showEnterWordFilterPopup(this, null);		
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
