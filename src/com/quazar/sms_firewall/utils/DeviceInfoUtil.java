package com.quazar.sms_firewall.utils;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

public class DeviceInfoUtil {
	// need READ_PHONE_STATE permission
	public static String getIMEI(Activity act) {
		TelephonyManager telephonyManager = (TelephonyManager) act
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	public static String getMyPhoneNumber(Activity act) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) act
				.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}
}
