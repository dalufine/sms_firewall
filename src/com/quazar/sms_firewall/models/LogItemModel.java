package com.quazar.sms_firewall.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogItemModel {
	public enum LogStatus {
		RECIEVED, SUSPICIOUS, BLOCKED
	};

	private int id;
	private String phoneName, body;
	private Date date;
	private LogStatus status;

	public LogItemModel(int id, String phoneName, String body, Date date, int status) {
		super();
		this.id = id;
		this.phoneName = phoneName;
		this.body = body;
		this.date = date;
		for (LogStatus ls : LogStatus.values()) {
			if (ls.ordinal() == status)
				this.status = ls;
		}
	}

	public int getId() {
		return id;
	}

	public String getPhoneName() {
		return phoneName;
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

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss",
				Locale.getDefault());
		return String.format("id:%d date:%s phone:%s body:%s;\r\n", id,
				sdf.format(date), phoneName, body);
	}
}
