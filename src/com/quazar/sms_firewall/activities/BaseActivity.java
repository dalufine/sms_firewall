package com.quazar.sms_firewall.activities;
import com.quazar.sms_firewall.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class BaseActivity extends Activity {
	public void onGoBack(View v){
		this.onBackPressed();
	}
	protected View createTabView(Context context, String text, int drawableId) {
		View view = LayoutInflater.from(context).inflate(R.layout.layout_tab,
				null);
		TextView tv = (TextView) view.findViewById(R.id.tab_text);
		tv.setText(text);
		ImageView im = (ImageView) view.findViewById(R.id.tab_img);		
		im.setImageDrawable(getResources().getDrawable(drawableId));
		return view;

	}
}
