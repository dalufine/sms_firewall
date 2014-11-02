package com.quazar.sms_firewall;

import android.content.Context;

public enum ResponseCodes {
	OK(0), SYSTEM_ERROR(1), USER_NOT_FOUND(2),USER_ALREADY_REGISTERED(3),
	FILTER_NOT_FOUND(4), VOTED_TODAY(5), NOT_VALID_INPUT(6), VOTE_ADDED(7), ALREADY_HAS_EXAMPLE(8), EXAMPLE_ADDED(9),
	PASSWORD_OK(10), PASSWORD_ERROR(11), NOT_ALLOWED_ACTION(12);
	private String description;
	private int code;

	public static void init(Context context) {
		getErrorByCode(2).description = context
				.getString(R.string.votes_per_day_error);
	}

	public static ResponseCodes getErrorByCode(int code) {
		for (ResponseCodes c : ResponseCodes.values()) {
			if (c.code == code)
				return c;
		}
		return null;
	}

	private ResponseCodes(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public int getCode(){
		return code;
	}

}
