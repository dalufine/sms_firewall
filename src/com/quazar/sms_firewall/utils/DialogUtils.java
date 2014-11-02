package com.quazar.sms_firewall.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.ResponseCodes;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.CallsSelectDialog;
import com.quazar.sms_firewall.dialogs.EnterValueDialog;
import com.quazar.sms_firewall.dialogs.SelectSourceDialog;
import com.quazar.sms_firewall.dialogs.SmsSelectDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;
import com.quazar.sms_firewall.models.UserFilter.FilterType;

public class DialogUtils{
	public static void createWarningDialog(final Context context, String title, String message, String buttonTitle){
		AlertDialog.Builder builder=new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(message).setCancelable(true).setPositiveButton(buttonTitle, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		}).show();
	}

	public static void showSourceSelectPopup(final Activity activity, List<Integer> exclude, final Handler handler){
		SelectSourceDialog sourceSelect=new SelectSourceDialog(activity, new SelectListener<Integer>(){
			@Override
			public void recieveSelection(Integer selection){
				final DataDao dao=new DataDao(activity);
				try{
					switch(selection){
						case SelectSourceDialog.FROM_CONTACTS:
							StateManager.getContact(activity);
							break;
						case SelectSourceDialog.FROM_INBOX_SMS:{
							final List<HashMap<String, Object>> sms=ContentUtils.getInboxSms(activity);
							if(!sms.isEmpty()){
								SmsSelectDialog smsPopup=new SmsSelectDialog(activity, new SelectListener<HashMap<String, Object>>(){
									@Override
									public void recieveSelection(HashMap<String, Object> selection){
										if(handler!=null){
											Message mes=handler.obtainMessage(1, selection);
											handler.dispatchMessage(mes);
										}
									}
								}, sms);
								smsPopup.show();
							}else{
								DialogUtils.createWarningDialog(activity, activity.getResources().getString(R.string.warning), activity.getResources().getString(R.string.no_sms_warn), activity.getResources().getString(
										R.string.ok));
							}
							break;
						}
						case SelectSourceDialog.FROM_INCOME_CALLS:
							final List<HashMap<String, Object>> sources=ContentUtils.getIncomeCalls(activity);
							if(!sources.isEmpty()){
								CallsSelectDialog callsPopup=new CallsSelectDialog(activity, new SelectListener<HashMap<String, Object>>(){
									@Override
									public void recieveSelection(HashMap<String, Object> selection){
										if(handler!=null){
											Message mes=handler.obtainMessage(1, selection);
											handler.dispatchMessage(mes);
										}
									}
								}, sources);
								callsPopup.show();
							}else{
								DialogUtils.createWarningDialog(activity, activity.getResources().getString(R.string.warning), activity.getResources().getString(R.string.no_income_calls), activity.getResources()
										.getString(R.string.ok));
							}
							break;
						case SelectSourceDialog.FROM_SUSPICIOUS_SMS:
						case SelectSourceDialog.FROM_FRAUDS_TOP:
							break;
						case SelectSourceDialog.FROM_ENTER_PHONE:{
							EnterValueDialog evd=new EnterValueDialog(activity, activity.getResources().getString(R.string.enter_phone_number), true, new SelectListener<String>(){
								@Override
								public void recieveSelection(String selection){
									if(handler!=null){
										Map<String, Object> data=new HashMap<String, Object>();
										data.put(ContentUtils.NUMBER, selection);
										Message mes=handler.obtainMessage(1, data);
										handler.dispatchMessage(mes);
									}
								}
							});
							evd.show();
							break;
						}
						case SelectSourceDialog.FROM_ENTER_WORD:{
							EnterValueDialog evd=new EnterValueDialog(activity, activity.getResources().getString(R.string.enter_word), false, new SelectListener<String>(){
								@Override
								public void recieveSelection(String selection){
									if(handler!=null){
										Map<String, Object> data=new HashMap<String, Object>();
										data.put(ContentUtils.NUMBER, selection);
										Message mes=handler.obtainMessage(1, data);
										handler.dispatchMessage(mes);
									}
								}
							});
							evd.show();
							break;
						}
						default:
							break;
					}
				}finally{
					dao.close();
				}
			}
		}, exclude);
		sourceSelect.show();
	}

	public static void showEnterWordFilterPopup(final Context context, final Handler handler){
		EnterValueDialog popup=new EnterValueDialog(context, context.getResources().getString(R.string.enter_word), false, new SelectListener<String>(){
			@Override
			public void recieveSelection(String selection){
				DataDao dao=new DataDao(context);
				try{
					dao.insertUserFilter(FilterType.WORD, selection);
					if(handler!=null)
						handler.dispatchMessage(new Message());
				}finally{
					dao.close();
				}
			}
		});
		popup.show();
	}

	public static void showConfirmDialog(Context context, String title, String question, final DialogListener<Boolean> listener){
		new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title).setMessage(question).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				listener.ok(true);
			}
		}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				listener.cancel();
			}
		}).show();
	}

	public static void showErrorDialog(Context context, int errorCode){
		showInfoDialog(context, context.getString(R.string.error_dialog_title), ResponseCodes.getErrorByCode(errorCode).getDescription());
	}

	public static void showInfoDialog(Context context, String title, String info){
		AlertDialog dialog=new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_info).setTitle(title).setMessage(info).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		}).show();
	}
}
