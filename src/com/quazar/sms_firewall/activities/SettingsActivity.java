package com.quazar.sms_firewall.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.adapters.SpinnerAdapter;
import com.quazar.sms_firewall.adapters.SpinnerModel;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.EnterPasswordDialog;
import com.quazar.sms_firewall.dialogs.HelpDialog.Window;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.LocaleUtils;
import com.quazar.sms_firewall.utils.LogUtil;
import com.quazar.sms_firewall.utils.Utils;

public class SettingsActivity extends BaseActivity{

	private Spinner langSpinner;
	private CheckBox syncCheckBox;
	private CheckBox top100CheckBox;
	private DataDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.windowType = Window.SETTINGS;
		setContentView(R.layout.activity_settings);
		dao = new DataDao(this);
		langSpinner = ((Spinner) findViewById(R.id.language_select));
		//spinner init
		List<SpinnerModel> items = new ArrayList<SpinnerModel>();
		String[] languages = getResources().getStringArray(R.array.languages);
		SpinnerModel model = new SpinnerModel();
		model.setImageId(R.drawable.gb);
		model.setText(languages[0]);
		items.add(model);
		model = new SpinnerModel();
		model.setImageId(R.drawable.ru);
		model.setText(languages[1]);
		items.add(model);
		langSpinner.setAdapter(new SpinnerAdapter(this, items));
		//
		if (Param.LOCALE.getValue() != null && ((String) Param.LOCALE.getValue()).length() > 0) {
			langSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.lang_iso_codes)).indexOf(
					(String) Param.LOCALE.getValue()));
		}
		syncCheckBox = ((CheckBox) findViewById(R.id.sync_on));
		syncCheckBox.setChecked((Boolean) Param.USE_SYNC.getValue());
		top100CheckBox = ((CheckBox) findViewById(R.id.top_filter_on));
		top100CheckBox.setChecked((Boolean) Param.FRAUD_NOTIFICATION.getValue());
		//set version
		TextView copyright = (TextView) findViewById(R.id.copyright);
		copyright.setText(String.format(copyright.getText().toString(), Param.VERSION.getValue().toString()));
		//set last sync
		TextView lastSync = (TextView) findViewById(R.id.server_sync);
		String syncStr=String.format(lastSync.getText().toString(), SimpleDateFormat.getDateTimeInstance().format(new Date(
				(Long) Param.LAST_SYNC.getValue())));
		lastSync.setText(syncStr);
	}

	public void onClearLogs(View view){
		DialogUtils.showConfirmDialog(this, getString(R.string.confirmation), getString(R.string.all_logs_clear_conf),
				new DialogListener<Boolean>(){

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
		EnterPasswordDialog dialog =
				new EnterPasswordDialog(this, R.string.enter_new_password, null, new DialogListener<String>(){

					@Override
					public void ok(String value){
						try {
							Param.PASSWORD.setValue(Utils.md5AsBase64String(value));
						}
						catch(Exception ex) {
							LogUtil.error(SettingsActivity.this, "password hash error", ex);
						}
						Toast.makeText(SettingsActivity.this, R.string.password_saved, Toast.LENGTH_SHORT).show();
					}

					@Override
					public void cancel(){}
				});
		dialog.show();
	}

	public void onResetStatistics(View view){
		DialogUtils.showConfirmDialog(this, getString(R.string.confirmation),
				getString(R.string.reset_statistics_conf), new DialogListener<Boolean>(){

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
		boolean useSync = syncCheckBox.isChecked();
		Param.USE_SYNC.setValue(useSync);
		//fraud notification
		boolean fraudNotification = top100CheckBox.isChecked();
		Param.FRAUD_NOTIFICATION.setValue(fraudNotification);
		//set language				
		LocaleUtils.setLanguage(this, getLangIsoCode(langSpinner.getSelectedItem().toString()), true);
		onCreate(null);
		MainActivity.invalidate();
	}

	private String getLangIsoCode(String lang){
		int index = Arrays.binarySearch(getResources().getStringArray(R.array.languages), lang);
		return getResources().getStringArray(R.array.lang_iso_codes)[index];
	}
}
