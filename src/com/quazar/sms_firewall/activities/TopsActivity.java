package com.quazar.sms_firewall.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.TopItem;
import com.quazar.sms_firewall.models.TopItem.TopCategory;
import com.quazar.sms_firewall.models.TopItem.TopType;

public class TopsActivity extends Activity{
	private static DataDao dataDao;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tops);
		TabHost tabHost=(TabHost)findViewById(R.id.tops_tabhost);
		tabHost.setup();
		dataDao=new DataDao(this);

		TabHost.TabSpec fraudNumbersTab=tabHost.newTabSpec("fraud top");
		fraudNumbersTab.setIndicator(getResources().getString(R.string.frauds));
		fraudNumbersTab.setContent(R.id.fraud_numbers_top);
		ListView fraudList=(ListView)findViewById(R.id.fraud_numbers_list);
		fraudList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.FRAUD));

		TabHost.TabSpec wordsTab=tabHost.newTabSpec("words top");
		wordsTab.setIndicator(getResources().getString(R.string.words));
		wordsTab.setContent(R.id.words_top);
		ListView wordsList=(ListView)findViewById(R.id.words_list);
		wordsList.setAdapter(getAdapter(TopType.WORD, TopCategory.GENERIC));		
		
		TabHost.TabSpec spamTab=tabHost.newTabSpec("spam top");
		spamTab.setIndicator(getResources().getString(R.string.spam));
		spamTab.setContent(R.id.spam_numbers_top);
		ListView spamList=(ListView)findViewById(R.id.spam_numbers_list);
		spamList.setAdapter(getAdapter(TopType.GENERIC, TopCategory.SPAM));

		tabHost.addTab(fraudNumbersTab);		
		tabHost.addTab(spamTab);		
		tabHost.addTab(wordsTab);
		tabHost.setCurrentTab(0);
	}
	public SimpleAdapter getAdapter(TopType type, TopCategory category){
		List<TopItem> topItems=dataDao.getTop(type, category);
		List<HashMap<String, Object>> list=new ArrayList<HashMap<String, Object>>();
		for(TopItem item:topItems){
			HashMap<String, Object> map=new HashMap<String, Object>();
			map.put("id", item.getId());
			map.put("position", item.getPos());
			map.put("value", item.getValue());
			map.put("votes", item.getVotes());			
			list.add(map);
		}
		return new SimpleAdapter(this, list, R.layout.tops_list_item, new String[]{"position", "value", "votes"}, new int[]{
				R.id.top_place, R.id.top_value, R.id.top_votes});
	}
}
