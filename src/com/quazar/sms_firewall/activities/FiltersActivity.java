package com.quazar.sms_firewall.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.popups.MenuPopup;
import com.quazar.sms_firewall.popups.SelectListener;

public class FiltersActivity extends Activity{
	private static DataDao dataDao;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filters);
		TabHost tabHost=(TabHost)findViewById(R.id.filters_tabhost);
		tabHost.setup();
		dataDao=new DataDao(this);		

		TabHost.TabSpec phoneFiltersTab=tabHost.newTabSpec("phone filters");
		phoneFiltersTab.setIndicator(getResources().getString(R.string.numbers));
		phoneFiltersTab.setContent(R.id.phone_filters);

		ListView phonesList=(ListView)findViewById(R.id.phone_filters_list);
		OnItemClickListener listener=new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				final ListView list=(ListView)parent;
				final HashMap<String, Object> map=(HashMap<String, Object>)list.getAdapter().getItem(position);				
				MenuPopup popup=new MenuPopup(FiltersActivity.this, new String[]{
						getResources().getString(R.string.delete), getResources().getString(R.string.cancel)}, new SelectListener<Integer>(){
					@Override
					public void recieveSelection(Integer selection){
						if(selection==0) {
							dataDao.deleteFilter((Integer)map.get("id"));
							list.setAdapter(getAdapter((FilterType)map.get("type")));							
						}						
					}
				});
				popup.show();
			}
		};
		phonesList.setOnItemClickListener(listener);		
		phonesList.setAdapter(getAdapter(FilterType.PHONE_NAME));

		TabHost.TabSpec wordFiltersTab=tabHost.newTabSpec("word filters");
		wordFiltersTab.setIndicator(getResources().getString(R.string.words));
		wordFiltersTab.setContent(R.id.word_filters);
		ListView wordsList=(ListView)findViewById(R.id.word_filters_list);
		wordsList.setOnItemClickListener(listener);		
		wordsList.setAdapter(getAdapter(FilterType.WORD));

		tabHost.addTab(phoneFiltersTab);
		tabHost.addTab(wordFiltersTab);
		tabHost.setCurrentTab(0);
	}
	public SimpleAdapter getAdapter(FilterType type) {
		List<Filter> filters=dataDao.getFilters();
		List<HashMap<String, Object>>list=new ArrayList<HashMap<String, Object>>();
		for(Filter filter:filters){
			if(filter.getType()==type){
				HashMap<String, Object> map=new HashMap<String, Object>();
				map.put("filter_value", filter.getValue());
				map.put("type", type);
				map.put("id", filter.getId());
				list.add(map);
			}
		}
		return new SimpleAdapter(this, list, R.layout.filters_list_item, new String[]{"filter_value"}, new int[]{R.id.filter_value});
	}
}
