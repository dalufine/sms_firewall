package com.quazar.sms_firewall;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

public class SmsFirewallApp extends Application{
	public void onCreate(){
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread thread, Throwable e){
				handleUncaughtException(thread, e);
			}
		});
	}

	public void handleUncaughtException(Thread thread, Throwable e){
		e.printStackTrace();
		//		Intent intent=new Intent();
		//		intent.setAction("com.quazar.sms_firewall.activities.MainActivity");
		//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//		startActivity(intent);
		extractLogToFile();
		System.exit(1);
	}
	private void extractLogToFile(){
		PackageManager manager=this.getPackageManager();
		PackageInfo info=null;
		try{
			info=manager.getPackageInfo(this.getPackageName(), 0);
		}catch(NameNotFoundException e2){}
		String model=Build.MODEL;
		if(!model.startsWith(Build.MANUFACTURER))
			model=Build.MANUFACTURER+" "+model;
		// Extract to file.
		InputStreamReader reader=null;
		FileWriter writer=null;
		try{
			String cmd=(Build.VERSION.SDK_INT<=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)?"logcat -d -v time com.quazar.sms_firewall:v dalvikvm:v System.err:v *:s":"logcat -d -v time";
			Process process=Runtime.getRuntime().exec(cmd);
			reader=new InputStreamReader(process.getInputStream());
			File path=new File(Environment.getExternalStorageDirectory()+"/Sms Firewall/");
			if(!path.exists()){
				path.mkdirs();
			}
			String fullName=path.getAbsolutePath()+"/appCrashLog.txt";
			File file=new File(fullName);
			writer=new FileWriter(file);
			writer.write("Android version: "+Build.VERSION.SDK_INT+"\n");
			writer.write("Device: "+model+"\n");
			writer.write("App version: "+(info==null?"(null)":info.versionCode)+"\n");
			char[] buffer=new char[10000];
			do{
				int n=reader.read(buffer, 0, buffer.length);
				if(n==-1)
					break;
				writer.write(buffer, 0, n);
			}while(true);
			reader.close();
			writer.close();
		}catch(IOException e){
			if(writer!=null)
				try{
					writer.close();
				}catch(IOException e1){}
			if(reader!=null)
				try{
					reader.close();
				}catch(IOException e1){}
		}
	}
}
