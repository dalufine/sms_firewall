package com.quazar.sms_firewall.dialogs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.models.LogFilter;

public class LogsFilterDialog extends AlertDialog{
	private DialogListener<LogFilter> listener;
	private View view;
	public LogsFilterDialog(final Context context, DialogListener<LogFilter> listener){
		super(context);
		this.listener=listener;
		view=getLayoutInflater().inflate(R.layout.logs_filter_dialog, null);		
		setView(view);	
		final Spinner senderSelect=(Spinner)view.findViewById(R.id.sender);
		List<String> senders=new DataDao(context).getLogSenders();
		senders.add(0, "");
		senderSelect.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, senders));
		final EditText dateFromField=(EditText)view.findViewById(R.id.dateFrom), 
				dateToField=(EditText)view.findViewById(R.id.dateTo);
		dateFromField.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(v.hasFocus()){
					setDateToEditText(context, dateFromField);
				}
			}
		});
		dateFromField.setOnFocusChangeListener(new View.OnFocusChangeListener() {			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					setDateToEditText(context, dateFromField);
				}				
			}
		});
		dateToField.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(v.hasFocus()){
					setDateToEditText(context, dateToField);
				}
			}
		});
		dateToField.setOnFocusChangeListener(new View.OnFocusChangeListener() {			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					setDateToEditText(context, dateToField);
				}				
			}
		});
		((Button)view.findViewById(R.id.okLogFilterBtn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){				
				String phoneName=senderSelect.getSelectedItem().toString(),
					   dateFrom=dateFromField.getText().toString(),
					   dateTo=dateToField.getText().toString(),
					   bodyLike=((EditText)view.findViewById(R.id.bodyContains)).getText().toString().trim();
					   LogsFilterDialog.this.listener.ok(new LogFilter(phoneName, bodyLike, dateFrom, dateTo));
					   dismiss();
			}
		});
		((Button)view.findViewById(R.id.cancelLogFilterBtn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){
				cancel();
				dismiss();				
			}
		});		
	}
	public void setDateToEditText(Context context, final EditText edit){
		final Calendar cal=Calendar.getInstance();
		final SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		String val=edit.getText().toString().trim();
		if(!val.isEmpty()){
			try{								
				cal.setTime(sdf.parse(val));								
			}catch(Exception ex){				
			}
		}		
		DatePickerDialog dpd=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {					
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				edit.setText(sdf.format(cal.getTime()));
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		dpd.show();
	}
}
