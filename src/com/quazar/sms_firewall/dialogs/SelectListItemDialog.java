package com.quazar.sms_firewall.dialogs;

import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;
import com.quazar.sms_firewall.utils.ContentUtils;

public class SelectListItemDialog extends Dialog{
	public SelectListItemDialog(final Context context, String title, final List<HashMap<String, Object>> items, final SelectListener<HashMap<String, Object>> listener){
		super(context, R.style.Dialog);
		setContentView(R.layout.list_common);
		((TextView)findViewById(R.id.list_title)).setText(title);
		ListView listView=(ListView)findViewById(R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				listener.recieveSelection(items.get(position));
				dismiss();
			}
		});
		SimpleAdapter adapter=
				new SimpleAdapter(context, items, R.layout.item_common, new String[] { ContentUtils.NAME, ContentUtils.NUMBER, ContentUtils.TEXT, ContentUtils.DATE }, new int[] { R.id.item_name, R.id.item_number,
						R.id.item_text, R.id.item_date });
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

	}
}