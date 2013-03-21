package com.quazar.sms_firewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReciever extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		//this.abortBroadcast();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Object[] smsextras = (Object[]) extras.get("pdus");
			String str="";
			for (int i = 0; i < smsextras.length; i++) {
				SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);
				String strMsgBody = smsmsg.getMessageBody().toString();
				String strMsgSrc = smsmsg.getOriginatingAddress();
				str += "SMS from " + strMsgSrc + " : " + strMsgBody;
				Log.i("sms_recieved", str);
				Toast t=Toast.makeText(context, str, Toast.LENGTH_LONG);
				t.show();
			}
		}
	}
}
