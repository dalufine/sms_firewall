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
import com.quazar.sms_firewall.utils.DialogUtils;

public class SmsSelectDialog extends Dialog {
	public SmsSelectDialog(final Context context,
			final SelectListener<HashMap<String, Object>> listener,
			final List<HashMap<String, Object>> sms) {
		super(context, R.style.Dialog);

		View v = getLayoutInflater().inflate(R.layout.sms_list, null);
		ListView listView = (ListView) v.findViewById(R.id.sms_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				listener.recieveSelection(sms.get(position));
				dismiss();
			}
		});
		SimpleAdapter adapter = new SimpleAdapter(context, sms,
				R.layout.sms_list_item, new String[] { ContentUtils.SMS_NUMBER,
						ContentUtils.SMS_TEXT, ContentUtils.SMS_DATE },
				new int[] { R.id.sms_number, R.id.sms_text, R.id.sms_date });
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setContentView(v);

	}
}
