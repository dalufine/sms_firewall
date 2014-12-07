package com.quazar.sms_firewall.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.network.ApiService;

public class ErrorActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTheme(R.style.ErrorDialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_error);
	}

	public void onSendReport(View view){
		final ApiService api = new ApiService(this);
		new Thread(){

			@Override
			public void run(){
				String description = ((TextView) findViewById(R.id.error_description)).getText().toString();
				String log = extractLogToZipString();
				try {
					api.registerBug(description, log);
				}
				catch(Exception ex) {
					Log.e("onSendReport", ex.toString());
				}
			}
		}.start();
		finish();
	}

	public void onClose(View view){
		finish();
	}

	private String extractLogToZipString(){
		String result = null;
		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
		}
		catch(NameNotFoundException e2) {}
		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER)) {
			model = Build.MANUFACTURER + " " + model;
		}
		InputStream inputStream = null;
		GZIPOutputStream zipStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			String cmd = "logcat -d -v time System.err:W *:S";
			Process process = Runtime.getRuntime().exec(cmd);
			inputStream = process.getInputStream();
			outputStream = new ByteArrayOutputStream();
			zipStream = new GZIPOutputStream(outputStream);
			StringBuilder sb = new StringBuilder();
			sb.append("Android version: " + Build.VERSION.SDK_INT + "; ");
			sb.append("Device: " + model + "; ");
			sb.append("App version: " + (info == null?"(null)":info.versionCode) + "; ");
			zipStream.write(sb.toString().getBytes("ASCII"));
			byte[] buffer = new byte[10000];
			do {
				int n = inputStream.read(buffer, 0, buffer.length);
				if (n == -1)
					break;
				zipStream.write(buffer, 0, n);
			} while(true);
			zipStream.finish();
			zipStream.flush();
			result = new String(Base64.encode(outputStream.toByteArray(), Base64.DEFAULT));
		}
		catch(IOException e) {
			Log.e("", e.toString());
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch(IOException e1) {}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				}
				catch(IOException e1) {}
			}
			if (zipStream != null) {
				try {
					zipStream.close();
				}
				catch(IOException e1) {}
			}
		}
		return result;
	}
}
