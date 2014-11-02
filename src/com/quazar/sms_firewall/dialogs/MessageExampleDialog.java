package com.quazar.sms_firewall.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.models.TopFilter;
import com.quazar.sms_firewall.models.TopFilter.TopType;

public class MessageExampleDialog extends AlertDialog{	
	public MessageExampleDialog(final Context context, TopFilter item, List<String> examples){
		super(context);		
		View v=getLayoutInflater().inflate(R.layout.message_example_dialog, null);	
		setView(v);
		if(item.getType()==TopType.WORD){
			((TextView)v.findViewById(R.id.header_ex)).setText(R.string.word_with_dots);
		}
		((TextView)v.findViewById(R.id.phone_number_ex)).setText(item.getValue());
		((TextView)v.findViewById(R.id.message_votes)).setText(""+item.getVotes());
		((TextView)v.findViewById(R.id.message_position)).setText(""+item.getPos());
		ListView messagesLisn=(ListView)v.findViewById(R.id.messages_ex);
		messagesLisn.setAdapter(new ArrayAdapter<String>(context, R.layout.examples_list_item, R.id.example_message, examples));
		((Button)v.findViewById(R.id.close_btn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){				
				dismiss();				
			}
		});
	}
}