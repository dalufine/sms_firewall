package com.quazar.sms_firewall.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.LogsFilterDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.models.LogFilter;
import com.quazar.sms_firewall.models.SmsLogItem;
import com.quazar.sms_firewall.models.SmsLogItem.LogStatus;
import com.quazar.sms_firewall.utils.ContentUtils;
import com.quazar.sms_firewall.utils.DialogUtils;
import com.quazar.sms_firewall.utils.DictionaryUtils;

public class LogsActivity extends BaseActivity implements OnScrollListener {
	private static DataDao dataDao;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		tabHost = (TabHost) findViewById(R.id.logs_tabhost);
		tabHost.setup();
		dataDao = new DataDao(this);
		lists = new SparseArray<List<HashMap<String, Object>>>();
		listViews = new ArrayList<ListView>();

		TabHost.TabSpec blockedLogsTab = tabHost.newTabSpec("blocked logs");
		blockedLogsTab.setIndicator(createTabView(tabHost.getContext(),
				getResources().getString(R.string.blocked),
				R.drawable.blocked_selector));
		blockedLogsTab.setContent(R.id.blocked_logs);
		ListView blockedList = (ListView) findViewById(R.id.blocked_logs_list);
		blockedList.setId(3);
		blockedList.setAdapter(getAdapter(3, LogStatus.BLOCKED, null));

		TabHost.TabSpec suspiciousLogsTab = tabHost
				.newTabSpec("suspicious logs");
		suspiciousLogsTab.setIndicator(createTabView(tabHost.getContext(),
				getResources().getString(R.string.suspicious),
				R.drawable.suspicious_selector));
		suspiciousLogsTab.setContent(R.id.suspicious_logs);
		ListView suspiciousList = (ListView) findViewById(R.id.suspicious_logs_list);
		suspiciousList.setId(2);
		suspiciousList.setAdapter(getAdapter(2, LogStatus.SUSPICIOUS, null));

		TabHost.TabSpec filteredLogsTab = tabHost.newTabSpec("filtered logs");
		filteredLogsTab.setIndicator(createTabView(tabHost.getContext(),
				getResources().getString(R.string.filtered),
				R.drawable.passed_selector));
		filteredLogsTab.setContent(R.id.filtered_logs);
		ListView filteredList = (ListView) findViewById(R.id.filtered_logs_list);
		filteredList.setId(1);
		filteredList.setAdapter(getAdapter(1, LogStatus.FILTERED, null));

		tabHost.addTab(filteredLogsTab);
		filteredList.setOnScrollListener(this);
		listViews.add(filteredList);
		tabHost.addTab(suspiciousLogsTab);
		suspiciousList.setOnScrollListener(this);
		listViews.add(suspiciousList);
		tabHost.addTab(blockedLogsTab);
		blockedList.setOnScrollListener(this);
		listViews.add(blockedList);
		tabHost.setCurrentTab(2);

		filterDialog = new LogsFilterDialog(this,
				new DialogListener<LogFilter>() {
					@Override
					public void ok(LogFilter value) {
						loadPage(1, value);
					}

					@Override
					public void cancel() {
					}
				});
	}

	public SimpleAdapter getAdapter(int viewId, LogStatus status,
			LogFilter filter) {
		List<SmsLogItem> logs = dataDao.getLogs(status, filter, 0, PAGE_SIZE);
		lists.put(viewId,
				addItems(logs, new ArrayList<HashMap<String, Object>>()));
		return new SimpleAdapter(this, lists.get(viewId),
				R.layout.logs_list_item, new String[] { "date", "body",
						"contact" }, new int[] { R.id.log_date, R.id.log_body,
						R.id.log_contact });
	}

	private void loadPage(int page, LogFilter filter) {
		int index = tabHost.getCurrentTab();
		LogStatus status = LogStatus.values()[index];
		ListView view = listViews.get(index);
		List<HashMap<String, Object>> list = lists.get(view.getId());
		if (page == 1) {
			list.clear();
		}
		int start = (page - 1) * PAGE_SIZE;
		addItems(dataDao.getLogs(status, filter, start, start + PAGE_SIZE),
				list);
		((SimpleAdapter) view.getAdapter()).notifyDataSetChanged();
	}

	private List<HashMap<String, Object>> addItems(List<SmsLogItem> items,
			List<HashMap<String, Object>> list) {
		for (SmsLogItem log : items) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", log.getId());
			map.put("date", ContentUtils.getDateFormater()
					.format(log.getDate()));
			map.put("body", log.getBody());
			map.put("contact",
					DictionaryUtils.getInstance().getContactsName(
							log.getPhoneName()));
			list.add(map);
		}
		return list;
	}

	public void onShowFilterDialog(View v) {
		filterDialog.show();
	}

	public void onResetFilter(View v) {
		filterDialog.resetFilter();
		loadPage(1, null);
	}

	public void onClearTabLogs(View v) {
		DialogUtils.showConfirmDialog(this,
				getResources().getString(R.string.warning), getResources()
						.getString(R.string.logs_clear_conf),
				new DialogListener<Boolean>() {
					@Override
					public void ok(Boolean value) {
						LogStatus status = LogStatus.values()[tabHost
								.getCurrentTab()];
						dataDao.clearLogs(status);
						filterDialog.resetFilter();
						loadPage(1, null);
					}

					@Override
					public void cancel() {
					}
				});
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		//
		Log.i("list", "scrolled list with id=" + view.getId());
		((SimpleAdapter) view.getAdapter()).notifyDataSetChanged();
		//
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
				currentPage++;
			}
		}
		if (!loading
				&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {

			loadPage(currentPage + 1, filterDialog.getFilter());
			loading = true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
