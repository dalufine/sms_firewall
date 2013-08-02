package com.quazar.sms_firewall.dialogs;

import java.util.Arrays;

import com.quazar.sms_firewall.dialogs.listeners.SelectListener;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MenuDialog extends AlertDialog{
	public MenuDialog(final Context context, String[] items, final SelectListener<Integer> listener){
		super(context);
		ListView listView=new ListView(context);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				listener.recieveSelection(position);
				dismiss();
			}
		});
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, Arrays.asList(items));
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setView(listView);
	}
}
