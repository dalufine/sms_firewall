package com.quazar.sms_firewall.dialogs;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.quazar.sms_firewall.Param;
import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.ResponseCodes;
import com.quazar.sms_firewall.network.ApiService;

public class RegistrationDialog extends Dialog{
	private CheckBox useSync, useEmail, sendSuspicious;
	private EditText userEmail, logsPassword;

	public RegistrationDialog(final Context context){
		super(context, R.style.Dialog);
		final View v=getLayoutInflater().inflate(R.layout.dialog_registration, null);
		setContentView(v);
		useSync=(CheckBox)v.findViewById(R.id.syncFilters);
		useEmail=(CheckBox)v.findViewById(R.id.useEmail);
		sendSuspicious=(CheckBox)v.findViewById(R.id.sendSuspicious);
		userEmail=(EditText)v.findViewById(R.id.userEmail);
		logsPassword=(EditText)v.findViewById(R.id.logsPassword);
		useSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				useEmail.setEnabled(isChecked);
			}
		});
		useEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				userEmail.setEnabled(isChecked);
			}
		});
		((Button)v.findViewById(R.id.saveReg)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				boolean hasError=false;
				String email=null, password=logsPassword.getText().toString().trim();
				if(useSync.isChecked()){
					Param.USE_SYNC.setValue(true);
					if(useEmail.isChecked()){
						email=userEmail.getText().toString().trim();
						if(email.matches("\\S*@\\w*\\.\\w{2,6}"))
							Param.USER_EMAIL.setValue(email);
						else{
							userEmail.setError(context.getResources().getString(R.string.enter_email));
							hasError=true;
						}
					}
				}
				if(password.length()>=8){
					if(!hasError)
						Param.LOGS_PASSWORD.setValue(password);
				}else{
					logsPassword.setError(context.getResources().getString(R.string.password_length_error));
					hasError=true;
				}
				if(!hasError){
					Toast.makeText(context, context.getResources().getString(R.string.registration_thanks), Toast.LENGTH_SHORT).show();
					Param.SEND_SUSPICIOUS.setValue(sendSuspicious.isChecked());
					Param.IS_NEW.setValue(false);										
					final ApiService api=new ApiService(context);
					try{
						api.register(new Handler(){
							@Override
							public void handleMessage(Message msg){
								JSONObject data=(JSONObject)msg.obj;
								if(data.has("code")){
									try{
										int errorCode=data.getInt("code");
										if(errorCode==ResponseCodes.USER_ALREADY_REGISTERED.getCode()){
											api.loadUserFilters();											
										}
										api.loadTops(null);
									}catch(Exception ex){
										Log.e("json error", ex.toString());
									}
								}
							}
						});
					}catch(Exception e){						
						Log.e("registration", e.toString());
						e.printStackTrace();
					}
					RegistrationDialog.this.dismiss();
				}else{
					Toast.makeText(context, context.getResources().getString(R.string.have_errors), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
