package com.quazar.sms_firewall.activities;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.ResponseCodes;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.HelpDialog;
import com.quazar.sms_firewall.dialogs.RegistrationDialog;
import com.quazar.sms_firewall.dialogs.SelectSourceDialog;
import com.quazar.sms_firewall.models.UserFilter.FilterType;
import com.quazar.sms_firewall.network.ApiService;
import com.quazar.sms_firewall.utils.ContentUtils;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.DictionaryUtils;
import com.quazar.sms_firewall.utils.LocaleUtils;
import com.quazar.sms_firewall.utils.LogUtil;

public class MainActivity extends Activity{
	private DataDao dataDao;
	private static volatile boolean invalidated=false;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if((Boolean)Param.IS_NEW.getValue()){
			Param.saveDefaults(this);
		}
		Param.load(this);
		if(Param.LOCALE.getValue()!=null&&((String)Param.LOCALE.getValue()).length()>0){
			LocaleUtils.setLanguage(this, (String)Param.LOCALE.getValue(), false);
		}
		dataDao=new DataDao(this);
		ResponseCodes.init(this);		
		DictionaryUtils.createInstance(this);		
		setContentView(R.layout.activity_main);
		updateStatisticsViews();
		NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(777);
		if((Boolean)Param.IS_NEW.getValue()){
			Param.LOCALE.setValue(Locale.getDefault().getCountry());
			RegistrationDialog rd=new RegistrationDialog(this);
			rd.setOnCancelListener(new DialogInterface.OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog){
					finish();
				}
			});
			rd.show();
		}else if((Boolean)Param.USE_SYNC.getValue()&&(System.currentTimeMillis()-(Long)Param.LAST_SYNC.getValue())/86400000L>2){
			ApiService api=new ApiService(this);
			try{
				api.sync();
			}catch(Exception ex){
				LogUtil.error(this, "onCreate", ex);
			}
			//Param.LAST_SYNC.setValue(System.currentTimeMillis());//TODO uncomment on prod
		}
	}
	@Override
	protected void onDestroy(){
		if(dataDao!=null){
			dataDao.close();
		}
		super.onDestroy();
	}
	public static void invalidate(){
		invalidated=true;
	}
	@Override
	protected void onResume(){
		super.onResume();
		if(invalidated){
			setContentView(R.layout.activity_main);
			invalidated=false;
		}
		updateStatisticsViews();
	}
	private void updateStatisticsViews(){
		TextView tv=(TextView)findViewById(R.id.stat_blocked);
		tv.setText(String.format(getResources().getString(R.string.stat_blocked), (Integer)Param.BLOCKED_SMS_CNT.getValue()));
		tv=(TextView)findViewById(R.id.stat_received);
		tv.setText(String.format(getResources().getString(R.string.stat_recieved), (Integer)Param.RECIEVED_SMS_CNT.getValue()));
		tv=(TextView)findViewById(R.id.stat_suspicious);
		tv.setText(String.format(getResources().getString(R.string.stat_suspicious), (Integer)Param.SUSPICIOUS_SMS_CNT.getValue()));
	}

	public void onHelpClick(View v){
		HelpDialog dialog=new HelpDialog(this, HelpDialog.Window.MAIN);
		dialog.show();
	}

	public void onPhoneNumberClick(View v){
		try{
			DialogUtils.showSourceSelectPopup(this, Arrays.asList(SelectSourceDialog.FROM_ENTER_WORD), new Handler(){
				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				public void handleMessage(Message msg){
					Map<String, Object> map=(Map)msg.obj;
					Object value=map.get(ContentUtils.PROC_NUMBER);
					if(value!=null){
						dataDao.insertUserFilter(FilterType.PHONE_NAME, (String)value);
					}
				}
			});
		}catch(Exception ex){
			LogUtil.error(this, "onPhoneNumberClick", ex);
		}
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
		DialogUtils.showEnterValueDialog(this, R.string.enter_word, InputType.TYPE_CLASS_TEXT, new Handler(new Handler.Callback(){
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public boolean handleMessage(Message msg){
				Map<String, Object> map=(Map)msg.obj;
				dataDao.insertUserFilter(FilterType.WORD, (String)map.get(ContentUtils.PROC_NUMBER));
				return false;
			}
		}));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==StateManager.CONTACTS_REQUEST_ID&&resultCode==Activity.RESULT_OK){
			String phoneNumber=ContentUtils.getPhoneNumber(this, data.getData());
			try{
				dataDao.insertUserFilter(FilterType.PHONE_NAME, ContentUtils.getFormatedPhoneNumber(this, phoneNumber));
			}catch(Exception ex){
				LogUtil.error(this, "onActivityResult", ex);
			}
		}
	}
}
