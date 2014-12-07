package com.quazar.sms_firewall.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.LogsFilterDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.models.LogFilter;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.LogUtil;

public class LogsActivity extends BaseActivity implements OnScrollListener{

	private DataDao dataDao;
	private static final int PAGE_SIZE = 30;
	private TabHost tabHost;
	private SparseArray<List<HashMap<String, Object>>> lists;
	private List<ListView> listViews;
	private int visibleThreshold = 5;
	private int currentPage = 0;
	private int previousTotal = 0;
	private boolean loading = true;
	private LogsFilterDialog filterDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		tabHost = (TabHost) findViewById(R.id.logs_tabhost);
		tabHost.setup();
		dataDao = new DataDao(this);
		lists = new SparseArray<List<HashMap<String, Object>>>();
		listViews = new ArrayList<ListView>();
		try {
			filterDialog = new LogsFilterDialog(this, new DialogListener<LogFilter>(){

				@Override
				public void ok(LogFilter value){
					try {
						loadPage(1, value);
					}
					catch(Exception ex) {
						LogUtil.error(LogsActivity.this, "DialogListener", ex);
					}
				}

				@Override
				public void cancel(){}
			});
		}
		catch(Exception ex) {
			LogUtil.error(this, "onCreate", ex);
		}
		
		TabHost.TabSpec blockedLogsTab = tabHost.newTabSpec("blocked logs");				
		blockedLogsTab.setContent(R.id.blocked_logs);
		ListView blockedList = (ListView) findViewById(R.id.blocked_logs_list);
		blockedList.setId(3);
		String label;
		try {
			label = getResources().getString(R.string.blocked);
			blockedLogsTab.setIndicator(createTabView(tabHost.getContext(), String.format(label, dataDao.getLogsCnt(LogStatus.BLOCKED, null)),
					R.drawable.blocked_selector));
			blockedList.setAdapter(getAdapter(3, LogStatus.BLOCKED, null));
		}
		catch(Exception ex) {
			LogUtil.error(this, "onCreate", ex);
		}
		
		TabHost.TabSpec suspiciousLogsTab = tabHost.newTabSpec("suspicious logs");		
		suspiciousLogsTab.setContent(R.id.suspicious_logs);
		ListView suspiciousList = (ListView) findViewById(R.id.suspicious_logs_list);
		suspiciousList.setId(2);
		try {
			label = getResources().getString(R.string.suspicious);
			suspiciousLogsTab.setIndicator(createTabView(tabHost.getContext(), String.format(label, dataDao.getLogsCnt(LogStatus.SUSPICIOUS, null)),
					R.drawable.suspicious_selector));
			suspiciousList.setAdapter(getAdapter(2, LogStatus.SUSPICIOUS, null));
		}
		catch(Exception ex) {
			LogUtil.error(this, "onCreate", ex);
		}
		
		TabHost.TabSpec filteredLogsTab = tabHost.newTabSpec("filtered logs");		
		filteredLogsTab.setContent(R.id.filtered_logs);
		ListView filteredList = (ListView) findViewById(R.id.filtered_logs_list);
		filteredList.setId(1);
		try {
			label=getResources().getString(R.string.filtered);
			filteredLogsTab.setIndicator(createTabView(tabHost.getContext(), String.format(label, dataDao.getLogsCnt(LogStatus.FILTERED, null)),
					R.drawable.passed_selector));
			filteredList.setAdapter(getAdapter(1, LogStatus.FILTERED, null));
		}
		catch(Exception ex) {
			LogUtil.error(this, "onCreate", ex);
		}
		tabHost.addTab(filteredLogsTab);
		listViews.add(filteredList);
		tabHost.addTab(suspiciousLogsTab);
		listViews.add(suspiciousList);
		tabHost.addTab(blockedLogsTab);
		listViews.add(blockedList);
		tabHost.setCurrentTab(2);
		blockedList.setOnScrollListener(this);
		suspiciousList.setOnScrollListener(this);
		filteredList.setOnScrollListener(this);
	}

	@Override
	protected void onDestroy(){
		if (dataDao != null) {
			dataDao.close();
		}
		super.onDestroy();
	}

	private SimpleAdapter getAdapter(int viewId, LogStatus status, LogFilter filter) throws Exception{
		List<SmsLogItem> logs = dataDao.getLogs(status, filter, 0, PAGE_SIZE);
		lists.put(viewId, addItems(logs, new ArrayList<HashMap<String, Object>>()));
		return new SimpleAdapter(this, lists.get(viewId), R.layout.item_common, new String[] {"date", "body", "name",
				"number" }, new int[] {R.id.item_date, R.id.item_text, R.id.item_name, R.id.item_number });
	}

	private void loadPage(int page, LogFilter filter) throws Exception{
		int index = tabHost.getCurrentTab();
		LogStatus status = LogStatus.values()[index];
		ListView view = listViews.get(index);
		List<HashMap<String, Object>> list = lists.get(view.getId());
		if (page == 1) {
			list.clear();
		}
		int start = (page - 1) * PAGE_SIZE;
		addItems(dataDao.getLogs(status, filter, start, start + PAGE_SIZE), list);
		((SimpleAdapter) view.getAdapter()).notifyDataSetChanged();
	}

	private List<HashMap<String, Object>> addItems(List<SmsLogItem> items, List<HashMap<String, Object>> list){
		SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.default_datetime_format), Locale.getDefault());
		for (SmsLogItem log:items) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", log.getId());
			map.put("date", sdf.format(log.getDate()));
			map.put("body", log.getBody());
			map.put("name", log.getName());
			map.put("number", log.getNumber());
			list.add(map);
		}
		return list;
	}

	public void onShowFilterDialog(View v){
		filterDialog.show();
	}

	public void onResetFilter(View v){
		filterDialog.resetFilter();
		try {
			loadPage(1, null);
		}
		catch(Exception ex) {
			LogUtil.error(this, "onResetFilter", ex);
		}
		Toast.makeText(this, getResources().getString(R.string.filter_reseted), Toast.LENGTH_SHORT).show();
	}

	public void onClearTabLogs(View v){
		DialogUtils.showConfirmDialog(this, getResources().getString(R.string.warning), getResources().getString(
				R.string.logs_clear_conf), new DialogListener<Boolean>(){

			@Override
			public void ok(Boolean value){
				LogStatus status = LogStatus.values()[tabHost.getCurrentTab()];
				dataDao.clearLogs(status);
				filterDialog.resetFilter();
				try {
					loadPage(1, null);
				}
				catch(Exception ex) {
					LogUtil.error(LogsActivity.this, "onClearTabLogs", ex);
				}
			}

			@Override
			public void cancel(){}
		});
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
		((SimpleAdapter) view.getAdapter()).notifyDataSetChanged();
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
				currentPage++;
			}
		}
		if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
			LogFilter filter = null;
			if (filterDialog != null) {
				filter = filterDialog.getFilter();
			}
			try {
				loadPage(currentPage + 1, filter);
			}
			catch(Exception ex) {
				LogUtil.error(LogsActivity.this, "onScroll", ex);
			}
			loading = true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState){}
}
