package com.quazar.sms_firewall.utils;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.CallsSelectDialog;
import com.quazar.sms_firewall.dialogs.EnterValueDialog;
import com.quazar.sms_firewall.dialogs.SelectSourceDialog;
import com.quazar.sms_firewall.dialogs.SmsSelectDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;
import com.quazar.sms_firewall.models.Filter.FilterType;

public class DialogUtils {
	public static void createWarningDialog(final AlertDialog dialog,
			String title, String message, String buttonTitle) {
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setCancelable(true);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, buttonTitle,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	}

	public static void showSourceSelectPopup(final Activity activity, final Handler handler){		
		SelectSourceDialog sourceSelect=new SelectSourceDialog(activity, new SelectListener<Integer>(){
			@Override
			public void recieveSelection(Integer selection){
				final DataDao dao=new DataDao(activity);
				try{
					switch(selection){
						case SelectSourceDialog.FROM_CONTACTS:
							StateManager.getContact(activity);
							break;
						case SelectSourceDialog.FROM_INBOX_SMS:
							SmsSelectDialog smsPopup=new SmsSelectDialog(activity, new SelectListener<HashMap<String, Object>>(){
								@Override
								public void recieveSelection(HashMap<String, Object> selection){
									dao.insertFilter(FilterType.PHONE_NAME, (String)selection.get(ContentUtils.SMS_NUMBER));
									if(handler!=null)
										handler.dispatchMessage(new Message());
								}
							});
							smsPopup.show();
							break;
						case SelectSourceDialog.FROM_INCOME_CALLS:
							CallsSelectDialog callsPopup=new CallsSelectDialog(activity, new SelectListener<HashMap<String, Object>>(){
								@Override
								public void recieveSelection(HashMap<String, Object> selection){
									dao.insertFilter(FilterType.PHONE_NAME, (String)selection.get(ContentUtils.CALLS_NUMBER));
									if(handler!=null)
										handler.dispatchMessage(new Message());
								}
							});
							callsPopup.show();
							break;
						default:
							break;
					}
				}finally{
					dao.close();
				}
			}
		});
		sourceSelect.show();
	}

	public static void showEnterWordFilterPopup(final Context context,
			final Handler handler) {
		EnterValueDialog popup = new EnterValueDialog(context, context
				.getResources().getString(R.string.enter_word),
				new SelectListener<String>() {
					@Override
					public void recieveSelection(String selection) {
						DataDao dao = new DataDao(context);
						try{
							dao.insertFilter(FilterType.WORD, selection);
							if (handler != null)
								handler.dispatchMessage(new Message());
						}finally{
							dao.close();
						}
					}
				});
		popup.show();
	}
	public static void showConfirmDialog(Context context, String title, String question, final DialogListener<Boolean> listener){
		new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(question)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.ok(true);    
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.cancel();    
            }
        })
        .show();
	}
}
