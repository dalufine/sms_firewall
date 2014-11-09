package com.quazar.sms_firewall.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

public class JSONClient{
	private static final String HOST="malyanov.ddns.net:8080";
	private static final String PROTOCOL="http";

	public void ping(final int timeout, final Handler handler){
		Thread thread=new Thread(){
			@Override
			public void run(){
				long start=System.currentTimeMillis();
				while(true&&System.currentTimeMillis()-start<timeout){
					try{
						HttpURLConnection connection=(HttpURLConnection)new URL(PROTOCOL+"://"+HOST).openConnection();
						connection.setConnectTimeout(timeout);
						connection.setReadTimeout(timeout);
						connection.setRequestMethod("HEAD");
						connection.getResponseCode();
						Message message=handler.obtainMessage(1, true);
						handler.dispatchMessage(message);
						return;
					}catch(Exception e){
						Log.i("ping", "ping error");
						try{
							Thread.sleep(1000);
						}catch(Exception ex){
						}
					}
				}
				Message message=handler.obtainMessage(1, false);
				handler.dispatchMessage(message);
			}
		};
		thread.start();
	}

	protected JSONObject get(String serviceUrl){
		try{
			DefaultHttpClient httpclient=new DefaultHttpClient();
			String url=PROTOCOL+"://"+HOST+serviceUrl;
			HttpGet httpGet=new HttpGet(url);
			HttpResponse response=(HttpResponse)httpclient.execute(httpGet);
			HttpEntity entity=response.getEntity();
			if(entity!=null){
				InputStream instream=entity.getContent();
				String str=convertStreamToString(instream);
				instream.close();
				return new JSONObject(str);
			}
		}catch(Exception e){
			Log.e("network", e.toString());
		}
		return null;
	}

	protected JSONObject post(String serviceUrl, JSONObject data){
		try{
			HttpClient httpclient=new DefaultHttpClient();
			String url=PROTOCOL+"://"+HOST+serviceUrl;
			HttpPost httpPost=new HttpPost(url);
			httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
			httpPost.setHeader("Content-type", "application/json;charset=UTF-8");
			HttpResponse response=httpclient.execute(httpPost);
			HttpEntity entity=response.getEntity();
			if(entity!=null){
				InputStream instream=entity.getContent();
				String str=convertStreamToString(instream);
				instream.close();
				return new JSONObject(str);
			}
		}catch(Exception e){
			Log.e("network", e.toString());
		}
		return null;
	}

	// =================Async calls========================
	protected void getAsync(final String serviceUrl, final Handler handler){
		Thread thread=new Thread(){
			@Override
			public void run(){
				JSONObject result=get(serviceUrl);
				if(handler!=null&&result!=null){
					Message message=handler.obtainMessage(1, result);
					handler.sendMessage(message);
				}
			}
		};
		thread.start();
	}

	protected void postAsync(final String serviceUrl, final JSONObject data, final Handler handler){
		Thread thread=new Thread(){
			@Override
			public void run(){
				JSONObject result=post(serviceUrl, data);
				if(handler!=null&&result!=null){
					Message message=handler.obtainMessage(1, result);
					handler.sendMessage(message);
				}
			}
		};
		thread.start();
	}

	// ------------------------Help functions----------------------
	public String convertStreamToString(InputStream inputStream){
		if(inputStream!=null){
			StringBuilder sb=new StringBuilder();
			String line;
			try{
				BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				while((line=reader.readLine())!=null){
					sb.append(line).append("\n");
				}
			}catch(Exception ex){
				Log.e("network", ex.toString());
			}finally{
				try{
					inputStream.close();
				}catch(Exception ex){}
			}
			return sb.toString();
		}else{
			return "";
		}
	}

	@SuppressWarnings("unused")
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
