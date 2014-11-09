package com.quazar.sms_firewall.dialogs;

import java.util.ArrayList;
import java.util.Collections;
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

public class SelectSourceDialog extends Dialog{
	private final String TEXT_KEY="text", ICON_KEY="icon";
	public static final int FROM_CONTACTS=0, FROM_INBOX_SMS=1, FROM_INCOME_CALLS=2, FROM_SUSPICIOUS_SMS=3, FROM_FRAUDS_TOP=4, FROM_ENTER_WORD=5, FROM_ENTER_PHONE=6;

	public SelectSourceDialog(final Context context, String title, final SelectListener<Integer> listener, final List<Integer> exclude){
		super(context, R.style.Dialog);
		Collections.sort(exclude);
		View v=getLayoutInflater().inflate(R.layout.list_source, null);
		if(title!=null){
			((TextView)v.findViewById(R.id.select_source_popup_title)).setText(title);
		}
		ListView listView=(ListView)v.findViewById(R.id.sources_list);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				for(Integer i:exclude){
					if(i<=position)
						position++;
				}
				listener.recieveSelection(position);
				dismiss();
			}
		});
		List<HashMap<String, Object>> sources=new ArrayList<HashMap<String, Object>>();

		String[] texts=context.getResources().getStringArray(R.array.sources);
		int[] icons= { R.drawable.contacts, R.drawable.inbox_sms, R.drawable.call, R.drawable.suspicious, R.drawable.top, R.drawable.enter_value, R.drawable.enter_value};

		for(int i=0;i<texts.length;i++){
			if(exclude.contains(i))
				continue;
			HashMap<String, Object> hm=new HashMap<String, Object>();
			hm.put(TEXT_KEY, texts[i]);
			hm.put(ICON_KEY, icons[i]);
			sources.add(hm);
		}
		SimpleAdapter adapter=new SimpleAdapter(context, sources, R.layout.item_source, new String[] { TEXT_KEY, ICON_KEY }, new int[] { R.id.item_text, R.id.item_icon });
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setContentView(v);
	}
	
	public SelectSourceDialog(final Context context, final SelectListener<Integer> listener, final List<Integer> exclude){
		this(context, null, listener, exclude);
	}
}
