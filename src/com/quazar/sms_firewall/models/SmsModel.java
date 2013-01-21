package com.quazar.sms_firewall.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsModel {
	private int id;
	private String phone, body;
	private boolean seen;
	private Date date;
	public SmsModel(int id, String phone, String body, boolean seen, Date date) {
		super();
		this.id = id;
		this.phone = phone;		
		this.body = body;
		this.seen = seen;
		this.date = date;
	}
	public int getId() {
		return id;
	}
	public String getPhone() {
		return phone;
	}
	public String getBody() {
		return body;
	}
	public boolean isSeen() {
		return seen;
	}
	public Date getDate() {
		return date;
	}
	@Override
	public String toString() {		
		SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
		return String.format("id:%d date:%s phone:%s body:%s seen:%b;\r\n", id, sdf.format(date), phone, body, seen);
	}
}
