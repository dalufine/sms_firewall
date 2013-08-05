package com.quazar.sms_firewall.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.quazar.sms_firewall.R;

public class HelpDialog extends AlertDialog {
	public HelpDialog(final Context context, String title, String text) {
		super(context);
		View v = getLayoutInflater().inflate(R.layout.help_dialog, null);
		setView(v);
		v.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				HelpDialog.this.dismiss();
			}
		});
		((TextView) v.findViewById(R.id.help_title)).setText(title);
		((TextView) v.findViewById(R.id.help_text)).setText(text);
	}
}
