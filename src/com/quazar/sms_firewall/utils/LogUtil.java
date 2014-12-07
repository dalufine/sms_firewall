package com.quazar.sms_firewall.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.util.Log;

import com.quazar.sms_firewall.network.ApiService;

public class LogUtil {
	public static void error(Context context, String comment, Exception ex) {
		String log = toString(ex);
		Log.e("error", log);		
		try {
			ApiService api = new ApiService(context);
			api.registerBug(comment, log);
		} catch (Exception e) {
		}
	}

	public static void info(String comment) {
		Log.e("info", comment);
	}

	public static void debug(String comment) {
		Log.e("debug", comment);
	}

	public static String toString(Exception ex) {
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			return sw.toString();
		} finally {
			try {
				sw.close();
			} catch (Exception e) {
			}
			pw.close();
		}
	}
}
