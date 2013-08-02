package com.quazar.sms_firewall.dialogs.listeners;

public interface DialogListener<T>{
	void ok(T value);
	void cancel();
}
