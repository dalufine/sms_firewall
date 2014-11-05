package com.quazar.sms_firewall.dialogs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class ConnectionDialog extends Dialog{
	public interface ConnectionListener{
		void onConnectionReady();
	}

	public ConnectionDialog(final Context context, final ConnectionListener listener){
		super(context, R.style.Dialog);
		View v=getLayoutInflater().inflate(R.layout.connection_dialog, null);
		setContentView(v);
		((Button)v.findViewById(R.id.closeConnPopup)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				dismiss();
			}
		});
		ToggleButton mobBtn=((ToggleButton)v.findViewById(R.id.MobNetBtn));
		mobBtn.setChecked(DeviceInfoUtil.isOnline(context));
		mobBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				setMobileDataEnabled(context, !DeviceInfoUtil.isOnline(context));
				if(listener!=null)
					listener.onConnectionReady();
				dismiss();
			}
		});
		final WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		ToggleButton wifiBtn=((ToggleButton)v.findViewById(R.id.wifiBtn));
		wifiBtn.setChecked(wifiManager.isWifiEnabled());
		wifiBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
				if(listener!=null)
					listener.onConnectionReady();
				dismiss();
			}
		});
	}

	private void setMobileDataEnabled(Context context, boolean enabled){
		try{
			final ConnectivityManager conman=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class conmanClass=Class.forName(conman.getClass().getName());
			final Field iConnectivityManagerField=conmanClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager=iConnectivityManagerField.get(conman);
			final Class iConnectivityManagerClass=Class.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod=iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);
			setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		}catch(Exception ex){
			Log.i("network", ex.toString());
		}
	}
}
