package com.quazar.sms_firewall.utils;

import java.security.MessageDigest;

import android.util.Base64;


public class Utils{
	public static String md5AsBase64String(String input) throws Exception{
		byte[] md5 = MessageDigest.getInstance("MD5").digest(input.getBytes("UTF-8"));
		md5 = Base64.encode(md5, Base64.NO_WRAP);
		return new String(md5);
	}
}
