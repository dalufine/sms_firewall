package com.quazar.sms_firewall.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;

public class PasswordDialog extends AlertDialog{
	private DialogListener<String> listener;
	public PasswordDialog(final Context context, final String etalon, DialogListener<String> listener){
		super(context);
		this.listener=listener;
		View v=getLayoutInflater().inflate(R.layout.enter_password_popup, null);		
		setView(v);
		((TextView)v.findViewById(R.id.password_title)).setText(R.string.ask_logs_password);
		final EditText password=((EditText)v.findViewById(R.id.password_fileld));		
		((Button)v.findViewById(R.id.password_ok_btn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){
				String value=password.getText().toString().trim();
				if(value==null||value.isEmpty()||value.length()<8){
					password.setError(context.getResources().getString(R.string.password_length_error));
					return;
				}
				if(etalon.equals(value)){
					PasswordDialog.this.listener.ok(value);
					dismiss();				
				}else{
					password.setError(context.getResources().getString(R.string.logs_password_error));
				}
			}
		});
		((Button)v.findViewById(R.id.password_cancel_btn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){				
				dismiss();				
			}
		});
	}
}
