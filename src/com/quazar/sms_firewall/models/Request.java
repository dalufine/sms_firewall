package com.quazar.sms_firewall.models;

import org.json.JSONObject;

import com.quazar.sms_firewall.network.ApiClient.ApiMethods;

public class Request {
	private ApiMethods method;
	private JSONObject data;
	public Request(ApiMethods method, JSONObject data) {
		super();
		this.method = method;
		this.data = data;
	}
	public ApiMethods getMethod() {
		return method;
	}
	public JSONObject getData() {
		return data;
	}	
}
