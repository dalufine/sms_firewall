package com.quazar.sms_firewall.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

	public static String getPhoneName() {
		return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
	}
	// need ACCESS_NETWORK_STATE permission
	public static boolean isOnline(Activity act) {
		ConnectivityManager cm = (ConnectivityManager) act
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
