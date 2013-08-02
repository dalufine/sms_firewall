package com.quazar.sms_firewall.activities;
import android.app.Activity;
import android.view.View;


public class BaseActivity extends Activity {
	public void onGoBack(View v){
		this.onBackPressed();
	}
}
