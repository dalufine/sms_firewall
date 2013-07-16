package com.quazar.sms_firewall.network;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.models.Request;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.TopItem.TopCategory;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class ApiClient {
	private static final String HOST = "http://xn--80ae0achg0hya.xn--p1ai/sms_firewall/services/api/";

	public static enum ApiMethods {
		register, addFilter, syncUserFilters, check, getTops, addVote, saveSms, loadSms, registerBug, pack
	};

	private JSONObject get(ApiMethods method) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			String url = HOST + method.toString();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = (HttpResponse) httpclient.execute(httpGet);
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

	private JSONObject post(ApiMethods method, JSONObject data) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(HOST + method.toString());
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("data", encodeString(data
					.toString())));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httpPost);
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

	// =================Async calls========================
	private void getAsync(final ApiMethods method, final Handler handler) {
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

	private void postAsync(final ApiMethods method, final JSONObject data,
			final Handler handler) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				JSONObject result = post(method, data);
				Message message = handler.obtainMessage(1, result);
				handler.sendMessage(message);
			}
		};
		thread.start();
	}

	// ------------------------Help functions----------------------
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

	private String encodeString(String source) {
		try {
			byte[] bytes = Base64.encode(source.getBytes("UTF-8"),
					Base64.DEFAULT);
			return URLEncoder.encode(new String(bytes, "UTF-8"), "UTF-8");
		} catch (Exception ex) {
			Log.e("param encode", ex.toString());
		}
		return null;
	}

	// ---------------Api implementation-----------------
	public JSONObject register(Activity act, String email, String password,
			Handler handler) {
		String imei = DeviceInfoUtil.getIMEI(act), phoneName = DeviceInfoUtil
				.getPhoneName(), phoneNumber = DeviceInfoUtil
				.getMyPhoneNumber(act);
		JSONObject data = new JSONObject();
		try {
			if (email != null) {
				data.put("id", email);
				data.put("password", password);
			} else {
				data.put("id", imei);
			}
			data.put("imei", imei);
			data.put("info", phoneName);
			data.put("phone_number", phoneNumber);
			data.put("locale", Locale.getDefault().getCountry());
			postAsync(ApiMethods.register, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject addFilter(FilterType type, TopCategory category,
			String value, String example, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("type", type.ordinal());
			data.put("category", category.ordinal());
			data.put("value", value);
			data.put("example", example);
			data.put("locale", Locale.getDefault().getCountry());
			postAsync(ApiMethods.addFilter, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject syncUserFilters(String id, List<Filter> filters,
			Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			JSONArray array = new JSONArray();
			for (Filter f : filters) {
				array.put(f.toJSON());
			}
			data.put("filters", array);
			data.put("locale", Locale.getDefault().getCountry());
			postAsync(ApiMethods.syncUserFilters, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject check(String value, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("value", value);
			data.put("locale", Locale.getDefault().getCountry());
			postAsync(ApiMethods.check, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject getTops(Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("locale", Locale.getDefault().getCountry());
			postAsync(ApiMethods.getTops, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject addVote(String id, int filterId, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			data.put("filter_id", filterId);
			postAsync(ApiMethods.addVote, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject saveSms(String id, List<SmsLogItem> sms, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			JSONArray array = new JSONArray();
			for (SmsLogItem s : sms) {
				array.put(s.toJSON());
			}
			data.put("sms", array);
			postAsync(ApiMethods.saveSms, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject loadSms(String id, int offset, int limit, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			data.put("offset", offset);
			data.put("limit", limit);
			postAsync(ApiMethods.loadSms, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONObject registerBug(String id, String version, String stacktrace,
			Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			data.put("version", version);
			data.put("stacktrace", stacktrace);
			postAsync(ApiMethods.registerBug, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
		return null;
	}

	public JSONArray sendWaitingRequests(DataDao dao, Handler handler) {
		List<Request> requests = dao.getRequests();
		if (!requests.isEmpty()) {
			try {
				JSONObject data = new JSONObject();
				JSONArray array = new JSONArray();
				for(Request r:requests){
					JSONObject obj=r.getData();
					obj.put("method", r.getMethod());
					array.put(obj);
				}
				data.put("requests", array);
				postAsync(ApiMethods.pack, data, handler);
			} catch (Exception ex) {
				Log.e("api", ex.toString());
			}
		}
		return null;
	}
}
