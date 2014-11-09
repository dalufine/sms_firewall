package com.quazar.sms_firewall.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class DeviceInfoUtil{
	public static String getIMEI(Context context){
		TelephonyManager telephonyManager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	public static String getMyPhoneNumber(Context context){
		TelephonyManager mTelephonyMgr=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}
	public static String getPhoneName(){
		return android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;
	}
	public static boolean isOnline(Context context){
		ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo=cm.getActiveNetworkInfo();
		if(netInfo!=null&&netInfo.isAvailable()&&netInfo.isConnected()&&(netInfo.getType()==ConnectivityManager.TYPE_WIFI||netInfo.getType()==ConnectivityManager.TYPE_MOBILE&&!netInfo.isRoaming())){
			return true;
		}
		return false;
	}
}
