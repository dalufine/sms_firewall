package com.quazar.sms_firewall.models;

import org.json.JSONObject;

public class Request {
	private long id;
	private String method;
	private JSONObject data;
	public Request(long id, String method, JSONObject data) {
		super();
		this.id=id;
		this.method = method;
		this.data = data;
	}
	public String getMethod() {
		return method;
	}
	public JSONObject getData() {
		return data;
	}
	public long getId(){
		return id;
	}		
}
