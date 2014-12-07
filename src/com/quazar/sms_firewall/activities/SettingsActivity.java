package com.quazar.sms_firewall.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.EnterPasswordDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.LocaleUtils;

public class SettingsActivity extends BaseActivity{
	private Spinner langSpinner;
	private CheckBox syncCheckBox;
	private CheckBox top100CheckBox;
	private DataDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState){		
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_settings);
		dao=new DataDao(this);
		langSpinner=((Spinner)findViewById(R.id.language_select));
		if(Param.LOCALE.getValue()!=null&&((String)Param.LOCALE.getValue()).length()>0){
			for(int i=0;i<langSpinner.getCount();i++){
				String lang=((String)langSpinner.getItemAtPosition(i)).substring(1, 3);
				if(lang.equalsIgnoreCase((String)Param.LOCALE.getValue())){
					langSpinner.setSelection(i);
					break;
				}
			}
		}
		syncCheckBox=((CheckBox)findViewById(R.id.sync_on));
		syncCheckBox.setChecked((Boolean)Param.USE_SYNC.getValue());
		top100CheckBox=((CheckBox)findViewById(R.id.top_filter_on));
		top100CheckBox.setChecked((Boolean)Param.USE_TOP_100.getValue());
	}

	public void onClearLogs(View view){
		DialogUtils.showConfirmDialog(this, getString(R.string.confirmation), getString(R.string.all_logs_clear_conf), new DialogListener<Boolean>(){
			@Override
			public void ok(Boolean value){
				dao.clearLogs(null);
				Toast.makeText(SettingsActivity.this, R.string.logs_empty, Toast.LENGTH_SHORT).show();
			}
			@Override
			public void cancel(){}
		});
	}

	public void onResetLogsPassword(View view){
		EnterPasswordDialog dialog=new EnterPasswordDialog(this, R.string.enter_new_password, null, new DialogListener<String>(){
			@Override
			public void ok(String value){
				Param.LOGS_PASSWORD.setValue(value);
				Toast.makeText(SettingsActivity.this, R.string.password_saved, Toast.LENGTH_SHORT).show();
			}
			@Override
			public void cancel(){}
		});
		dialog.show();		
	}

	public void onResetStatistics(View view){
		DialogUtils.showConfirmDialog(this, getString(R.string.confirmation), getString(R.string.reset_statistics_conf), new DialogListener<Boolean>(){
			@Override
			public void ok(Boolean value){
				Param.BLOCKED_SMS_CNT.setValue(0);
				Param.RECIEVED_SMS_CNT.setValue(0);
				Param.SUSPICIOUS_SMS_CNT.setValue(0);
				Toast.makeText(SettingsActivity.this, R.string.statistic_reseted, Toast.LENGTH_SHORT).show();
			}
			@Override
			public void cancel(){}
		});
	}

	public void onSave(View v){
		//set use_sync
		boolean useSync=syncCheckBox.isChecked();
		Param.USE_SYNC.setValue(useSync);
		//set use top 100 filters
		boolean useTop100=top100CheckBox.isChecked();
		Param.USE_TOP_100.setValue(useTop100);
		//set language
		String lang=langSpinner.getSelectedItem().toString().substring(1, 3);
		LocaleUtils.setLanguage(this, lang, true);
		onCreate(null);
		MainActivity.invalidate();
	}
}
