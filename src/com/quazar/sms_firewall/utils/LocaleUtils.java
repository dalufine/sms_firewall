package com.quazar.sms_firewall.utils;

import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;

import com.quazar.sms_firewall.Param;

public class LocaleUtils{
	public static void setLanguage(Activity activity, String isoCode, boolean save){
		Locale locale=new Locale(isoCode);
		Locale.setDefault(locale);
		Configuration config=new Configuration();
		config.locale=locale;
		activity.getBaseContext().getResources().updateConfiguration(config, activity.getBaseContext().getResources().getDisplayMetrics());
		activity.getApplicationContext().getResources().updateConfiguration(config, activity.getBaseContext().getResources().getDisplayMetrics());
		activity.getResources().updateConfiguration(config, activity.getBaseContext().getResources().getDisplayMetrics());
		if(save){
			Param.LOCALE.setValue(isoCode);			
		}
	}
}
