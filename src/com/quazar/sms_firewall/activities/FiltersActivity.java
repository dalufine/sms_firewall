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
import com.quazar.sms_firewall.models.FilterModel;
import com.quazar.sms_firewall.models.FilterModel.FilterType;

public class FiltersActivity extends Activity {
    private static DataDao dataDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_filters);
	TabHost tabHost = (TabHost) findViewById(R.id.filters_tabhost);
	tabHost.setup();
	dataDao = new DataDao(this);
	List<FilterModel> filters = dataDao.getFilters();

	TabHost.TabSpec phoneFiltersTab = tabHost.newTabSpec("phone filters");
	phoneFiltersTab
		.setIndicator(getResources().getString(R.string.numbers));
	phoneFiltersTab.setContent(R.id.phone_filters);
	ListView phonesList = (ListView) findViewById(R.id.phone_filters_list);
	List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	for (FilterModel filter : filters) {
	    if (filter.getType() == FilterType.PHONE_NAME) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("filter_value", filter.getValue());
		list.add(map);
	    }
	}
	phonesList.setAdapter(new SimpleAdapter(this, list,
		R.layout.filters_list_item, new String[] { "filter_value" },
		new int[] { R.id.filter_value }));

	TabHost.TabSpec wordFiltersTab = tabHost.newTabSpec("word filters");
	wordFiltersTab.setIndicator(getResources().getString(R.string.words));
	wordFiltersTab.setContent(R.id.word_filters);
	ListView wordsList = (ListView) findViewById(R.id.word_filters_list);
	list = new ArrayList<HashMap<String, Object>>();
	for (FilterModel filter : filters) {
	    if (filter.getType() == FilterType.WORD) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("filter_value", filter.getValue());
		list.add(map);
	    }
	}
	wordsList.setAdapter(new SimpleAdapter(this, list,
		R.layout.filters_list_item, new String[] { "filter_value" },
		new int[] { R.id.filter_value }));

	tabHost.addTab(phoneFiltersTab);
	tabHost.addTab(wordFiltersTab);
	tabHost.setCurrentTab(0);
    }
}
