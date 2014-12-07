package com.quazar.sms_firewall.dialogs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import com.quazar.sms_firewall.utils.LogUtil;

public class LogsFilterDialog extends Dialog{
	private DialogListener<LogFilter> listener;	
	private LogFilter filter=new LogFilter();

	public LogsFilterDialog(final Context context, DialogListener<LogFilter> listener) throws Exception{
		super(context, R.style.Dialog);
		this.listener=listener;		
		setContentView(R.layout.dialog_logs_filter);
		final Spinner senderSelect=(Spinner)findViewById(R.id.sender);
		DataDao dao=new DataDao(context);
		List<String> senders=null;
		try{
			senders=dao.getLogSenders();
		}finally{
			if(dao!=null){
				dao.close();
			}
		}
		senders.add(0, "");
		senderSelect.setAdapter(new ArrayAdapter<String>(context, R.layout.item_spinner, R.id.spinner_list_value, senders));
		final EditText dateFromField=(EditText)findViewById(R.id.dateFrom), dateToField=(EditText)findViewById(R.id.dateTo), bodyField=((EditText)findViewById(R.id.bodyContains));
		String localSdf=((SimpleDateFormat)SimpleDateFormat.getDateInstance()).toLocalizedPattern();
		dateFromField.setHint(localSdf);
		dateToField.setHint(localSdf);
		dateFromField.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				if(v.hasFocus()){
					setDateToEditText(context, dateFromField);
				}
			}
		});
		dateFromField.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus){
				if(hasFocus){
					setDateToEditText(context, dateFromField);
				}
			}
		});
		dateToField.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				if(v.hasFocus()){
					setDateToEditText(context, dateToField);
				}
			}
		});
		dateToField.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus){
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
		((Button)findViewById(R.id.okLogFilterBtn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				String phoneName=senderSelect.getSelectedItem().toString().trim(), dateFrom=dateFromField.getText().toString().trim(), dateTo=dateToField.getText().toString().trim(), bodyLike=
						bodyField.getText().toString().trim();
				LogsFilterDialog.this.filter.setBodyLike(bodyLike.length()==0?null:bodyLike);
				LogsFilterDialog.this.filter.setPhoneName(phoneName.length()==0?null:phoneName);
				LogsFilterDialog.this.filter.setFrom(dateFrom.length()==0?null:dateFrom);
				LogsFilterDialog.this.filter.setTo(dateTo.length()==0?null:dateTo);
				LogsFilterDialog.this.listener.ok(LogsFilterDialog.this.filter);
				dismiss();
			}
		});
		((Button)findViewById(R.id.resetLogFilterBtn)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				resetFilter();
				senderSelect.setSelection(0);
				dateFromField.setText(null);
				dateToField.setText(null);
				bodyField.setText(null);
			}
		});
		((Button)findViewById(R.id.cancelLogFilterBtn)).setOnClickListener(new Button.OnClickListener(){
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
				LogUtil.error(context, "setDateToEditText", ex);
			}
		}
		DatePickerDialog dpd=new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener(){
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				edit.setText(SimpleDateFormat.getDateInstance().format(cal.getTime()));
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		dpd.show();
	}

	public LogFilter getFilter(){
		return filter;
	}
	public void resetFilter(){
		filter=new LogFilter();
	}
}
