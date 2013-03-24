package com.quazar.sms_firewall;

import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.LogItem.LogStatus;

public class SmsReciever extends BroadcastReceiver{
	private DataDao dataDao=null;
	@Override
	public void onReceive(Context context, Intent intent){
		if(dataDao==null)
			dataDao=new DataDao(context);
		Bundle extras=intent.getExtras();
		if(extras!=null){
			Object[] smsextras=(Object[])extras.get("pdus");			
			List<Filter> filters=dataDao.getFilters();
			for(int i=0; i<smsextras.length; i++){
				SmsMessage smsmsg=SmsMessage.createFromPdu((byte[])smsextras[i]);
				String body=smsmsg.getMessageBody().toString();
				String phoneName=smsmsg.getOriginatingAddress();				
				for(Filter f:filters){
					if(!f.isValid(phoneName, body)) {
						abortBroadcast();
						dataDao.insertLog(phoneName, body, LogStatus.BLOCKED);
						return;
					}
				}				
				dataDao.insertLog(phoneName, body, LogStatus.FILTERED);				
			}
		}
	}
}
