package com.quazar.sms_firewall.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.SelectSourceDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.models.UserFilter;
import com.quazar.sms_firewall.models.UserFilter.FilterType;
import com.quazar.sms_firewall.utils.ContentUtils;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.DictionaryUtils;

public class FiltersActivity extends BaseActivity{
	private static DataDao dataDao;
	private TabHost tabHost;
	private ListView phonesList, wordsList;
	private static int PHONE_NUMBERS_TAB=0;//WORDS_TAB=1

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filters);
		tabHost=(TabHost)findViewById(R.id.filters_tabhost);
		tabHost.setup();
		dataDao=new DataDao(this);

		TabHost.TabSpec phoneFiltersTab=tabHost.newTabSpec("phone filters");
		phoneFiltersTab.setIndicator(createTabView(tabHost.getContext(), getResources().getString(R.string.numbers), R.drawable.phone_selector));
		phoneFiltersTab.setContent(R.id.phone_filters);

		phonesList=(ListView)findViewById(R.id.phone_filters_list);
		phonesList.setAdapter(getAdapter(FilterType.PHONE_NAME));

		TabHost.TabSpec wordFiltersTab=tabHost.newTabSpec("word filters");
		wordFiltersTab.setIndicator(createTabView(tabHost.getContext(), getResources().getString(R.string.words), R.drawable.word_selector));
		wordFiltersTab.setContent(R.id.word_filters);
		wordsList=(ListView)findViewById(R.id.word_filters_list);
		wordsList.setAdapter(getAdapter(FilterType.WORD));

		tabHost.addTab(phoneFiltersTab);
		tabHost.addTab(wordFiltersTab);
		tabHost.setCurrentTab(PHONE_NUMBERS_TAB);
	}
	public SimpleAdapter getAdapter(FilterType type){
		List<UserFilter> filters=dataDao.getUserFilters();
		List<HashMap<String, Object>> list=new ArrayList<HashMap<String, Object>>();
		for(UserFilter filter:filters){
			if(filter.getType()==type){
				HashMap<String, Object> map=new HashMap<String, Object>();
				if(type==FilterType.PHONE_NAME)
					map.put("filter_value", DictionaryUtils.getInstance().getContactsName(filter.getValue()));
				else map.put("filter_value", filter.getValue());
				map.put("type", type);
				map.put("id", filter.getId());
				list.add(map);
			}
		}
		return new SimpleAdapter(this, list, R.layout.item_filters, new String[] { "filter_value" }, new int[] { R.id.filter_value });
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==StateManager.CONTACTS_REQUEST_ID&&resultCode==Activity.RESULT_OK){
			String phoneNumber=ContentUtils.getPhoneNumber(this, data.getData());
			dataDao.insertUserFilter(FilterType.PHONE_NAME, phoneNumber);
			phonesList.setAdapter(getAdapter(FilterType.PHONE_NAME));
		}
	}
	public void onAddFilter(View v){
		if(tabHost.getCurrentTab()==PHONE_NUMBERS_TAB){
			DialogUtils.showSourceSelectPopup(this, Arrays.asList(SelectSourceDialog.FROM_ENTER_WORD), new Handler(new Handler.Callback(){
				@Override
				public boolean handleMessage(Message msg){
					HashMap<String, Object> map=(HashMap<String, Object>)msg.obj;
					Object value=map.get(ContentUtils.NUMBER);
					if(value!=null){
						dataDao.insertUserFilter(FilterType.PHONE_NAME, (String)value);
					}
					phonesList.setAdapter(getAdapter(FilterType.PHONE_NAME));
					return false;
				}
			}));
		}else{
			DialogUtils.showEnterValueDialog(this, R.string.enter_word, InputType.TYPE_CLASS_TEXT, new Handler(new Handler.Callback(){
				@Override
				public boolean handleMessage(Message msg){
					Map<String, Object> map=(Map)msg.obj;
					DataDao dao=new DataDao(FiltersActivity.this);
					dao.insertUserFilter(FilterType.WORD, (String)map.get(ContentUtils.NUMBER));
					dao.close();
					wordsList.setAdapter(getAdapter(FilterType.WORD));
					return false;
				}
			}));
		}
	}
	public void onClearFilters(View v){
		DialogUtils.showConfirmDialog(this, getResources().getString(R.string.confirmation), getResources().getString(R.string.filters_clear_conf), new DialogListener<Boolean>(){
			@Override
			public void ok(Boolean value){
				if(tabHost.getCurrentTab()==PHONE_NUMBERS_TAB){
					dataDao.clearUserFilters(FilterType.PHONE_NAME);
					phonesList.setAdapter(getAdapter(FilterType.PHONE_NAME));
				}else{
					dataDao.clearUserFilters(FilterType.WORD);
					wordsList.setAdapter(getAdapter(FilterType.WORD));
				}
			}
			@Override
			public void cancel(){}
		});

	}
	public void onRemoveFilter(final View view){
		DialogUtils.showConfirmDialog(this, getResources().getString(R.string.confirmation), getResources().getString(R.string.filter_delete_conf), new DialogListener<Boolean>(){
			@Override
			public void ok(Boolean value){
				if(tabHost.getCurrentTab()==PHONE_NUMBERS_TAB){
					int position=phonesList.getPositionForView((View)view.getParent());
					HashMap<String, Object> map=(HashMap<String, Object>)phonesList.getAdapter().getItem(position);
					dataDao.deleteUserFilter((Long)map.get("id"));
					phonesList.setAdapter(getAdapter(FilterType.PHONE_NAME));
				}else{
					int position=wordsList.getPositionForView((View)view.getParent());
					HashMap<String, Object> map=(HashMap<String, Object>)wordsList.getAdapter().getItem(position);
					dataDao.deleteUserFilter((Long)map.get("id"));
					wordsList.setAdapter(getAdapter(FilterType.WORD));
				}
			}
			@Override
			public void cancel(){}
		});
	}
}
