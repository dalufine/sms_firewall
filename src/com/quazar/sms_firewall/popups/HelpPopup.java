package com.quazar.sms_firewall.popups;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.quazar.sms_firewall.R;

public class HelpPopup extends AlertDialog {
	public HelpPopup(final Context context, String title, String text) {
		super(context);
		View v = getLayoutInflater().inflate(R.layout.help_popup, null);
		setView(v);
		v.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				HelpPopup.this.dismiss();
			}
		});
		((TextView) v.findViewById(R.id.help_title)).setText(title);
		((TextView) v.findViewById(R.id.help_text)).setText(text);
	}
}
