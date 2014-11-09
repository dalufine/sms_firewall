package com.quazar.sms_firewall.models;

import java.util.Date;

public class SmsLogItem {
	public enum LogStatus {
		FILTERED, SUSPICIOUS, BLOCKED
	};

	private long id;
	private String name;
	private String number;
	private String body;
	private Date date;
	private LogStatus status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setStatus(int status) {
		this.status = LogStatus.values()[status];
	}

	public long getId() {
		return id;
	}

	public String getBody() {
		return body;
	}

	public Date getDate() {
		return date;
	}

	public LogStatus getStatus() {
		return status;
	}
}
