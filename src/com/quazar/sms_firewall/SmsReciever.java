package com.quazar.sms_firewall;

import java.util.HashMap;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;

public class SmsReciever extends BroadcastReceiver {
	private DataDao dataDao = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (dataDao == null)
			dataDao = new DataDao(context);
		if(!Param.isLoaded())
			Param.load(context);
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Object[] smsextras = (Object[]) extras.get("pdus");
			List<Filter> filters = dataDao.getFilters();
			HashMap<String, String> messages = new HashMap<String, String>();
			for (int i = 0; i < smsextras.length; i++) {
				SmsMessage smsmsg = SmsMessage
						.createFromPdu((byte[]) smsextras[i]);
				String phoneName = smsmsg.getOriginatingAddress();
				if (!messages.containsKey(phoneName)) {
					messages.put(phoneName, smsmsg.getMessageBody().toString());
				} else {
					messages.put(phoneName, messages.get(phoneName)+smsmsg.getMessageBody().toString());					
				}
			}
			for (String phoneName : messages.keySet()) {
				String body = messages.get(phoneName);
				boolean normal = true;
				for (Filter f : filters) {
					if (!f.isValid(phoneName, body)) {
						abortBroadcast();
						dataDao.insertLog(phoneName, body, LogStatus.BLOCKED);
						normal = false;
						Param.BLOCKED_SMS_CNT.setValue((Integer)Param.BLOCKED_SMS_CNT.getValue()+1);
						break;
					}
				}
				if (normal){
					dataDao.insertLog(phoneName, body, LogStatus.FILTERED);
					Param.RECIEVED_SMS_CNT.setValue((Integer)Param.RECIEVED_SMS_CNT.getValue()+1);
				}
			}
		}
	}
}
