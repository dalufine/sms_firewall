package com.quazar.sms_firewall.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;

public class SelectSourceDialog extends AlertDialog {
	private final String TEXT_KEY = "text", ICON_KEY = "icon";
	public static final int FROM_CONTACTS = 0, FROM_INBOX_SMS = 1,
			FROM_INCOME_CALLS = 2, FROM_SUSPICIOUS_SMS = 3,
			FROM_FRAUDS_TOP = 4;	
	public SelectSourceDialog(final Context context, final SelectListener<Integer> listener, boolean forCheck) {
		super(context);
		View v = getLayoutInflater().inflate(R.layout.source_list, null);
		ListView listView = (ListView) v.findViewById(R.id.sources_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				listener.recieveSelection(position);
				dismiss();
			}
		});
		List<HashMap<String, Object>> sources = new ArrayList<HashMap<String, Object>>();
		int listId=R.array.sources;
		if(forCheck)
			listId=R.array.check_sources;
		String[] texts = context.getResources().getStringArray(listId);
		int[] icons = { R.drawable.contacts, R.drawable.inbox_sms,
				R.drawable.call, R.drawable.warning, R.drawable.top };
		if(forCheck)
			icons[3]=R.drawable.contacts;
		for (int i = 0; i < texts.length; i++) {
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put(TEXT_KEY, texts[i]);
			hm.put(ICON_KEY, icons[i]);
			sources.add(hm);
		}
		SimpleAdapter adapter = new SimpleAdapter(context, sources,
				R.layout.source_list_item, new String[] { TEXT_KEY, ICON_KEY },
				new int[] { R.id.item_text, R.id.item_icon });
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setView(v);
	}	
}
