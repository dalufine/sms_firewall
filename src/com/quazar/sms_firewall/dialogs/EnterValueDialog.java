package com.quazar.sms_firewall.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;

public class EnterValueDialog extends Dialog{
	private SelectListener<String> listener;

	public EnterValueDialog(Context context, String title, boolean phoneNumber, SelectListener<String> listener){
		super(context, R.style.Dialog);
		this.listener=listener;
		View v=getLayoutInflater().inflate(R.layout.enter_value_dialog, null);
		setContentView(v);
		final EditText valueField=(EditText)findViewById(R.id.value_fileld);
		if(phoneNumber)
			valueField.setInputType(InputType.TYPE_CLASS_PHONE);
		((TextView)v.findViewById(R.id.value_title)).setText(title);
		((Button)v.findViewById(R.id.ok_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				String value=valueField.getText().toString().trim();
				if(value==null||value.equals("")){
					Toast.makeText(getContext(), getContext().getResources().getString(R.string.enter_value_warning), Toast.LENGTH_SHORT).show();
					return;
				}
				EnterValueDialog.this.listener.recieveSelection(value);
				dismiss();
			}
		});
		((Button)v.findViewById(R.id.cancel_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				dismiss();
			}
		});
	}
}
