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

	private Spinner categorySpinner;

	public ComplainDialog(final Context context, final String value, final String example, final FilterType filterType,
			final DialogListener<Object> listener){
		super(context, R.style.Dialog);
		setContentView(R.layout.dialog_complain);
		categorySpinner = (Spinner) findViewById(R.id.filter_category);
		TextView messageExample=((TextView) findViewById(R.id.message_example));
		if (filterType == FilterType.WORD) {
			categorySpinner.setVisibility(View.GONE);
			messageExample.setVisibility(View.GONE);
			((TextView) findViewById(R.id.message_example_header)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.filter_category_header)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.type_header)).setText(R.string.word_with_dots);
		}
		((TextView) findViewById(R.id.phone_number)).setText(value);
		messageExample.setText(example);
		((TextView) findViewById(R.id.filter_type))
				.setText(context.getResources().getStringArray(R.array.filter_types)[filterType.ordinal()]);
		((Button) findViewById(R.id.send_btn)).setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v){
				try {
					int position = categorySpinner.getSelectedItemPosition();
					new ApiService(context).addFilter(filterType, TopCategory.values()[position], value, example,
							new Handler(){

								@Override
								public void handleMessage(Message msg){
									try {
										listener.ok(msg.obj);
									}
									catch(Exception ex) {
										Log.e("add_filter", ex.toString());
										ex.printStackTrace();
									}
								}
							});
				}
				catch(Exception e) {
					Log.e("complain", e.toString());
					e.printStackTrace();
				}
				dismiss();
			}
		});
		((Button) findViewById(R.id.close_btn)).setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v){
				listener.cancel();
				dismiss();
			}
		});
	}
}
