package com.quazar.sms_firewall.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class DialogUtils {
	public static void createWarningDialog(final AlertDialog dialog, String title, String message, String buttonTitle){
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setCancelable(true);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, buttonTitle, new OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}
}
