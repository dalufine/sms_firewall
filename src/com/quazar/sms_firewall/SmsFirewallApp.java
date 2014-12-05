package com.quazar.sms_firewall;

import android.app.Application;
import android.content.Intent;

public class SmsFirewallApp extends Application{
	public void onCreate(){
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread thread, Throwable e){
				handleUncaughtException(thread, e);
			}
		});
	}

	public void handleUncaughtException(Thread thread, Throwable e){
		e.printStackTrace();
		Intent intent = new Intent();
		intent.setAction("com.quazar.sms_firewall.SEND_REPORT");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);		
		System.exit(1);
	}	
}
