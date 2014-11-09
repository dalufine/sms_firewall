package com.quazar.sms_firewall.dialogs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.network.ApiService;
import com.quazar.sms_firewall.utils.DeviceInfoUtil;

public class ConnectionDialog extends Dialog{
	public interface ConnectionListener{
		void onConnectionReady();
	}

	public ConnectionDialog(final Activity activity, final ConnectionListener listener){
		super(activity, R.style.Dialog);
		View v=getLayoutInflater().inflate(R.layout.dialog_connection, null);
		setContentView(v);
		((Button)v.findViewById(R.id.closeConnPopup)).setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				dismiss();
			}
		});
		ToggleButton mobBtn=((ToggleButton)v.findViewById(R.id.MobNetBtn));
		mobBtn.setChecked(DeviceInfoUtil.isOnline(activity));
		mobBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				setMobileDataEnabled(activity, !DeviceInfoUtil.isOnline(activity));
				if(listener!=null)
					listener.onConnectionReady();
				dismiss();
			}
		});
		final WifiManager wifiManager=(WifiManager)activity.getSystemService(Context.WIFI_SERVICE);
		ToggleButton wifiBtn=((ToggleButton)v.findViewById(R.id.wifiBtn));
		wifiBtn.setChecked(wifiManager.isWifiEnabled());
		wifiBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
				new ApiService(activity).ping(10000, new Handler(){
					@Override
					public void handleMessage(Message msg){
						final Boolean result=(Boolean)msg.obj;
						activity.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								if(result){
									if(listener!=null)
										listener.onConnectionReady();
									dismiss();
								}else{
									Toast.makeText(activity, activity.getResources().getString(R.string.wifi_connect_error), Toast.LENGTH_LONG).show();
								}
							}
						});

					}
				});
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
