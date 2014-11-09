package com.quazar.sms_firewall.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.models.TopFilter.TopCategory;
import com.quazar.sms_firewall.models.UserFilter.FilterType;
import com.quazar.sms_firewall.network.ApiService;

public class ComplainDialog extends Dialog{

	public ComplainDialog(final Context context, final String value, final String example, final FilterType filterType, final DialogListener<Object> listener){
		super(context, R.style.Dialog);
		final View content=getLayoutInflater().inflate(R.layout.dialog_complain, null);
		setContentView(content);
		((TextView)findViewById(R.id.phone_number)).setText(value);
		((TextView)content.findViewById(R.id.message_example)).setText(example);
		((TextView)content.findViewById(R.id.filter_type)).setText(context.getResources().getStringArray(R.array.filter_types)[filterType.ordinal()]);
		((Button)content.findViewById(R.id.send_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				try{
					int position=((Spinner)content.findViewById(R.id.filter_category)).getSelectedItemPosition();
					new ApiService(context).addFilter(filterType, TopCategory.values()[position], value, example, new Handler(){
						@Override
						public void handleMessage(Message msg){
							try{								
								listener.ok(msg.obj);								
							}catch(Exception ex){
								Log.e("add_filter", ex.toString());
								ex.printStackTrace();
							}
						}
					});
				}catch(Exception e){
					Log.e("complain", e.toString());
					e.printStackTrace();
				}
				dismiss();
			}
		});
		((Button)content.findViewById(R.id.close_btn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				listener.cancel();
				dismiss();
			}
		});
	}
}
