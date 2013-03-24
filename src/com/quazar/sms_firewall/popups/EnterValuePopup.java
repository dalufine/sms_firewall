package com.quazar.sms_firewall.popups;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.quazar.sms_firewall.R;

public class EnterValuePopup extends AlertDialog{
	private SelectListener<String> listener;
	public EnterValuePopup(Context context, String title, SelectListener<String> listener){
		super(context);
		this.listener=listener;
		View v=getLayoutInflater().inflate(R.layout.enter_value_popup, null);		
		setView(v);
		((TextView)v.findViewById(R.id.value_title)).setText(title);
		((Button)v.findViewById(R.id.ok_btn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){
				String value=((EditText)findViewById(R.id.value_fileld)).getText().toString();
				if(value==null||value.equals("")){
					Toast.makeText(getContext(), getContext().getResources().getString(R.string.enter_value_warning), Toast.LENGTH_SHORT).show();
					return;
				}
				EnterValuePopup.this.listener.recieveSelection(value);
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
