package com.quazar.sms_firewall.dialogs;

import android.content.Context;
import android.text.InputType;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.utils.Utils;

public class EnterPasswordDialog extends EnterValueDialog{

	private String rightPassword;

	public EnterPasswordDialog(Context context, String passwordToCheck, DialogListener<String> listener){
		super(context, context.getResources().getString(R.string.ask_logs_password), InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD, listener);
		this.rightPassword = passwordToCheck;
	}

	public EnterPasswordDialog(Context context, int headerStringId, String passwordToCheck,
			DialogListener<String> listener){
		super(context, context.getResources().getString(headerStringId), InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD, listener);
		this.rightPassword = passwordToCheck;
	}

	@Override
	protected boolean isValidValue(String value){
		if (super.isValidValue(value)) {
			boolean validPassword = false;
			try {
				validPassword=Utils.md5AsBase64String(value).equalsIgnoreCase(rightPassword);
			}
			catch(Exception ex) {}
			if (rightPassword != null && !validPassword) {
				valueField.setError(getContext().getResources().getString(R.string.logs_password_error));
				return false;
			}
			if (value.length() < 8) {
				valueField.setError(getContext().getResources().getString(R.string.password_length_error));
				return false;
			}
		}
		return true;
	}
}
