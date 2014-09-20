package com.quazar.sms_firewall.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.view.View;
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

public class LogsActivity extends BaseActivity {
	private static DataDao dataDao;
	private TabHost tabHost;
	private List<ListView> lists;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		tabHost = (TabHost) findViewById(R.id.logs_tabhost);
		tabHost.setup();
		dataDao = new DataDao(this);

		TabHost.TabSpec blockedLogsTab = tabHost.newTabSpec("blocked logs");
		blockedLogsTab.setIndicator(createTabView(tabHost.getContext(),
				getResources().getString(R.string.blocked),
				R.drawable.blocked));
		blockedLogsTab.setContent(R.id.blocked_logs);
		ListView blockedList = (ListView) findViewById(R.id.blocked_logs_list);
		blockedList.setAdapter(getAdapter(LogStatus.BLOCKED, null));

		TabHost.TabSpec suspiciousLogsTab = tabHost
				.newTabSpec("suspicious logs");
		suspiciousLogsTab.setIndicator(createTabView(tabHost.getContext(),
				getResources().getString(R.string.suspicious),
				R.drawable.suspicious));
		suspiciousLogsTab.setContent(R.id.suspicious_logs);
		ListView suspiciousList = (ListView) findViewById(R.id.suspicious_logs_list);
		suspiciousList.setAdapter(getAdapter(LogStatus.SUSPICIOUS, null));

		TabHost.TabSpec filteredLogsTab = tabHost.newTabSpec("filtered logs");
		filteredLogsTab.setIndicator(createTabView(tabHost.getContext(),
				getResources().getString(R.string.filtered),
				R.drawable.passed));
		filteredLogsTab.setContent(R.id.filtered_logs);
		ListView filteredList = (ListView) findViewById(R.id.filtered_logs_list);
		filteredList.setAdapter(getAdapter(LogStatus.FILTERED, null));

		lists = new ArrayList<ListView>();
		tabHost.addTab(filteredLogsTab);
		lists.add(filteredList);
		tabHost.addTab(suspiciousLogsTab);
		lists.add(suspiciousList);
		tabHost.addTab(blockedLogsTab);
		lists.add(blockedList);
		tabHost.setCurrentTab(2);
	}

	public SimpleAdapter getAdapter(LogStatus status, LogFilter filter) {
		List<SmsLogItem> logs = dataDao.getLogs(status, filter, null, null);
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		for (SmsLogItem log : logs) {
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
		return new SimpleAdapter(this, list, R.layout.logs_list_item,
				new String[] { "date", "body", "contact" }, new int[] {
						R.id.log_date, R.id.log_body, R.id.log_contact });
	}

	public void onShowFilterDialog(View v) {
		LogsFilterDialog lfd = new LogsFilterDialog(this,
				new DialogListener<LogFilter>() {
					@Override
					public void ok(LogFilter value) {
						lists.get(tabHost.getCurrentTab()).setAdapter(
								getAdapter(LogStatus.values()[tabHost
										.getCurrentTab()], value));
					}

					@Override
					public void cancel() {
					}
				});
		lfd.show();
	}

	public void onResetFilter(View v) {
		int tab = tabHost.getCurrentTab();
		LogStatus status = LogStatus.values()[tab];
		LogsFilterDialog.resetFilter();
		lists.get(tab).setAdapter(getAdapter(status, null));
	}

	public void onClearTabLogs(View v) {
		DialogUtils.showConfirmDialog(this,
				getResources().getString(R.string.warning), getResources()
						.getString(R.string.logs_clear_conf),
				new DialogListener<Boolean>() {
					@Override
					public void ok(Boolean value) {
						int tab = tabHost.getCurrentTab();
						LogStatus status = LogStatus.values()[tab];
						dataDao.clearLogs(status);
						lists.get(tab).setAdapter(getAdapter(status, null));
					}

					@Override
					public void cancel() {
					}
				});
	}
}
