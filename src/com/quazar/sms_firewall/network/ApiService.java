package com.quazar.sms_firewall.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.ResponseCodes;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.ConnectionDialog;
import com.quazar.sms_firewall.models.Request;
import com.quazar.sms_firewall.models.TopFilter;
import com.quazar.sms_firewall.models.TopFilter.TopCategory;
import com.quazar.sms_firewall.models.UserFilter;
import com.quazar.sms_firewall.models.UserFilter.FilterType;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class ApiService extends JSONClient{

	private Context context;

	public ApiService(Context context){
		this.context=context;
	}
	// ---------------Api implementation-----------------
	//------------------User functions----------------
	//if no connection registration is saved to db to send on active connection event
	public boolean register(Handler handler) throws Exception{
		JSONObject data=new JSONObject();
		data.put("user_id", getUserId());
		data.put("password", getUserPassword());
		data.put("imei", DeviceInfoUtil.getIMEI(context));
		data.put("phone_info", DeviceInfoUtil.getPhoneName());
		data.put("phone_number", DeviceInfoUtil.getMyPhoneNumber(context));
		data.put("locale", getLocale());

		return sendOrSaveIfNoConnection("/service/users/register", data, handler);
	}
	//use only at registartion
	public boolean loadUserFilters(){
		Handler handler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				saveFiltersToDb((JSONObject)msg.obj);
			}
		};
		return sendOrRequestConnection(String.format("/service/users/filters?user_id=%s&password=%s", getUserId(), getUserPassword()), null, handler);
	}
	//use at registration and periodically sync on active connection event
	public boolean saveUserFiltersToServer(List<UserFilter> filters, Handler handler) throws Exception{
		JSONObject data=new JSONObject();
		data.put("user_id", getUserId());
		data.put("password", getUserPassword());
		JSONArray array=new JSONArray();
		for(UserFilter f:filters){
			array.put(f.toJSON());
		}
		data.put("filters", array);

		return sendOrRequestConnection("/service/users/sync_filters", data, handler);
	}
	//------------------Common functions----------------
	//use at tops
	public boolean addFilter(FilterType type, TopCategory category, String value, String example, Handler handler) throws Exception{
		JSONObject data=new JSONObject();
		data.put("user_id", getUserId());
		data.put("type", type.name());
		data.put("category", category.name());
		data.put("value", value);
		data.put("example", example);
		data.put("locale", getLocale());
		return sendOrRequestConnection("/service/filters/add", data, handler);
	}
	//use at tops
	public boolean addFilterExample(Long filterId, String example, Handler handler) throws Exception{
		return sendOrRequestConnection(String.format("/service/filters/add_example?user_id=%s&filter_id=%d", getUserId(), filterId), null, handler);
	}

	public boolean check(String value, final Handler handler) throws Exception{
		return sendOrRequestConnection(String.format("/service/filters/check?locale=%s&value=%s", getLocale(), value), null, handler);
	}

	public boolean getTops(Handler handler) throws Exception{
		return sendOrRequestConnection(String.format("/service/filters/top?locale=%s", getLocale()), null, handler);
	}

	public boolean addVote(long filterId, final Handler handler) throws Exception{
		return sendOrRequestConnection(String.format("/service/filters/add_vote?user_id=%s&filter_id=%d", getUserId(), filterId), null, handler);
	}

	public boolean registerBug(String stacktrace) throws Exception{
		JSONObject data=new JSONObject();
		data.put("user_id", getUserId());
		data.put("version", Param.VERSION.getValue());
		data.put("stacktrace", stacktrace);
		return sendOrSaveIfNoConnection("/service/bugs/add", data, null);
	}

	public boolean sendWaitingRequests() throws Exception{
		DataDao dao=new DataDao(context);
		List<Request> requests=dao.getRequests();
		if(!requests.isEmpty()){
			for(Request r:requests){
				JSONObject response=post(r.getMethod(), r.getData());
				if(response!=null&&response.getInt("code")!=ResponseCodes.SYSTEM_ERROR.getCode()){
					dao.deleteRequest(r.getId());
				}
			}
		}
		return true;
	}

	public void loadTops(final Handler handler) throws Exception{
		getTops(new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				try{
					JSONObject obj=(JSONObject)msg.obj;
					Set<TopFilter> items=new HashSet<TopFilter>();
					Map<Long, List<String>> examples=new HashMap<Long, List<String>>();
					parseTopFilters(items, examples, obj.getJSONArray("words"));
					parseTopFilters(items, examples, obj.getJSONArray("spam"));
					parseTopFilters(items, examples, obj.getJSONArray("fraud"));
					new DataDao(context).updateTopFilters(items, examples);
					if(handler!=null){
						handler.dispatchMessage(new Message());
					}
				}catch(Exception ex){
					Log.e("api", ex.toString());
				}
			}
		});
	}

	private void parseTopFilters(Set<TopFilter> list, Map<Long, List<String>> examples, JSONArray array) throws JSONException{
		TopFilter tf=null;
		List<String> examplesList=null;
		for(int i=0;i<array.length();i++){
			JSONObject json=array.getJSONObject(i);
			tf=new TopFilter(i+1, json);
			examplesList=new ArrayList<String>();
			examples.put(tf.getId(), examplesList);
			JSONArray exArray=json.getJSONArray("examples");
			for(int j=0;j<exArray.length();j++){
				examplesList.add(exArray.getString(j));
			}
			list.add(tf);
		}
	}
	//---------------------Help functions------------------------------
	private boolean sendOrSaveIfNoConnection(String serviceUrl, JSONObject data, Handler handler){
		if(DeviceInfoUtil.isOnline(context)){
			if(data!=null)
				postAsync(serviceUrl, data, handler);
			else getAsync(serviceUrl, handler);
			return true;
		}else{
			(new DataDao(context)).insertRequest(serviceUrl, data);
		}
		return false;
	}

	private boolean sendOrRequestConnection(final String serviceUrl, final JSONObject data, final Handler handler){
		if(DeviceInfoUtil.isOnline(context)){
			if(data!=null)
				postAsync(serviceUrl, data, handler);
			else getAsync(serviceUrl, handler);
			return true;
		}else{
			(new ConnectionDialog(context, new ConnectionDialog.ConnectionListener(){
				@Override
				public void onConnectionReady(){
					if(data!=null)
						postAsync(serviceUrl, data, handler);
					else getAsync(serviceUrl, handler);
				}
			})).show();
		}
		return false;
	}

	private String getLocale(){
		String locale=Locale.getDefault().getCountry();
		if(locale==null)
			locale=Locale.ENGLISH.getCountry();
		return locale.toLowerCase();
	}

	private void saveFiltersToDb(JSONObject data){
		List<UserFilter> filters=new ArrayList<UserFilter>();
		try{
			JSONArray array=data.getJSONArray("filters");
			for(int i=0;i<array.length();i++){
				JSONObject obj=array.getJSONObject(i);
				filters.add(new UserFilter(obj.getLong("id"), obj.getString("value"), obj.getInt("type")));
			}
		}catch(Exception ex){
			Log.e("json parse error", ex.toString());
		}
		if(!filters.isEmpty()){
			DataDao dao=new DataDao(context);
			dao.insertUserFilters(filters);
		}
	}

	private String getUserPassword(){
		Object password=Param.LOGS_PASSWORD.getValue();
		if(password!=null)
			return (String)password;
		return null;
	}

	private String getUserId(){
		String id=null;
		Object email=Param.USER_EMAIL.getValue();
		if(email!=null&&((String)email).trim().length()!=0)
			id=(String)email;
		else id=DeviceInfoUtil.getIMEI(context);
		return id;
	}

	// ------------------Sync---------------------------------
	public boolean sync() throws Exception{
		if(DeviceInfoUtil.isOnline(context)){
			loadTops(null);
			saveUserFiltersToServer(new DataDao(context).getUserFilters(), null);
			return true;
		}
		return false;
	}
}
