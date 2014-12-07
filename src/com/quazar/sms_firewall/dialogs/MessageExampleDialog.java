package com.quazar.sms_firewall.dialogs;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.models.TopFilter;
import com.quazar.sms_firewall.models.TopFilter.TopType;

public class MessageExampleDialog extends Dialog{
	public static final int ADD_EXAMPLE=1;
	public static final int TO_MY_FILTERS=2;

	public MessageExampleDialog(final Activity activity, final TopFilter item, List<String> examples, final Handler handler){
		super(activity, R.style.Dialog);
		setOwnerActivity(activity);
		setContentView(R.layout.dialog_message_example);
		if(item.getType()==TopType.WORD){
			((TextView)findViewById(R.id.header_ex)).setText(R.string.word_with_dots);
		}
		((TextView)findViewById(R.id.phone_number_ex)).setText(item.getValue());
		((TextView)findViewById(R.id.message_votes)).setText(""+item.getVotes());
		((TextView)findViewById(R.id.message_position)).setText(""+item.getPos());
		ListView messagesLisn=(ListView)findViewById(R.id.messages_ex);
		messagesLisn.setAdapter(new ArrayAdapter<String>(activity, R.layout.item_examples, R.id.example_message, examples));
		((Button)findViewById(R.id.close_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				dismiss();
			}
		});
		((Button)findViewById(R.id.add_example_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				Message message=handler.obtainMessage(ADD_EXAMPLE, item);
				handler.dispatchMessage(message);
				dismiss();
			}
		});
		((Button)findViewById(R.id.to_my_filters_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				Message message=handler.obtainMessage(TO_MY_FILTERS, item);
				handler.dispatchMessage(message);
				dismiss();
			}
		});
	}
}