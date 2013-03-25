package com.quazar.sms_firewall.popups;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.quazar.sms_firewall.R;

public class PasswordPopup extends AlertDialog{
	private DialogListener<String> listener;
	public PasswordPopup(Context context, String title, DialogListener<String> listener){
		super(context);
		this.listener=listener;
		View v=getLayoutInflater().inflate(R.layout.enter_password_popup, null);		
		setView(v);
		((TextView)v.findViewById(R.id.password_title)).setText(title);
		final EditText password=((EditText)v.findViewById(R.id.password_fileld));		
		((Button)v.findViewById(R.id.password_ok_btn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){
				String value=password.getText().toString();
				if(value==null||value.equals("")){
					Toast.makeText(getContext(), getContext().getResources().getString(R.string.enter_value_warning), Toast.LENGTH_SHORT).show();
					return;
				}
				PasswordPopup.this.listener.ok(value);
				dismiss();				
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
