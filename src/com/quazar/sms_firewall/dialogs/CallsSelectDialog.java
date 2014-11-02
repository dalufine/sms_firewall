package com.quazar.sms_firewall.dialogs;

import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;
import com.quazar.sms_firewall.utils.ContentUtils;

public class CallsSelectDialog extends Dialog {
	public CallsSelectDialog(final Context context,
			final SelectListener<HashMap<String, Object>> listener,
			final List<HashMap<String, Object>> sources) {
		super(context, R.style.Dialog);

		View v = getLayoutInflater().inflate(R.layout.calls_list, null);
		ListView listView = (ListView) v.findViewById(R.id.calls_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				listener.recieveSelection(sources.get(position));
				CallsSelectDialog.this.dismiss();
			}
		});
		SimpleAdapter adapter = new SimpleAdapter(context, sources,
				R.layout.calls_list_item, new String[] {
						ContentUtils.NAME, ContentUtils.CALLS_DURATION,
						ContentUtils.DATE }, new int[] { R.id.call_name,
						R.id.call_duration, R.id.call_date });
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setContentView(v);
	}
}
