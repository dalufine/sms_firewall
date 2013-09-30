package com.quazar.sms_firewall.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.MessageExampleDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.models.TopItem;
import com.quazar.sms_firewall.models.TopItem.TopCategory;
import com.quazar.sms_firewall.models.TopItem.TopType;
import com.quazar.sms_firewall.network.ApiClient;
import com.quazar.sms_firewall.utils.DialogUtils;

public class TopsActivity extends BaseActivity {
	private static DataDao dataDao;
	private TabHost tabHost;
	private ListView fraudList, wordsList, spamList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tops);
		tabHost = (TabHost) findViewById(R.id.tops_tabhost);
		tabHost.setup();
		dataDao = new DataDao(this);

		TabHost.TabSpec fraudNumbersTab = tabHost.newTabSpec("fraud top");
		fraudNumbersTab.setIndicator(getResources().getString(R.string.frauds));
		fraudNumbersTab.setContent(R.id.fraud_numbers_top);
		fraudList = (ListView) findViewById(R.id.fraud_numbers_list);
		fraudList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.FRAUD));

		TabHost.TabSpec wordsTab = tabHost.newTabSpec("words top");
		wordsTab.setIndicator(getResources().getString(R.string.words));
		wordsTab.setContent(R.id.words_top);
		wordsList = (ListView) findViewById(R.id.words_list);
		wordsList.setAdapter(getAdapter(TopType.WORD, TopCategory.GENERIC));

		TabHost.TabSpec spamTab = tabHost.newTabSpec("spam top");
		spamTab.setIndicator(getResources().getString(R.string.spam));
		spamTab.setContent(R.id.spam_numbers_top);
		spamList = (ListView) findViewById(R.id.spam_numbers_list);
		spamList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.SPAM));

		tabHost.addTab(fraudNumbersTab);
		tabHost.addTab(spamTab);
		tabHost.addTab(wordsTab);
		tabHost.setCurrentTab(0);
	}

	public SimpleAdapter getAdapter(TopType type, TopCategory category) {
		List<TopItem> topItems = dataDao.getTop(type, category);
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		for (TopItem item : topItems) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", item.getId());
			map.put("position", item.getPos());
			map.put("value", item.getValue());
			map.put("votes", item.getVotes());
			map.put("example", item.getExamples());
			map.put("type", item.getType().ordinal());
			list.add(map);
		}
		return new SimpleAdapter(this, list, R.layout.tops_list_item,
				new String[] { "position", "value", "votes" }, new int[] {
						R.id.top_place, R.id.top_value, R.id.top_votes });
	}

	public void onRefresh(View v) {

	}

	public void onCheck(View v) {
		DialogUtils.showSourceSelectPopup(this, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				ApiClient api = new ApiClient(TopsActivity.this);
				api.check(msg.obj.toString(), new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// TODO show example
						Toast.makeText(TopsActivity.this, msg.obj.toString(),
								Toast.LENGTH_LONG).show();
					}
				});
			}
		}, true);
	}

	public void onComplain(View v) {
		DialogUtils.showSourceSelectPopup(this, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				ApiClient api = new ApiClient(TopsActivity.this);
				// TODO replace call to complain
				api.check(msg.obj.toString(), new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// TODO show example
						Toast.makeText(TopsActivity.this, msg.obj.toString(),
								Toast.LENGTH_LONG).show();
					}
				});
			}
		}, true);
	}

	public void onAddAll(View v) {
		DialogUtils.showConfirmDialog(this,
				getResources().getString(R.string.warning), getResources()
						.getString(R.string.add_all_from_top),
				new DialogListener<Boolean>() {
					@Override
					public void ok(Boolean value) {
						TopType type = TopType.GENERIC;
						TopCategory category = TopCategory.FRAUD;
						switch (tabHost.getCurrentTab()) {
						case 1:
							category = TopCategory.SPAM;
							break;
						case 2:
							type = TopType.WORD;
							category = TopCategory.GENERIC;
							break;
						}
						dataDao.addAllToFilters(type, category);
						Toast.makeText(
								TopsActivity.this,
								getResources()
										.getString(R.string.filters_added),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void cancel() {
					}
				});
	}

	public void onVote(View v) {		
		Map<String, Object> map = getSelectedItemProperties(v, 1);
		ApiClient api = new ApiClient(this);
		api.addVote((Integer) map.get("id"), new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Toast.makeText(TopsActivity.this, msg.obj.toString(),
						Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public void onItemClick(View v){
		Map<String, Object> map = getSelectedItemProperties(v, 1);
		MessageExampleDialog med=new MessageExampleDialog(this, new TopItem((Integer)map.get("id"), (Integer)map.get("position"), 
				(Integer)map.get("votes"), (String)map.get("value"), (List)map.get("example"), (Integer)map.get("type"), 0));
		med.show();
	}
	private Map<String, Object> getSelectedItemProperties(View v, int level){
		ListView list = null;
		switch (tabHost.getCurrentTab()) {
		case 0:
			list = fraudList;
			break;
		case 1:
			list = spamList;
			break;
		case 2:
			list = wordsList;
		}
		while(level-->0)
			v=(View)v.getParent();
		int position = list.getPositionForView(v);
		HashMap<String, Object> map = (HashMap<String, Object>) list
				.getAdapter().getItem(position);
		return map;
	}
}
