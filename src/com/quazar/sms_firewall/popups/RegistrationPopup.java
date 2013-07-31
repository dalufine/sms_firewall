package com.quazar.sms_firewall.popups;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.R;

public class RegistrationPopup extends AlertDialog {
	private CheckBox useSync, useEmail;
	private EditText userEmail, logsPassword;
	public RegistrationPopup(final Context context) {
		super(context);
		final View v = getLayoutInflater().inflate(R.layout.registration_popup, null);
		setView(v);
		useSync=(CheckBox) v.findViewById(R.id.syncFilters);
		useEmail=(CheckBox) v.findViewById(R.id.useEmail);		
		userEmail=(EditText) v.findViewById(R.id.userEmail);
		logsPassword=(EditText) v.findViewById(R.id.logsPassword);
		useSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				useEmail.setEnabled(isChecked);
			}
		});
		useEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				userEmail.setEnabled(isChecked);
			}
		});		
		((Button)v.findViewById(R.id.saveReg)).setOnClickListener(new Button.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(useSync.isChecked()){
					Param.USE_SYNC.setValue(true);
					if(useEmail.isChecked()){
						if(userEmail.getText().length()>3)
							Param.USER_EMAIL.setValue(userEmail.getText().toString());
						else{ 
							Toast.makeText(context, context.getResources().getString(R.string.enter_email), Toast.LENGTH_LONG).show();
							return;
						}
					}
					if(logsPassword.getText().length()>=8){
						Param.LOGS_PASSWORD.setValue(logsPassword.getText().toString());
					}else{
						Toast.makeText(context, context.getResources().getString(R.string.password_length_error), Toast.LENGTH_LONG).show();
						return;
					}
					Param.SEND_SUSPICIOUS.setValue(((CheckBox)v.findViewById(R.id.sendSuspicious)).isChecked());
				}
			}
		});
	}
}
