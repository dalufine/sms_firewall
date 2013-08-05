package com.quazar.sms_firewall.dialogs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class ConnectionDialog extends AlertDialog {
	public ConnectionDialog(final Context context) {
		super(context);
		View v = getLayoutInflater().inflate(R.layout.connection_dialog, null);
		setView(v);
		((TextView) v.findViewById(R.id.con_value_title)).setText(context
				.getResources().getString(R.string.inet_conn_on));
		((Button) v.findViewById(R.id.closeConnPopup))
				.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
		ToggleButton mobBtn = ((ToggleButton) v.findViewById(R.id.MobNetBtn));
		mobBtn.setChecked(DeviceInfoUtil.isOnline(context));
		mobBtn.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						setMobileDataEnabled(context, !DeviceInfoUtil.isOnline(context));
						//dismiss();
					}
				});
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		ToggleButton wifiBtn = ((ToggleButton) v.findViewById(R.id.wifiBtn));
		wifiBtn.setChecked(wifiManager.isWifiEnabled());
		wifiBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
				// dismiss();
			}
		});
	}

	private void setMobileDataEnabled(Context context, boolean enabled) {
		try {
			final ConnectivityManager conman = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class conmanClass = Class
					.forName(conman.getClass().getName());
			final Field iConnectivityManagerField = conmanClass
					.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField
					.get(conman);
			final Class iConnectivityManagerClass = Class
					.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = iConnectivityManagerClass
					.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);
			setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		} catch (Exception ex) {
			Log.i("network", ex.toString());
		}
	}
}
