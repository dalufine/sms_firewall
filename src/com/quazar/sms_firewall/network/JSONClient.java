package com.quazar.sms_firewall.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;
import com.quazar.sms_firewall.utils.LogUtil;
import com.quazar.sms_firewall.utils.Utils;

public class JSONClient{

	protected static final String HOST = "malyanov.ddns.net:8080";
	//private static final String HOST="10.7.4.83:9999";
	protected static final String PROTOCOL = "http";
	protected static Executor executor = Executors.newFixedThreadPool(3);
	protected Context context;

	public JSONClient(Context context){
		this.context = context;
	}

	public void ping(final int timeout, final Handler handler){
		executor.execute(new Runnable(){

			@Override
			public void run(){
				long start = System.currentTimeMillis();
				while(true && System.currentTimeMillis() - start < timeout) {
					try {
						HttpURLConnection connection =
								(HttpURLConnection) new URL(PROTOCOL + "://" + HOST).openConnection();
						connection.setConnectTimeout(timeout);
						connection.setReadTimeout(timeout);
						connection.setRequestMethod("HEAD");
						connection.getResponseCode();
						Message message = handler.obtainMessage(1, true);
						handler.dispatchMessage(message);
						return;
					}
					catch(Exception e) {
						Log.i("ping", "ping error");
						try {
							Thread.sleep(1000);
						}
						catch(Exception ex) {}
					}
				}
				Message message = handler.obtainMessage(1, false);
				handler.dispatchMessage(message);
			}
		});
	}

	protected JSONObject get(String serviceUrl){
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			String url = PROTOCOL + "://" + HOST + serviceUrl;
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Hash", getHash(serviceUrl));
			httpGet.setHeader("UserID", getUserId());
			HttpResponse response = (HttpResponse) httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String str = convertStreamToString(instream);
				instream.close();
				return new JSONObject(str);
			}
		}
		catch(Exception ex) {
			Log.e("network", LogUtil.toString(ex));
		}
		return null;
	}
	
	protected String getUserId(){
		String id = null;
		Object email = Param.USER_EMAIL.getValue();
		if (email != null && ((String) email).trim().length() != 0)
			id = (String) email;
		else id = DeviceInfoUtil.getIMEI(context);
		return id;
	}

	protected JSONObject post(String serviceUrl, JSONObject data){
		try {
			HttpClient httpclient = new DefaultHttpClient();
			String url = PROTOCOL + "://" + HOST + serviceUrl;
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Hash", getHash(data.toString()));
			httpPost.setHeader("UserID", getUserId());
			httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
			httpPost.setHeader("Content-type", "application/json;charset=UTF-8");
			HttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String str = convertStreamToString(instream);
				instream.close();
				return new JSONObject(str);
			}
		}
		catch(Exception ex) {
			Log.e("network", LogUtil.toString(ex));
		}
		return null;
	}

	private String getHash(String str) throws Exception{
		StringBuilder sb = new StringBuilder(str.toLowerCase(Locale.getDefault()).replaceAll("\\s", ""));
		sb.append(Param.PASSWORD.getValue());
		return Utils.md5AsBase64String(sb.toString());
	}

	// =================Async calls========================
	protected void getAsync(final String serviceUrl, final Handler handler){
		executor.execute(new Runnable(){

			@Override
			public void run(){
				JSONObject result = get(serviceUrl);
				if (handler != null && result != null) {
					Message message = handler.obtainMessage(1, result);
					handler.sendMessage(message);
				}
			}
		});
	}

	protected void postAsync(final String serviceUrl, final JSONObject data, final Handler handler){
		executor.execute(new Runnable(){

			@Override
			public void run(){
				JSONObject result = post(serviceUrl, data);
				if (handler != null && result != null) {
					Message message = handler.obtainMessage(1, result);
					handler.sendMessage(message);
				}
			}
		});
	}

	// ------------------------Help functions----------------------
	public String convertStreamToString(InputStream inputStream) throws Exception{
		if (inputStream != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				while((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			}
			finally {
				try {
					inputStream.close();
				}
				catch(Exception ex) {}
			}
			return sb.toString();
		}
		return "";
	}
}
