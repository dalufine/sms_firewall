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

public class SmsSelectPopup extends AlertDialog {	
	public SmsSelectPopup(final Context context, final SelectListener listener) {
		super(context);
		View v = getLayoutInflater().inflate(R.layout.sms_list, null);
		ListView listView = (ListView) v.findViewById(R.id.sms_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				listener.recieveSelection(position);
				SmsSelectPopup.this.hide();
			}
		});
		List<HashMap<String, Object>> sources = ContentUtils.getInboxSms(context);
		SimpleAdapter adapter = new SimpleAdapter(context, sources,
				R.layout.sms_list_item, new String[] {ContentUtils.SMS_NUMBER, ContentUtils.SMS_TEXT},
				new int[] {R.id.sms_number, R.id.sms_text});
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setView(v);
	}	
}
