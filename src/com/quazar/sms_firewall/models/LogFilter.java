package com.quazar.sms_firewall.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFilter {
	private String phoneName, bodyLike;
	private Date from, to;
	
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
	public void setFrom(String from) {		
		try{
			if(from!=null&&!from.isEmpty())
				this.from = SimpleDateFormat.getDateInstance().parse(from);			
		}catch(Exception ex){			
		}		
	}
	public Date getTo() {
		return to;
	}
	public void setTo(String to) {		
		try{
			if(to!=null&&!to.isEmpty())
				this.to = SimpleDateFormat.getDateInstance().parse(to);			
		}catch(Exception ex){			
		}
	}	
}
