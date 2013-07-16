package com.quazar.sms_firewall.models;

import org.json.JSONObject;

import android.util.Log;

public class Filter {
	public enum FilterType {
		PHONE_NAME, WORD, REGEXP
	};

	private int id;
	private String value;
	private FilterType type;

	public Filter(int id, String value, int type) {
		super();
		this.id = id;
		this.value = value;
		this.type=FilterType.values()[type];		
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public FilterType getType() {
		return type;
	}
	public boolean isValid(String phoneName, String body) {
		if(type==FilterType.PHONE_NAME) {
			return !phoneName.equals(value);
		}
		if(type==FilterType.WORD) {
			return !body.contains(value);
		}
		return false;
	}
	public JSONObject toJSON(){
		JSONObject obj=new JSONObject();
		try{
			obj.put("type", type.ordinal());
			obj.put("value", value);
		}catch(Exception ex){
			Log.e("filter", ex.toString());
		}
		return obj;
	}
}
