package com.quazar.sms_firewall.network;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.quazar.sms_firewall.utils.DeviceInfoUtil;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

public class ServerClient {
	public enum ServerMethods{get_vocabulary, get_tops, check_fraud, post_filters, post_vote, post_fraud};
	private static final String HOST="http://sms-firewall.appspot.com/";
	private String imei, phoneName, phoneNumber;
	public ServerClient(Activity act){
		this.imei=DeviceInfoUtil.getIMEI(act);
		this.phoneName=DeviceInfoUtil.getPhoneName();
		this.phoneNumber=DeviceInfoUtil.getMyPhoneNumber(act);
	}
	public JSONObject get(ServerMethods method) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			String url=HOST+method.toString();
			addParam(url, "i", this.imei);
			addParam(url, "pna", this.phoneName);
			addParam(url, "pnu", this.phoneNumber);
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = (HttpResponse) httpclient
					.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String str = convertStreamToString(instream);
				instream.close();
				return new JSONObject(str);
			}
		} catch (Exception e) {
			Log.e("network", e.toString());
		}
		return null;
	}

	public boolean post(ServerMethods method, JSONObject data) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(HOST+method.toString());
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);			
			nameValuePairs.add(new BasicNameValuePair("data", encodeString(data.toString())));			
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));			
			HttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String str = convertStreamToString(instream);
				instream.close();
				JSONObject obj=new JSONObject(str);
				return obj.getInt("success")==1;
			}
		} catch (Exception e) {
			Log.e("network", e.toString());
		}
		return false;
	}

	public void getAsync(final ServerMethods method, final Handler handler) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				JSONObject result = get(method);
				Message message = handler.obtainMessage(1, result);
				handler.sendMessage(message);
			}
		};
		thread.start();
	}
	
	public void postAsync(final ServerMethods method, final JSONObject data, final Handler handler) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				boolean result = post(method, data);
				Message message = handler.obtainMessage(1, result);
				handler.sendMessage(message);
			}
		};
		thread.start();
	}

	private String convertStreamToString(InputStream is) {
		try {
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			return new String(bytes, "UTF-8");
		} catch (Exception ex) {
			Log.e("network", ex.toString());
			return null;
		}
	}
	private void addParam(String url, String name, String value){
		if(!url.contains("?"))			
			url=String.format("%s?%s=$s", url, name, encodeString(value));
		else url=String.format("%s&%s=$s", url, name, encodeString(value));
	}
	private String encodeString(String source){
		try{
			byte[] bytes=Base64.encode(source.getBytes("UTF-8"), Base64.DEFAULT);
			return URLEncoder.encode(new String(bytes, "UTF-8"), "UTF-8");
		}catch(Exception ex){
			Log.e("param encode", ex.toString());
		}
		return null;
	}
}
