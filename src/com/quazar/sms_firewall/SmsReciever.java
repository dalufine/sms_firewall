package com.quazar.sms_firewall;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;
import com.quazar.sms_firewall.models.TopFilter;
import com.quazar.sms_firewall.models.TopFilter.TopCategory;
import com.quazar.sms_firewall.models.TopFilter.TopType;
import com.quazar.sms_firewall.models.UserFilter;
import com.quazar.sms_firewall.utils.DictionaryUtils;
import com.quazar.sms_firewall.utils.NotificationUtil;

public class SmsReciever extends BroadcastReceiver {

	private DataDao dataDao = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (dataDao == null)
			dataDao = new DataDao(context);
		if (!Param.isLoaded())
			Param.load(context);
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Object[] smsextras = (Object[]) extras.get("pdus");
			List<UserFilter> filters = dataDao.getUserFilters();
			List<TopFilter> topFilters = dataDao.getAllTopFilters();
			Map<String, String> messages = new HashMap<String, String>();
			for (int i = 0; i < smsextras.length; i++) {
				SmsMessage smsmsg = SmsMessage
						.createFromPdu((byte[]) smsextras[i]);
				String phoneName = smsmsg.getOriginatingAddress();
				if (!messages.containsKey(phoneName)) {
					messages.put(phoneName, smsmsg.getMessageBody().toString());
				} else {
					messages.put(phoneName, messages.get(phoneName)
							+ smsmsg.getMessageBody().toString());
				}
			}
			String fraudNumber = "";
			for (String phoneName : messages.keySet()) {
				String body = messages.get(phoneName);
				boolean blocked = false;
				for (UserFilter f : filters) {
					if (!f.isValid(phoneName, body)) {
						abortBroadcast();
						dataDao.insertLog(phoneName, body, LogStatus.BLOCKED);
						blocked = true;
						Param.BLOCKED_SMS_CNT
								.setValue((Integer) Param.BLOCKED_SMS_CNT
										.getValue() + 1);
						break;
					}
				}
				if (!blocked) {
					if ((Boolean) Param.FRAUD_NOTIFICATION.getValue()) {
						boolean fraudDetected = false;
						for (TopFilter ti : topFilters) {
							if (ti.getType() == TopType.PHONE_NAME
									&& ti.getValue().equals(phoneName)
									|| ti.getType() == TopType.WORD
									&& body.toLowerCase(Locale.getDefault())
											.contains(
													ti.getValue()
															.toLowerCase(
																	Locale.getDefault()))) {
								dataDao.insertLog(phoneName, body,
										LogStatus.SUSPICIOUS);
								Param.SUSPICIOUS_SMS_CNT
										.setValue((Integer) Param.SUSPICIOUS_SMS_CNT
												.getValue() + 1);
								if (ti.getCategory() == TopCategory.FRAUD) {
									fraudDetected = true;
									fraudNumber = phoneName;
								}
								break;
							}
						}
						if (fraudDetected) {
							String text = String.format(context.getResources()
									.getString(R.string.fraud_warn),
									DictionaryUtils.getInstance()
											.getContactsName(fraudNumber));
							NotificationUtil.warning(context,
									R.drawable.warning, R.string.warning, text);
						}
					}
					dataDao.insertLog(phoneName, body, LogStatus.FILTERED);
					Param.RECIEVED_SMS_CNT
							.setValue((Integer) Param.RECIEVED_SMS_CNT
									.getValue() + 1);
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (dataDao != null) {
			dataDao.close();
		}
	}
}
