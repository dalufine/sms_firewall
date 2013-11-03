package com.quazar.sms_firewall.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.ConnectionDialog;
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.models.Request;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.TopItem;
import com.quazar.sms_firewall.models.TopItem.TopCategory;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class ApiClient{
	private static final String HOST="http://xn--80ae0achg0hya.xn--p1ai/sms_firewall/services/api/";

	//private static final String HOST = "http://192.168.0.47:8080/sms_firewall/services/api/";

	public static enum ApiMethods{
		register, addFilter, syncUserFilters, check, getTops, addVote, saveSms, loadSms, registerBug, pack, getUserFilters
	};

	private Context context;

	public ApiClient(Context context){
		this.context=context;
	}

	private JSONObject get(ApiMethods method){
		try{
			DefaultHttpClient httpclient=new DefaultHttpClient();
			String url=HOST+method.toString();
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

	private JSONObject post(ApiMethods method, JSONObject data){
		try{
			HttpClient httpclient=new DefaultHttpClient();
			HttpPost httpPost=new HttpPost(HOST+method.toString());
			httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
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
	@SuppressWarnings("unused")
	private void getAsync(final ApiMethods method, final Handler handler){
		Thread thread=new Thread(){
			@Override
			public void run(){
				JSONObject result=get(method);
				if(handler!=null&&result!=null){
					Message message=handler.obtainMessage(1, result);
					handler.sendMessage(message);
				}
			}
		};
		thread.start();
	}

	private void postAsync(final ApiMethods method, final JSONObject data, final Handler handler){
		Thread thread=new Thread(){
			@Override
			public void run(){
				JSONObject result=post(method, data);
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

	// ---------------Api implementation-----------------
	public int register(String email, String password, Handler handler){
		String imei=DeviceInfoUtil.getIMEI(context), phoneName=DeviceInfoUtil.getPhoneName(), phoneNumber=DeviceInfoUtil.getMyPhoneNumber(context);
		JSONObject data=new JSONObject();
		try{
			if(email!=null&&!email.isEmpty()){
				data.put("id", email);
				data.put("password", password);
			}else{
				data.put("id", imei);
			}
			data.put("imei", imei);
			data.put("info", phoneName);
			data.put("phone_number", phoneNumber);
			data.put("locale", Locale.getDefault().getCountry());
		}catch(Exception ex){
			Log.e("api", ex.toString());
			return -1;
		}
		if(DeviceInfoUtil.isOnline(context)){
			postAsync(ApiMethods.register, data, handler);
			return 1;
		}else{
			(new DataDao(context)).insertRequest(ApiMethods.register, data);
			return 0;
		}
	}

	public int loadFilters(){
		JSONObject data=new JSONObject();
		String id=null;
		if(Param.USER_EMAIL.getValue()!=null)
			id=(String)Param.USER_EMAIL.getValue();
		else id=DeviceInfoUtil.getIMEI(context);
		try{
			data.put("id", id);
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
		if(DeviceInfoUtil.isOnline(context)){
			postAsync(ApiMethods.getUserFilters, data, new Handler(){
				@Override
				public void handleMessage(Message msg){
					JSONObject data=(JSONObject)msg.obj;
					List<Filter> filters=new ArrayList<Filter>();
					try{
						JSONArray array=data.getJSONArray("filters");
						for(int i=0;i<array.length();i++){
							JSONObject obj=array.getJSONObject(i);
							filters.add(new Filter(0, obj.getString("value"), obj.getInt("type")));
						}
					}catch(Exception ex){
						Log.e("json parse error", ex.toString());
					}
					if(!filters.isEmpty()){
						DataDao dao=new DataDao(context);
						dao.insertFilters(filters);
					}
				}
			});
			return 1;
		}else{
			(new DataDao(context)).insertRequest(ApiMethods.saveSms, data);
			return 0;
		}
	}

	public int addFilter(FilterType type, TopCategory category, String value, String example, Handler handler){
		JSONObject data=new JSONObject();
		try{
			data.put("type", type.ordinal());
			data.put("category", category.ordinal());
			data.put("value", value);
			data.put("example", example);
			data.put("locale", Locale.getDefault().getCountry());
		}catch(Exception ex){
			Log.e("api", ex.toString());
			return -1;
		}
		if(DeviceInfoUtil.isOnline(context)){
			postAsync(ApiMethods.addFilter, data, handler);
			return 1;
		}else{
			(new DataDao(context)).insertRequest(ApiMethods.addFilter, data);
			return 0;
		}
	}

	public void syncUserFilters(String id, List<Filter> filters, Handler handler){
		JSONObject data=new JSONObject();
		try{
			data.put("id", id);
			JSONArray array=new JSONArray();
			for(Filter f:filters){
				array.put(f.toJSON());
			}
			data.put("filters", array);
			data.put("locale", "ru");// Locale.getDefault().getCountry());
			postAsync(ApiMethods.syncUserFilters, data, handler);
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
	}

	public boolean check(String value, final Handler handler){
		try{
			final JSONObject data=new JSONObject();
			data.put("value", value);
			// data.put("locale", Locale.getDefault().getCountry());
			data.put("locale", "ru");
			if(DeviceInfoUtil.isOnline(context)){
				postAsync(ApiMethods.check, data, handler);
				return true;
			}else{
				(new ConnectionDialog(context, new ConnectionDialog.ConnectionListener(){
					@Override
					public void onConnectionReady(){
						postAsync(ApiMethods.check, data, handler);
					}
				})).show();
			}
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
		return false;
	}

	public boolean getTops(final Handler handler, boolean checkOnline){
		try{
			final JSONObject data=new JSONObject();
			data.put("locale", "ru");// Locale.getDefault().getCountry().toLowerCase());
			if(DeviceInfoUtil.isOnline(context)){
				postAsync(ApiMethods.getTops, data, handler);
				return true;
			}else if(checkOnline){
				(new ConnectionDialog(context, new ConnectionDialog.ConnectionListener(){
					@Override
					public void onConnectionReady(){
						postAsync(ApiMethods.getTops, data, handler);
					}
				})).show();
			}
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
		return false;
	}

	public boolean addVote(int filterId, final Handler handler){
		try{
			final JSONObject data=new JSONObject();
			if(Param.USER_EMAIL.getValue()!=null&&!((String)Param.USER_EMAIL.getValue()).isEmpty())
				data.put("id", Param.USER_EMAIL.getValue());
			else data.put("id", DeviceInfoUtil.getIMEI(context));
			data.put("filter_id", filterId);
			if(DeviceInfoUtil.isOnline(context)){
				postAsync(ApiMethods.addVote, data, handler);
				return true;
			}else{
				(new ConnectionDialog(context, new ConnectionDialog.ConnectionListener(){
					@Override
					public void onConnectionReady(){
						postAsync(ApiMethods.addVote, data, handler);
					}
				})).show();
			}
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
		return false;
	}

	public int registerBug(String id, String version, String stacktrace, Handler handler){
		JSONObject data=new JSONObject();
		try{
			data.put("id", id);
			data.put("version", version);
			data.put("stacktrace", stacktrace);
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
		if(DeviceInfoUtil.isOnline(context)){
			postAsync(ApiMethods.registerBug, data, handler);
			return 1;
		}else{
			(new DataDao(context)).insertRequest(ApiMethods.registerBug, data);
			return 0;
		}
	}

	public boolean sendWaitingRequests(){
		DataDao dao=new DataDao(context);
		List<Request> requests=dao.getRequests();
		if(!requests.isEmpty()){
			try{
				JSONObject data=new JSONObject();
				JSONArray array=new JSONArray();
				for(Request r:requests){
					JSONObject obj=r.getData();
					obj.put("method", r.getMethod());
					array.put(obj);
				}
				data.put("requests", array);
				postAsync(ApiMethods.pack, data, null);
			}catch(Exception ex){
				Log.e("api", ex.toString());
				return false;
			}
		}
		return true;
	}

	// ------------------Future Functions--------------
	public int saveSms(String id, List<SmsLogItem> sms, Handler handler){
		JSONObject data=new JSONObject();
		try{
			data.put("id", id);
			JSONArray array=new JSONArray();
			for(SmsLogItem s:sms){
				array.put(s.toJSON());
			}
			data.put("sms", array);
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
		if(DeviceInfoUtil.isOnline(context)){
			postAsync(ApiMethods.saveSms, data, handler);
			return 1;
		}else{
			(new DataDao(context)).insertRequest(ApiMethods.saveSms, data);
			return 0;
		}
	}

	public int loadSms(Context context, String id, int offset, int limit, Handler handler){
		JSONObject data=new JSONObject();
		try{
			data.put("id", id);
			data.put("offset", offset);
			data.put("limit", limit);
			postAsync(ApiMethods.loadSms, data, handler);
		}catch(Exception ex){
			Log.e("api", ex.toString());
		}
		if(DeviceInfoUtil.isOnline(context)){
			postAsync(ApiMethods.loadSms, data, handler);
			return 1;
		}else{
			(new DataDao(context)).insertRequest(ApiMethods.loadSms, data);
			return 0;
		}
	}

	// ------------------Sync---------------------------------
	public boolean sync(){
		if(DeviceInfoUtil.isOnline(context)){
			final DataDao dao=new DataDao(context);
			getTops(new Handler(){
				@Override
				public void handleMessage(Message msg){
					super.handleMessage(msg);
					try{
						JSONObject obj=(JSONObject)msg.obj;
						Set<TopItem> items=new HashSet<TopItem>();
						JSONArray array=obj.getJSONArray("words");
						for(int i=0;i<array.length();i++){
							items.add(new TopItem(i+1, array.getJSONObject(i)));
						}
						array=obj.getJSONArray("spam");
						for(int i=0;i<array.length();i++){
							items.add(new TopItem(i+1, array.getJSONObject(i)));
						}
						array=obj.getJSONArray("fraud");
						for(int i=0;i<array.length();i++){
							items.add(new TopItem(i+1, array.getJSONObject(i)));
						}
						dao.updateTop(items);
					}catch(Exception ex){
						Log.e("api", ex.toString());
					}
				}
			}, false);
			String id;
			if(Param.USER_EMAIL.getValue()!=null)
				id=(String)Param.USER_EMAIL.getValue();
			else id=DeviceInfoUtil.getIMEI(context);
			syncUserFilters(id, dao.getFilters(), new Handler(){
				@Override
				public void handleMessage(Message msg){
					super.handleMessage(msg);
				}
			});
			return true;
		}
		return false;
	}
}
