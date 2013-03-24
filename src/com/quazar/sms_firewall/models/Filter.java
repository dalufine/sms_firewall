package com.quazar.sms_firewall.models;

public class Filter {
	public enum FilterType {
		PHONE_NAME, WORD, REGEXP
	};

	private int id;
	private String value;
	private FilterType type;

	public Filter(int id, String value, int type) {
		super();
		this.id = id;
		this.value = value;
		this.type=FilterType.values()[type];		
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public FilterType getType() {
		return type;
	}
}
