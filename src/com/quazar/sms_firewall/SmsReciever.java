package com.quazar.sms_firewall;

import java.util.HashMap;
import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;

import com.quazar.sms_firewall.activities.MainActivity;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.UserFilter;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;
import com.quazar.sms_firewall.models.TopFilter;
import com.quazar.sms_firewall.models.TopFilter.TopCategory;
import com.quazar.sms_firewall.models.TopFilter.TopType;
import com.quazar.sms_firewall.utils.DictionaryUtils;

public class SmsReciever extends BroadcastReceiver{
	private DataDao dataDao=null;

	@Override
	public void onReceive(Context context, Intent intent){
		if(dataDao==null)
			dataDao=new DataDao(context);
		if(!Param.isLoaded())
			Param.load(context);
		Bundle extras=intent.getExtras();
		if(extras!=null){
			Object[] smsextras=(Object[])extras.get("pdus");
			List<UserFilter> filters=dataDao.getUserFilters();
			List<TopFilter> topFilters=dataDao.getAllTopFilters();
			HashMap<String, String> messages=new HashMap<String, String>();
			for(int i=0;i<smsextras.length;i++){
				SmsMessage smsmsg=SmsMessage.createFromPdu((byte[])smsextras[i]);
				String phoneName=smsmsg.getOriginatingAddress();
				if(!messages.containsKey(phoneName)){
					messages.put(phoneName, smsmsg.getMessageBody().toString());
				}else{
					messages.put(phoneName, messages.get(phoneName)+smsmsg.getMessageBody().toString());
				}
			}
			String fraudNumber="";
			for(String phoneName:messages.keySet()){
				String body=messages.get(phoneName);
				boolean blocked=false;
				for(UserFilter f:filters){
					if(!f.isValid(phoneName, body)){
						abortBroadcast();
						dataDao.insertLog(phoneName, body, LogStatus.BLOCKED);
						blocked=true;
						Param.BLOCKED_SMS_CNT.setValue((Integer)Param.BLOCKED_SMS_CNT.getValue()+1);
						break;
					}
				}
				if(!blocked){
					boolean fraudDetected=false;
					for(TopFilter ti:topFilters){
						if(ti.getType()==TopType.PHONE_NAME&&ti.getValue().equals(phoneName)||ti.getType()==TopType.WORD&&body.contains(ti.getValue())){
							dataDao.insertLog(phoneName, body, LogStatus.SUSPICIOUS);
							Param.SUSPICIOUS_SMS_CNT.setValue((Integer)Param.SUSPICIOUS_SMS_CNT.getValue()+1);
							if(ti.getCategory()==TopCategory.FRAUD){
								fraudDetected=true;
								fraudNumber=phoneName;
							}
							break;
						}
					}
					if(fraudDetected){
						NotificationCompat.Builder builder=new NotificationCompat.Builder(context);
						builder.setSmallIcon(R.drawable.ic_launcher);
						builder.setContentTitle(context.getResources().getString(R.string.warning));
						builder.setContentText(String.format(context.getResources().getString(R.string.fraud_warn), DictionaryUtils.getInstance().getContactsName(fraudNumber)));
						Intent resultIntent=new Intent(context, MainActivity.class);
						TaskStackBuilder stackBuilder=TaskStackBuilder.create(context);
						stackBuilder.addParentStack(MainActivity.class);
						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent=stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
						builder.setContentIntent(resultPendingIntent);
						NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
						nm.notify(777, builder.build());
					}
					dataDao.insertLog(phoneName, body, LogStatus.FILTERED);
					Param.RECIEVED_SMS_CNT.setValue((Integer)Param.RECIEVED_SMS_CNT.getValue()+1);
				}
			}
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(dataDao!=null){
			dataDao.close();
		}
	}
}
