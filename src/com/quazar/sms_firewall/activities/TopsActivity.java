package com.quazar.sms_firewall.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.ResponseCodes;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.ComplainDialog;
import com.quazar.sms_firewall.dialogs.MessageExampleDialog;
import com.quazar.sms_firewall.dialogs.SelectSourceDialog;
import com.quazar.sms_firewall.dialogs.HelpDialog.Window;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.models.TopFilter;
import com.quazar.sms_firewall.models.TopFilter.TopCategory;
import com.quazar.sms_firewall.models.TopFilter.TopType;
import com.quazar.sms_firewall.models.UserFilter.FilterType;
import com.quazar.sms_firewall.network.ApiService;
import com.quazar.sms_firewall.utils.ContentUtils;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.LogUtil;

public class TopsActivity extends BaseActivity{

	private DataDao dataDao;
	private TabHost tabHost;
	private ListView fraudList, wordsList, spamList;
	private TopFilter selected;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.windowType=Window.TOPS;
		setContentView(R.layout.activity_tops);
		tabHost = (TabHost) findViewById(R.id.tops_tabhost);
		tabHost.setup();
		dataDao = new DataDao(this);
		TabHost.TabSpec fraudNumbersTab = tabHost.newTabSpec("fraud top");
		fraudNumbersTab.setIndicator(createTabView(tabHost.getContext(), getResources().getString(R.string.frauds),
				R.drawable.fraud_selector));
		fraudNumbersTab.setContent(R.id.fraud_numbers_top);
		fraudList = (ListView) findViewById(R.id.fraud_numbers_list);
		fraudList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.FRAUD));
		TabHost.TabSpec wordsTab = tabHost.newTabSpec("words top");
		wordsTab.setIndicator(createTabView(tabHost.getContext(), getResources().getString(R.string.words),
				R.drawable.word_selector));
		wordsTab.setContent(R.id.words_top);
		wordsList = (ListView) findViewById(R.id.words_list);
		wordsList.setAdapter(getAdapter(TopType.WORD, TopCategory.GENERIC));
		TabHost.TabSpec spamTab = tabHost.newTabSpec("spam top");
		spamTab.setIndicator(createTabView(tabHost.getContext(), getResources().getString(R.string.spam),
				R.drawable.spam_selector));
		spamTab.setContent(R.id.spam_numbers_top);
		spamList = (ListView) findViewById(R.id.spam_numbers_list);
		spamList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.SPAM));
		tabHost.addTab(fraudNumbersTab);
		tabHost.addTab(spamTab);
		tabHost.addTab(wordsTab);
		tabHost.setCurrentTab(0);
	}

	@Override
	protected void onDestroy(){
		if (dataDao != null) {
			dataDao.close();
		}
		super.onDestroy();
	}

	public void refreshLists(){
		fraudList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.FRAUD));
		wordsList.setAdapter(getAdapter(TopType.WORD, TopCategory.GENERIC));
		spamList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.SPAM));
	}

	public SimpleAdapter getAdapter(TopType type, TopCategory category){
		List<TopFilter> topItems = dataDao.getTopFilters(type, category);
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		for (TopFilter item:topItems) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", item.getId());
			map.put("position", item.getPos());
			map.put("value", item.getValue());
			map.put("votes", item.getVotes());
			map.put("type", item.getType().ordinal());
			list.add(map);
		}
		return new SimpleAdapter(this, list, R.layout.item_tops, new String[] {"position", "value", "votes" },
				new int[] {R.id.top_place, R.id.top_value, R.id.top_votes });
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == StateManager.CONTACTS_REQUEST_ID && resultCode == Activity.RESULT_OK) {
			sendAndProccessCheck(ContentUtils.getPhoneNumber(this, data.getData()));
		}
	}

	private void sendAndProccessCheck(final String value){
		try {
			new ApiService(TopsActivity.this).check(value, new Handler(){

				@Override
				public void handleMessage(Message msg){
					JSONObject data = (JSONObject) msg.obj;
					try {
						JSONArray filters = data.getJSONArray("filters");
						if (filters == null || filters.length() == 0) {
							Toast.makeText(TopsActivity.this,
									String.format(getResources().getString(R.string.no_filters_found), value),
									Toast.LENGTH_LONG).show();
						} else {
							data = filters.getJSONObject(0);
							MessageExampleDialog med =
									new MessageExampleDialog(TopsActivity.this, new TopFilter((Integer) data.get("id"),
											0, (Integer) data.get("votes"), (String) data.get("value"), (Integer) data
													.get("type"), (Integer) data.get("category")),
											jsonArrayToStringList(data.getJSONArray("examples")), new Handler(){

												@Override
												public void handleMessage(Message msg){
													final TopFilter item = (TopFilter) msg.obj;
													addExample(item);
												}
											});
							med.show();
						}
					}
					catch(Exception ex) {
						LogUtil.error(TopsActivity.this, "sendAndProccessCheck", ex);
					}
				}
			});
		}
		catch(Exception ex) {
			LogUtil.error(TopsActivity.this, "sendAndProccessCheck", ex);
		}
	}

	private void sendAndProcessComplain(String value, String example){
		(new ComplainDialog(this, value, example, example != null?FilterType.PHONE_NAME:FilterType.WORD,
				new DialogListener<Object>(){

					@Override
					public void ok(Object value){
						try {
							JSONObject json = (JSONObject) value;
							ResponseCodes response = ResponseCodes.getErrorByCode(json.getInt("code"));
							switch(response) {
								case OK:
									reloadTops(R.string.filter_added);
									break;
								case EXAMPLE_ADDED:
									reloadTops(R.string.example_added);
									break;
								case VOTE_ADDED:
									reloadTops(R.string.vote_added);
									break;
								case VOTED_TODAY:
								case ALREADY_HAS_EXAMPLE:
									reloadTops(R.string.have_such_filter_and_example);
									break;
								default:
									break;
							}
						}
						catch(Exception ex) {
							LogUtil.error(TopsActivity.this, "sendAndProcessComplain", ex);
						}
					}

					@Override
					public void cancel(){}
				})).show();
	}

	public void onCheck(View v){
		try {
			DialogUtils.showSourceSelectPopup(this, getResources().getString(R.string.select_source_to_check), Arrays
					.asList(SelectSourceDialog.FROM_FRAUDS_TOP, SelectSourceDialog.FROM_SUSPICIOUS_SMS), new Handler(){

				@SuppressWarnings({"rawtypes", "unchecked" })
				@Override
				public void handleMessage(Message msg){
					Map<String, Object> map = (Map) msg.obj;
					sendAndProccessCheck((String) map.get(ContentUtils.PROC_NUMBER));
				}
			});
		}
		catch(Exception ex) {
			LogUtil.error(this, "onCheck", ex);
		}
	}

	private List<String> jsonArrayToStringList(JSONArray array) throws JSONException{
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < array.length(); i++) {
			list.add(array.getJSONObject(i).getString("value"));
		}
		return list;
	}

	public void onComplain(View v){
		try {
			if (tabHost.getCurrentTab() == 2) {//word
				DialogUtils.showEnterValueDialog(this, R.string.type_word_to_complain, InputType.TYPE_CLASS_TEXT,
						new Handler(){

							@SuppressWarnings({"rawtypes", "unchecked" })
							@Override
							public void handleMessage(Message msg){
								Map<String, Object> map = (Map) msg.obj;
								String number = (String) map.get(ContentUtils.PROC_NUMBER);
								sendAndProcessComplain(number, null);
							}
						});
			} else {
				DialogUtils.showSmsSelectDialog(this, R.string.select_sms_to_complain, new Handler(){

					@SuppressWarnings({"rawtypes", "unchecked" })
					@Override
					public void handleMessage(Message msg){
						Map<String, Object> map = (Map) msg.obj;
						String number = (String) map.get(ContentUtils.PROC_NUMBER);
						sendAndProcessComplain(number, (String) map.get(ContentUtils.TEXT));
					}
				});
			}
		}
		catch(Exception ex) {
			LogUtil.error(this, "onComplain", ex);
		}
	}

	private void reloadTops(final Integer messageStringId) throws Exception{
		new ApiService(TopsActivity.this).loadTops(new Handler(){

			@Override
			public void handleMessage(Message msg){
				if (messageStringId != null) {
					Toast.makeText(TopsActivity.this, getString(messageStringId), Toast.LENGTH_LONG).show();
				}
				refreshLists();
			}
		});
	}

	public void onAddAll(View v){
		DialogUtils.showConfirmDialog(this, getResources().getString(R.string.confirmation), getResources().getString(
				R.string.add_all_from_top), new DialogListener<Boolean>(){

			@Override
			public void ok(Boolean value){
				TopType type = TopType.GENERIC;
				TopCategory category = TopCategory.FRAUD;
				switch(tabHost.getCurrentTab()) {
					case 1:
						category = TopCategory.SPAM;
						break;
					case 2:
						type = TopType.WORD;
						category = TopCategory.GENERIC;
						break;
				}
				try {
					dataDao.addAllToUserFilters(type, category);
				}
				catch(Exception ex) {
					LogUtil.error(TopsActivity.this, "onAddAll", ex);
				}
				Toast.makeText(TopsActivity.this, getResources().getString(R.string.filters_added), Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void cancel(){}
		});
	}

	public void onVote(View v){
		Map<String, Object> map = getSelectedItemProperties(v, 1);
		ApiService api = new ApiService(this);
		final Long filterId = (Long) map.get("id");
		try {
			api.addVote(filterId, new Handler(){

				@Override
				public void handleMessage(Message msg){
					try {
						JSONObject data = (JSONObject) msg.obj;
						ResponseCodes responseCode = ResponseCodes.getErrorByCode(data.getInt("code"));
						switch(responseCode) {
							case SYSTEM_ERROR:
								Toast.makeText(TopsActivity.this,
										TopsActivity.this.getResources().getString(R.string.server_error),
										Toast.LENGTH_LONG).show();
								break;
							case VOTED_TODAY:
								Toast.makeText(TopsActivity.this,
										TopsActivity.this.getResources().getString(R.string.voted_today_error),
										Toast.LENGTH_LONG).show();
								break;
							default:
								reloadTops(R.string.vote_added);
								break;
						}
					}
					catch(Exception ex) {
						LogUtil.error(TopsActivity.this, "onVote", ex);
					}
				}
			});
		}
		catch(Exception ex) {
			LogUtil.error(TopsActivity.this, "onVote", ex);
		}
	}

	public void onItemClick(View v){
		Map<String, Object> map = getSelectedItemProperties(v, 1);
		selected = new TopFilter();
		selected.setId((Long) map.get("id"));
		selected.setValue((String) map.get("value"));
		selected.setPos((Integer) map.get("position"));
		selected.setVotes((Integer) map.get("votes"));
		selected.setType(TopType.values()[(Integer) map.get("type")]);
		final MessageExampleDialog med =
				new MessageExampleDialog(this, selected, new DataDao(this).getTopFilterExamples(selected.getId()),
						new Handler(){

							@Override
							public void handleMessage(Message msg){
								TopFilter item = (TopFilter) msg.obj;
								if (msg.what == MessageExampleDialog.ADD_EXAMPLE) {
									addExample(item);
								} else {
									dataDao.insertUserFilter(item.getType() == TopType.WORD?FilterType.WORD
											:FilterType.PHONE_NAME, item.getValue());
									Toast.makeText(TopsActivity.this, R.string.filter_added, Toast.LENGTH_SHORT).show();
								}
							}
						});
		med.show();
	}

	private void addExample(final TopFilter item){
		try {
			DialogUtils.showSmsSelectDialog(TopsActivity.this, R.string.select_sms_for_example, new Handler(){

				@SuppressWarnings("unchecked")
				@Override
				public void handleMessage(Message msg){
					ApiService api = new ApiService(TopsActivity.this);
					try {
						api.addExample(item.getId(), (String) ((Map<String, Object>) msg.obj).get(ContentUtils.TEXT),
								new Handler(){

									@Override
									public void handleMessage(Message msg){
										try {
											reloadTops(null);
										}
										catch(Exception ex) {
											LogUtil.error(TopsActivity.this, "addExample", ex);
										}
										Toast.makeText(TopsActivity.this, R.string.example_added_to_filter,
												Toast.LENGTH_LONG).show();
									}
								});
					}
					catch(Exception ex) {
						LogUtil.error(TopsActivity.this, "addExample", ex);
					}
				}
			});
		}
		catch(Exception ex) {
			LogUtil.error(TopsActivity.this, "addExample", ex);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getSelectedItemProperties(View v, int level){
		ListView list = null;
		switch(tabHost.getCurrentTab()) {
			case 0:
				list = fraudList;
				break;
			case 1:
				list = spamList;
				break;
			case 2:
				list = wordsList;
		}
		while(level-- > 0)
			v = (View) v.getParent();
		int position = list.getPositionForView(v);
		return (Map<String, Object>) list.getAdapter().getItem(position);
	}
}
