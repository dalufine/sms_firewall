package com.quazar.sms_firewall.models;

public class FilterModel {
	public enum FilterType {
		PHONE_NAME, WORD, REGEXP
	};

	private int id;
	private String value;
	private FilterType type;

	public FilterModel(int id, String value, int type) {
		super();
		this.id = id;
		this.value = value;
		for (FilterType ft : FilterType.values()) {
			if (ft.ordinal() == type)
				this.type = ft;
		}
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
