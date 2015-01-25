package com.quazar.sms_firewall.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quazar.sms_firewall.R;

public class MessageViewDialog extends Dialog{

	public MessageViewDialog(final Activity activity, String number, String text, String date){
		super(activity, R.style.Dialog);
		setOwnerActivity(activity);
		setContentView(R.layout.dialog_message_view);		
		((TextView)findViewById(R.id.phone_number)).setText(number);
		((TextView)findViewById(R.id.date)).setText(date);
		((TextView)findViewById(R.id.message)).setText(text);				
		((Button)findViewById(R.id.close_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				dismiss();
			}
		});
	}
}