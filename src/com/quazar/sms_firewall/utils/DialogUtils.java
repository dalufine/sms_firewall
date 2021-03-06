package com.quazar.sms_firewall.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.ResponseCodes;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.ConfirmDialog;
import com.quazar.sms_firewall.dialogs.EnterValueDialog;
import com.quazar.sms_firewall.dialogs.SelectListItemDialog;
import com.quazar.sms_firewall.dialogs.SelectSourceDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;
import com.quazar.sms_firewall.enums.SourceTypes;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;

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

	public static void showSourceSelectPopup(final Activity activity, List<Integer> exclude, final Handler handler) throws Exception{
		showSourceSelectPopup(activity, null, exclude, handler);
	}

	public static void showSmsSelectDialog(Activity activity, int titleStringId, final Handler handler) throws Exception{
		final List<HashMap<String, Object>> sms=ContentUtils.getInboxSms(activity);
		if(!sms.isEmpty()){
			SelectListItemDialog smsPopup=new SelectListItemDialog(activity, activity.getResources().getString(titleStringId), sms, new SelectListener<HashMap<String, Object>>(){
				@Override
				public void recieveSelection(HashMap<String, Object> selection){
					selection.put("type", SourceTypes.INBOX_SMS);
					if(handler!=null){
						Message mes=handler.obtainMessage(1, selection);
						handler.dispatchMessage(mes);
					}
				}
			});
			smsPopup.show();
		}else{
			DialogUtils.createWarningDialog(activity, activity.getResources().getString(R.string.warning), activity.getResources().getString(R.string.no_sms_warn), activity.getResources().getString(R.string.ok));
		}
	}

	public static void showCallSelectDialog(Activity activity, final Handler handler) throws Exception{
		final List<HashMap<String, Object>> sources=ContentUtils.getIncomeCalls(activity);
		if(!sources.isEmpty()){
			SelectListItemDialog callsPopup=new SelectListItemDialog(activity, activity.getResources().getString(R.string.income_calls), sources, new SelectListener<HashMap<String, Object>>(){
				@Override
				public void recieveSelection(HashMap<String, Object> selection){
					selection.put("type", SourceTypes.CALLS);
					if(handler!=null){
						Message mes=handler.obtainMessage(1, selection);
						handler.dispatchMessage(mes);
					}
				}
			});
			callsPopup.show();
		}else{
			DialogUtils.createWarningDialog(activity, activity.getResources().getString(R.string.warning), activity.getResources().getString(R.string.no_income_calls), activity.getResources().getString(R.string.ok));
		}
	}

	public static void showSuspiciousSelectDialog(Activity activity, final Handler handler) throws Exception{
		DataDao dao=new DataDao(activity);
		List<SmsLogItem> list=dao.getLogs(LogStatus.SUSPICIOUS, null, 0, 1);
		if(list.isEmpty()){
			DialogUtils.createWarningDialog(activity, activity.getResources().getString(R.string.warning), activity.getResources().getString(R.string.no_suspicious_sms), activity.getResources().getString(R.string.ok));
			return;
		}
		final List<HashMap<String, Object>> sources=new ArrayList<HashMap<String, Object>>();
		SimpleDateFormat sdf=new SimpleDateFormat(activity.getString(R.string.default_datetime_format), Locale.getDefault());
		for(SmsLogItem item:list){
			HashMap<String, Object> map=new HashMap<String, Object>();
			map.put(ContentUtils.NAME, item.getName()!=null?item.getName():item.getNumber());
			map.put(ContentUtils.NUMBER, item.getName()!=null&&!item.getName().equalsIgnoreCase(item.getNumber())?item.getNumber()+" ":"");
			map.put(ContentUtils.PROC_NUMBER, item.getNumber());
			map.put(ContentUtils.DATE, sdf.format(item.getDate()));
			map.put(ContentUtils.TEXT, item.getBody());
			sources.add(map);
		}
		SelectListItemDialog suspiciousPopup=new SelectListItemDialog(activity, activity.getResources().getString(R.string.suspicious_sms), sources, new SelectListener<HashMap<String, Object>>(){
			@Override
			public void recieveSelection(HashMap<String, Object> selection){
				selection.put("type", SourceTypes.SUSPICIOUS);
				if(handler!=null){
					Message mes=handler.obtainMessage(1, selection);
					handler.dispatchMessage(mes);
				}
			}
		});
		suspiciousPopup.show();
	}

	public static void showEnterValueDialog(Activity activity, int titleStringId, final int inputType, final Handler handler){
		EnterValueDialog evd=new EnterValueDialog(activity, activity.getResources().getString(titleStringId), inputType, new DialogListener<String>(){
			@Override
			public void ok(String value){
				if(handler!=null){
					Map<String, Object> data=new HashMap<String, Object>();
					data.put("type", inputType==InputType.TYPE_CLASS_PHONE?SourceTypes.ENTERED_NUMBER:SourceTypes.ENTERED_WORD);
					data.put(ContentUtils.PROC_NUMBER, value);
					Message mes=handler.obtainMessage(1, data);
					handler.dispatchMessage(mes);
				}
			}
			@Override
			public void cancel(){}

		});
		evd.show();
	}

	public static void showSourceSelectPopup(final Activity activity, String title, List<Integer> exclude, final Handler handler) throws Exception{
		SelectSourceDialog sourceSelect=new SelectSourceDialog(activity, title, new SelectListener<Integer>(){
			@Override
			public void recieveSelection(Integer selection){
				final DataDao dao=new DataDao(activity);
				try{
					switch(selection){
						case SelectSourceDialog.FROM_CONTACTS:
							StateManager.getContact(activity);
							break;
						case SelectSourceDialog.FROM_INBOX_SMS:
							showSmsSelectDialog(activity, R.string.inbox_sms, handler);
							break;
						case SelectSourceDialog.FROM_INCOME_CALLS:
							showCallSelectDialog(activity, handler);
							break;
						case SelectSourceDialog.FROM_SUSPICIOUS_SMS:
							showSuspiciousSelectDialog(activity, handler);
							break;
						case SelectSourceDialog.FROM_FRAUDS_TOP:
							break;
						case SelectSourceDialog.FROM_ENTER_PHONE:
							showEnterValueDialog(activity, R.string.enter_phone_number, InputType.TYPE_CLASS_PHONE, handler);
							break;
						case SelectSourceDialog.FROM_ENTER_WORD:
							showEnterValueDialog(activity, R.string.enter_word, InputType.TYPE_CLASS_TEXT, handler);
							break;
						default:
							break;
					}
				}catch(Exception ex){
					LogUtil.error(activity, "showSourceSelectPopup", ex);
				}finally{
					if(dao!=null){
						dao.close();
					}
				}
			}
		}, exclude);
		sourceSelect.show();
	}

	public static void showConfirmDialog(Context context, String title, String question, final DialogListener<Boolean> listener){
		new ConfirmDialog(context, title, question, listener).show();
	}

	public static void showErrorDialog(Context context, int errorCode){
		showInfoDialog(context, context.getString(R.string.error_dialog_title), ResponseCodes.getErrorByCode(errorCode).getDescription());
	}

	public static void showInfoDialog(Context context, String title, String info){
		new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_info).setTitle(title).setMessage(info).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		}).show();
	}
}
