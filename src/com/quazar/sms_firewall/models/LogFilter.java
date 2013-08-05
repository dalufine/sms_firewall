package com.quazar.sms_firewall.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFilter {
	private String phoneName, bodyLike;
	private Date from, to;
	private static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
	
	public LogFilter(String phoneName, String bodyLike, String from, String to) {
		super();
		if(phoneName!=null&&!phoneName.isEmpty())
			this.phoneName = phoneName;
		if(bodyLike!=null&&!bodyLike.isEmpty())
			this.bodyLike = bodyLike;
		try{
			if(from!=null&&!from.isEmpty())
				this.from = sdf.parse(from);
			if(to!=null&&!to.isEmpty())
				this.to = sdf.parse(to);
		}catch(Exception ex){			
		}
	}
	public String getPhoneName() {
		return phoneName;
	}
	public void setPhoneName(String phoneName) {
		this.phoneName = phoneName;
	}
	public String getBodyLike() {
		return bodyLike;
	}
	public void setBodyLike(String bodyLike) {
		this.bodyLike = bodyLike;
	}
	public Date getFrom() {
		return from;
	}
	public void setFrom(Date from) {
		this.from = from;
	}
	public Date getTo() {
		return to;
	}
	public void setTo(Date to) {
		this.to = to;
	}	
}
