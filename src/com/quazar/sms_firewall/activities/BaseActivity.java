package com.quazar.sms_firewall.activities;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.HelpDialog;
import com.quazar.sms_firewall.dialogs.HelpDialog.Window;

public class BaseActivity extends Activity{

	protected Window windowType;

	public void onGoBack(View v){
		this.onBackPressed();
	}

	protected View createTabView(Context context, String text, int drawableId){
		View view = LayoutInflater.from(context).inflate(R.layout.layout_tab, null);
		TextView tv = (TextView) view.findViewById(R.id.tab_text);
		tv.setText(text);
		ImageView im = (ImageView) view.findViewById(R.id.tab_img);
		im.setImageDrawable(getResources().getDrawable(drawableId));
		return view;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e){
		if (windowType != null) {
			switch(keycode) {
				case KeyEvent.KEYCODE_MENU:
					HelpDialog dialog = new HelpDialog(this, windowType);
					dialog.show();
					return true;
			}
		}
		return super.onKeyDown(keycode, e);
	}
}
