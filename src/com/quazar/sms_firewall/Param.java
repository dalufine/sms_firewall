package com.quazar.sms_firewall;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

public enum Param{
	IS_NEW(true), BLOCKED_SMS_CNT(0), RECIEVED_SMS_CNT(0), SUSPICIOUS_SMS_CNT(0), LAST_SYNC(0L), PASSWORD(null),
	USE_SYNC(false), USER_EMAIL(null), SEND_SUSPICIOUS(true), VERSION("1.0.0"), LOCALE(null), DEFAULT_LOCALE(Locale.getDefault()), FRAUD_NOTIFICATION(true);

	private Object value;
	private static SharedPreferences store;
	private static boolean loaded;
	private static List<Param> constants = Arrays.asList(VERSION, DEFAULT_LOCALE);

	Param(Object value){
		this.value = value;
	}

	public static void load(Context context){
		store = context.getSharedPreferences("sms_firewall_params", 0);
		for (Param p:Param.values()) {
			if (!constants.contains(p)) {
				if (p.value instanceof Integer) {
					p.value = store.getInt(p.name(), 0);
				} else if (p.value instanceof Long) {
					p.value = store.getLong(p.name(), 0);
				} else if (p.value instanceof Boolean) {
					p.value = store.getBoolean(p.name(), true);
				} else if (p.value instanceof String) {
					p.value = store.getString(p.name(), "");
				} else if (p.value instanceof Float) {
					p.value = store.getFloat(p.name(), 0);
				} else {
					p.value = store.getString(p.name(), "");
				}
			}
		}
		loaded = true;
	}

	public Object getValue(){
		return value;
	}

	public static void saveDefaults(Context context){
		store = context.getSharedPreferences("sms_firewall_params", 0);
		for (Param p:values()) {
			if (p.getValue() != null) {
				p.setValue(p.getValue());
			}
		}
	}

	public void setValue(Object value){
		this.value = value;
		SharedPreferences.Editor editor = store.edit();
		if (value instanceof Integer)
			value = editor.putInt(name(), (Integer) value);
		else if (value instanceof Long)
			value = editor.putLong(name(), (Long) value);
		else if (value instanceof Boolean)
			value = editor.putBoolean(name(), (Boolean) value);
		else if (value instanceof String)
			value = editor.putString(name(), (String) value);
		else if (value instanceof Float)
			value = editor.putFloat(name(), (Float) value);
		else value = editor.putString(name(), value.toString());
		editor.commit();
	}

	public static void reset(){
		SharedPreferences.Editor editor = store.edit();
		editor.clear();
		editor.commit();
	}

	public static boolean isLoaded(){
		return loaded;
	}
}
