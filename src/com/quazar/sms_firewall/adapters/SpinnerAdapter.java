package com.quazar.sms_firewall.adapters;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quazar.sms_firewall.R;

public class SpinnerAdapter extends ArrayAdapter<String>{

	private Activity activity;
	private List<SpinnerModel> items;

	public SpinnerAdapter(Activity activity, List<SpinnerModel> items){
		super(activity, R.layout.item_spinner, activity.getResources().getStringArray(R.array.languages));
		this.activity = activity;
		this.items=items;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		return getCustomView(position, convertView, parent);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = activity.getLayoutInflater();
		View row = inflater.inflate(R.layout.item_spinner_lang, parent, false);
		TextView label = (TextView) row.findViewById(R.id.lang_value);
		label.setText(items.get(position).getText());
		ImageView flag = (ImageView) row.findViewById(R.id.flag);
		flag.setImageResource(items.get(position).getImageId());
		return row;
	}
}