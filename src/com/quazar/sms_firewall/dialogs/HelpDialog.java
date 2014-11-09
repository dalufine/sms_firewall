package com.quazar.sms_firewall.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.quazar.sms_firewall.R;

public class HelpDialog extends Dialog {
	public HelpDialog(final Context context, String title, String text) {
		super(context, R.style.Dialog);
		View v = getLayoutInflater().inflate(R.layout.dialog_help, null);
		setContentView(v);
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
