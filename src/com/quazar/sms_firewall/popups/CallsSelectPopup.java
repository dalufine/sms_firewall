package com.quazar.sms_firewall.popups;

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
import com.quazar.sms_firewall.utils.ContentUtils;

public class CallsSelectPopup extends AlertDialog {
	public CallsSelectPopup(final Context context, final SelectListener<HashMap<String, Object>> listener) {
		super(context);
		View v = getLayoutInflater().inflate(R.layout.calls_list, null);
		ListView listView = (ListView) v.findViewById(R.id.calls_list);
		final List<HashMap<String, Object>> sources = ContentUtils.getIncomeCalls(context);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				listener.recieveSelection(sources.get(position));
				CallsSelectPopup.this.dismiss();
			}
		});		
		SimpleAdapter adapter = new SimpleAdapter(context, sources,
				R.layout.calls_list_item, new String[] {
						ContentUtils.CALLS_NAME,
						ContentUtils.CALLS_DURATION, ContentUtils.CALLS_DATE },
				new int[] {R.id.call_name,
						R.id.call_duration, R.id.call_date });
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setView(v);
	}
}
