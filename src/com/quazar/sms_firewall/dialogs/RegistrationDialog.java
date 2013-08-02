package com.quazar.sms_firewall.dialogs;

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

public class RegistrationDialog extends AlertDialog {
	private CheckBox useSync, useEmail, sendSuspicious;
	private EditText userEmail, logsPassword;
	public RegistrationDialog(final Context context) {
		super(context);
		final View v = getLayoutInflater().inflate(R.layout.registration_popup, null);
		setView(v);		
		useSync=(CheckBox) v.findViewById(R.id.syncFilters);
		useEmail=(CheckBox) v.findViewById(R.id.useEmail);		
		sendSuspicious=(CheckBox) v.findViewById(R.id.sendSuspicious);
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
				boolean hasError=false;
				if(useSync.isChecked()){
					Param.USE_SYNC.setValue(true);
					if(useEmail.isChecked()){
						String email=userEmail.getText().toString();
						if(email.matches("\\S*@\\w*\\.\\w{2,6}"))
							Param.USER_EMAIL.setValue(userEmail.getText().toString());
						else{							
							userEmail.setError(context.getResources().getString(R.string.enter_email));
							hasError=true;
						}
					}
					if(logsPassword.getText().length()>=8){
						if(!hasError)
							Param.LOGS_PASSWORD.setValue(logsPassword.getText().toString());
					}else{						
						logsPassword.setError(context.getResources().getString(R.string.password_length_error));
						hasError=true;
					}
					if(!hasError){
						Param.SEND_SUSPICIOUS.setValue(sendSuspicious.isChecked());
						Toast.makeText(context, context.getResources().getString(R.string.registration_thanks), Toast.LENGTH_SHORT).show();
						Param.IS_NEW.setValue(false);
						RegistrationDialog.this.dismiss();
					}else{
						Toast.makeText(context, context.getResources().getString(R.string.have_errors), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}
}
