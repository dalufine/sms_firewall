package com.quazar.sms_firewall.dialogs;

import android.app.Dialog;
import android.content.Context;

import com.quazar.sms_firewall.R;

public class HelpDialog extends Dialog{
	public static enum Window{MAIN, TOPS, FILTERS, LOGS, SETTINGS};
	public HelpDialog(final Context context, Window window){
		super(context, R.style.Dialog);
		switch(window){
			case FILTERS:
				setContentView(R.layout.dialog_help_filters);
				break;
			case TOPS:
				setContentView(R.layout.dialog_help_tops);
				break;
			case LOGS:
				setContentView(R.layout.dialog_help_logs);
				break;
			case SETTINGS:
				setContentView(R.layout.dialog_help_settings);
				break;
			default:
				setContentView(R.layout.dialog_help_main);
		}
	}
}
