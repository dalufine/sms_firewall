package com.quazar.sms_firewall.popups;

public interface DialogListener<T>{
	void ok(T value);
	void cancel();
}
