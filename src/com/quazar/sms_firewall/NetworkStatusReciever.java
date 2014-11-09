package com.quazar.sms_firewall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.network.ApiService;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class NetworkStatusReciever extends BroadcastReceiver{
	private DataDao dataDao;
	private ApiService api;

	@Override
	public void onReceive(Context context, Intent intent){
		if(dataDao==null)
			dataDao=new DataDao(context);
		if(api==null)
			api=new ApiService(context);
		if(!Param.isLoaded())
			Param.load(context);
		if(DeviceInfoUtil.isOnline(context)){
			Log.i("network", "network is on");
			try{
				api.sendWaitingRequests();
			}catch(Exception ex){
				Log.e("waiting request", ex.toString());
				ex.printStackTrace();
			}
		}else{
			Log.i("network", "network is off");
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(dataDao!=null){
			dataDao.close();
		}
	}
}
