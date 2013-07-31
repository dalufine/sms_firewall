package com.quazar.sms_firewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class NetworkStatusReciever extends BroadcastReceiver {
	private DataDao dataDao = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (dataDao == null)
			dataDao = new DataDao(context);
		if(!Param.isLoaded())
			Param.load(context);
		if(DeviceInfoUtil.isOnline(context)){
			Log.i("network", "network is on");
		}else{
			Log.i("network", "network is off");
		}
	}
}
