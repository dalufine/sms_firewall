package com.quazar.sms_firewall.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
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

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.models.Request;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.TopItem;
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
			httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));			
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
	@SuppressWarnings("unused")
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
	public String convertStreamToString(InputStream inputStream) {
		if (inputStream != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} catch (Exception ex) {
				Log.e("network", ex.toString());
			} finally {
				try {
					inputStream.close();
				} catch (Exception ex) {
				}
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	@SuppressWarnings("unused")
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
	public void register(Activity act, String email, String password,
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
	}

	public void addFilter(FilterType type, TopCategory category, String value,
			String example, Handler handler) {
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
	}

	public void syncUserFilters(String id, List<Filter> filters, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			JSONArray array = new JSONArray();
			for (Filter f : filters) {
				array.put(f.toJSON());
			}
			data.put("filters", array);
			data.put("locale", "ru");//Locale.getDefault().getCountry());
			postAsync(ApiMethods.syncUserFilters, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
	}

	public void check(String value, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("value", value);
			data.put("locale", Locale.getDefault().getCountry());
			postAsync(ApiMethods.check, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
	}

	public void getTops(Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("locale", "ru");//Locale.getDefault().getCountry().toLowerCase());
			postAsync(ApiMethods.getTops, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
	}

	public void addVote(String id, int filterId, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			data.put("filter_id", filterId);
			postAsync(ApiMethods.addVote, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
	}

	public void saveSms(String id, List<SmsLogItem> sms, Handler handler) {
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
	}

	public void loadSms(String id, int offset, int limit, Handler handler) {
		try {
			JSONObject data = new JSONObject();
			data.put("id", id);
			data.put("offset", offset);
			data.put("limit", limit);
			postAsync(ApiMethods.loadSms, data, handler);
		} catch (Exception ex) {
			Log.e("api", ex.toString());
		}
	}

	public void registerBug(String id, String version, String stacktrace,
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
	}

	public void sendWaitingRequests(DataDao dao, Handler handler) {
		List<Request> requests = dao.getRequests();
		if (!requests.isEmpty()) {
			try {
				JSONObject data = new JSONObject();
				JSONArray array = new JSONArray();
				for (Request r : requests) {
					JSONObject obj = r.getData();
					obj.put("method", r.getMethod());
					array.put(obj);
				}
				data.put("requests", array);
				postAsync(ApiMethods.pack, data, handler);
			} catch (Exception ex) {
				Log.e("api", ex.toString());
			}
		}
	}

	// ------------------Sync---------------------------------
	public boolean sync(Context context) {
		if (DeviceInfoUtil.isOnline(context)) {
			final DataDao dao = new DataDao(context);
			getTops(new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					try {
						JSONObject obj = (JSONObject) msg.obj;
						Set<TopItem> items = new HashSet<TopItem>();
						JSONArray array = obj.getJSONArray("words");
						for (int i = 0; i < array.length(); i++) {
							items.add(new TopItem(i + 1, array.getJSONObject(i)));
						}
						array = obj.getJSONArray("spam");
						for (int i = 0; i < array.length(); i++) {
							items.add(new TopItem(i + 1, array.getJSONObject(i)));
						}
						array = obj.getJSONArray("fraud");
						for (int i = 0; i < array.length(); i++) {
							items.add(new TopItem(i + 1, array.getJSONObject(i)));
						}
						dao.updateTop(items);
					} catch (Exception ex) {
						Log.e("api", ex.toString());
					}
				}
			});
			if(!dao.getFilters().isEmpty()){
				syncUserFilters(DeviceInfoUtil.getIMEI(context), dao.getFilters(), new Handler(){
					@Override
					public void handleMessage(Message msg) {					
						super.handleMessage(msg);
					}
				});
			}
			return true;
		}
		return false;
	}
}