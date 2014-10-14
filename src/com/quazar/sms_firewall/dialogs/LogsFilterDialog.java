package com.quazar.sms_firewall.dialogs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
	private LogFilter filter=new LogFilter();
	public LogsFilterDialog(final Context context, DialogListener<LogFilter> listener){
		super(context);
		this.listener=listener;		
		view=getLayoutInflater().inflate(R.layout.logs_filter_dialog, null);		
		setView(view);	
		final Spinner senderSelect=(Spinner)view.findViewById(R.id.sender);
		List<String> senders=new DataDao(context).getLogSenders();
		senders.add(0, "");
		senderSelect.setAdapter(new ArrayAdapter<String>(context, R.layout.spinner_list_item, R.id.spinner_list_value, senders));
		final EditText dateFromField=(EditText)view.findViewById(R.id.dateFrom), 
				dateToField=(EditText)view.findViewById(R.id.dateTo), bodyField=((EditText)view.findViewById(R.id.bodyContains));
		String localSdf=((SimpleDateFormat)SimpleDateFormat.getDateInstance()).toLocalizedPattern();
		dateFromField.setHint(localSdf);
		dateToField.setHint(localSdf);
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
		if(filter.getBodyLike()!=null){
			bodyField.setText(filter.getBodyLike());
		}
		if(filter.getPhoneName()!=null){
			senderSelect.setSelection(senders.indexOf(filter.getPhoneName()));
		}
		if(filter.getFrom()!=null){			
			dateFromField.setText(SimpleDateFormat.getDateInstance().format(filter.getFrom()));
		}
		if(filter.getTo()!=null){			
			dateToField.setText(SimpleDateFormat.getDateInstance().format(filter.getTo()));
		}
		((Button)view.findViewById(R.id.okLogFilterBtn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){				
				String phoneName=senderSelect.getSelectedItem().toString().trim(),
					   dateFrom=dateFromField.getText().toString().trim(),					   
					   dateTo=dateToField.getText().toString().trim(),
					   bodyLike=bodyField.getText().toString().trim();
					   LogsFilterDialog.this.filter.setBodyLike(bodyLike.length()==0?null:bodyLike);
					   LogsFilterDialog.this.filter.setPhoneName(phoneName.length()==0?null:phoneName);
					   LogsFilterDialog.this.filter.setFrom(dateFrom.length()==0?null:dateFrom);
					   LogsFilterDialog.this.filter.setTo(dateTo.length()==0?null:dateTo);
					   LogsFilterDialog.this.listener.ok(LogsFilterDialog.this.filter);
					   dismiss();
			}
		});
		((Button)view.findViewById(R.id.resetLogFilterBtn)).setOnClickListener(new Button.OnClickListener(){			
			@Override
			public void onClick(View v){
				resetFilter();
				senderSelect.setSelection(0);
				dateFromField.setText(null);					   
				dateToField.setText(null);
				bodyField.setText(null);
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
		String val=edit.getText().toString().trim();
		if(val.length()>0){
			try{								
				cal.setTime(SimpleDateFormat.getDateInstance().parse(val));								
			}catch(Exception ex){				
			}
		}		
		DatePickerDialog dpd=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {					
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				edit.setText(SimpleDateFormat.getDateInstance().format(cal.getTime()));
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		dpd.show();
	}
	
	public LogFilter getFilter() {
		return filter;
	}
	public void resetFilter(){
		filter=new LogFilter();
	}
}
