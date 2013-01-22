package com.quazar.sms_firewall.network;

import java.io.InputStream;
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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ServerClient {
	public enum ServerMethods{get_vocabulary, get_tops, check_fraud, post_filters, post_vote, post_fraud};
	private static final String HOST="http://sms-firewall.appspot.com/";
	public static JSONObject get(ServerMethods method) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGetRequest = new HttpGet(HOST+method.toString());
			HttpResponse response = (HttpResponse) httpclient
					.execute(httpGetRequest);
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

	public static boolean post(ServerMethods method, JSONObject data) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(HOST+method.toString());
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("data", data.toString()));			
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));			
			HttpResponse response = httpclient.execute(httppost);
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

	public static void getAsync(final ServerMethods method, final Handler handler) {
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
	
	public static void postAsync(final ServerMethods method, final JSONObject data, final Handler handler) {
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

	private static String convertStreamToString(InputStream is) {
		try {
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			return new String(bytes, "UTF-8");
		} catch (Exception ex) {
			Log.e("network", ex.toString());
			return null;
		}
	}
}
