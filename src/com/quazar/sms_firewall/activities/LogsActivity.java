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
import com.quazar.sms_firewall.models.Filter;
import com.quazar.sms_firewall.models.Filter.FilterType;
import com.quazar.sms_firewall.models.LogItem;
import com.quazar.sms_firewall.models.LogItem.LogStatus;
import com.quazar.sms_firewall.utils.ContentUtils;

public class LogsActivity extends Activity{
	private static DataDao dataDao;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		TabHost tabHost=(TabHost)findViewById(R.id.logs_tabhost);
		tabHost.setup();
		dataDao=new DataDao(this);

		TabHost.TabSpec blockedLogsTab=tabHost.newTabSpec("blocked logs");
		blockedLogsTab.setIndicator(getResources().getString(R.string.blocked));
		blockedLogsTab.setContent(R.id.blocked_logs);
		ListView blockedList=(ListView)findViewById(R.id.blocked_logs_list);
		blockedList.setAdapter(getAdapter(LogStatus.BLOCKED));

		TabHost.TabSpec suspiciousLogsTab=tabHost.newTabSpec("suspicious logs");
		suspiciousLogsTab.setIndicator(getResources().getString(R.string.suspicious));
		suspiciousLogsTab.setContent(R.id.suspicious_logs);
		ListView suspiciousList=(ListView)findViewById(R.id.suspicious_logs_list);
		suspiciousList.setAdapter(getAdapter(LogStatus.SUSPICIOUS));

		TabHost.TabSpec filteredLogsTab=tabHost.newTabSpec("filtered logs");
		filteredLogsTab.setIndicator(getResources().getString(R.string.filtered));
		filteredLogsTab.setContent(R.id.filtered_logs);
		ListView filteredList=(ListView)findViewById(R.id.filtered_logs_list);
		filteredList.setAdapter(getAdapter(LogStatus.FILTERED));

		tabHost.addTab(blockedLogsTab);
		tabHost.addTab(suspiciousLogsTab);
		tabHost.addTab(filteredLogsTab);
		tabHost.setCurrentTab(0);
	}
	public SimpleAdapter getAdapter(LogStatus status){
		List<LogItem> logs=dataDao.getLogs(status);
		List<HashMap<String, Object>> list=new ArrayList<HashMap<String, Object>>();
		for(LogItem log:logs){
			HashMap<String, Object> map=new HashMap<String, Object>();
			map.put("id", log.getId());			
			map.put("date", ContentUtils.getDateFormater().format(log.getDate()));
			list.add(map);
		}
		return new SimpleAdapter(this, list, R.layout.filters_list_item, new String[]{"filter_value"}, new int[]{R.id.filter_value});
	}
}
