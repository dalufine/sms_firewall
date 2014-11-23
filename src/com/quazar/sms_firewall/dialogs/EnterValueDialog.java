package com.quazar.sms_firewall.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.utils.ContentUtils;

public class EnterValueDialog extends Dialog{
	protected DialogListener<String> listener;
	protected EditText valueField;

	public EnterValueDialog(Context context, String title, final int inputType, DialogListener<String> listener){
		super(context, R.style.Dialog);
		this.listener=listener;
		View v=getLayoutInflater().inflate(R.layout.dialog_enter_value, null);
		setContentView(v);
		valueField=(EditText)findViewById(R.id.value_fileld);
		valueField.setInputType(inputType);
		((TextView)v.findViewById(R.id.value_title)).setText(title);
		((Button)v.findViewById(R.id.ok_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				String value=valueField.getText().toString().trim();
				if(isValidValue(value)){
					if(inputType==InputType.TYPE_CLASS_PHONE){
						value=ContentUtils.getFormatedPhoneNumber(value);
					}
					EnterValueDialog.this.listener.ok(value);
					dismiss();
				}
			}
		});
		((Button)v.findViewById(R.id.cancel_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				EnterValueDialog.this.listener.cancel();
				dismiss();
			}
		});
	}
	protected boolean isValidValue(String value){
		if(value==null||value.equals("")){
			valueField.setError(getContext().getResources().getString(R.string.enter_value_warning));
			return false;
		}
		return true;
	}
}
