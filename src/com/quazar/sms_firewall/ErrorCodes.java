package com.quazar.sms_firewall;

import android.content.Context;

public enum ErrorCodes {
	USER_ALREADY_REGISTERED(0), FILTER_NOT_FOUND(1), MAX_VOTES_PER_DAY(2);
	private String description;
	private int code;

	public static void init(Context context) {
		getErrorByCode(2).description = context
				.getString(R.string.votes_per_day_error);
	}

	public static ErrorCodes getErrorByCode(int code) {
		for (ErrorCodes c : ErrorCodes.values()) {
			if (c.code == code)
				return c;
		}
		return null;
	}

	private ErrorCodes(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

}
