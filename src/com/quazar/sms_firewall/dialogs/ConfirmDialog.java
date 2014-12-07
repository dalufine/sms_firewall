package com.quazar.sms_firewall.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;

public class ConfirmDialog extends Dialog{
	protected DialogListener<Boolean> listener;

	public ConfirmDialog(Context context, String title, String question, DialogListener<Boolean> listener){
		super(context, R.style.Dialog);
		this.listener=listener;		
		setContentView(R.layout.dialog_confirm);
		((TextView)findViewById(R.id.title)).setText(title);
		((TextView)findViewById(R.id.question)).setText(question);
		((Button)findViewById(R.id.ok_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				ConfirmDialog.this.listener.ok(true);
				dismiss();
			}
		});
		((Button)findViewById(R.id.cancel_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				ConfirmDialog.this.listener.cancel();
				dismiss();
			}
		});
	}
}
