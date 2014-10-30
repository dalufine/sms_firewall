package com.quazar.sms_firewall.utils;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;

import com.quazar.sms_firewall.ErrorCodes;
import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.StateManager;
import com.quazar.sms_firewall.dao.DataDao;
import com.quazar.sms_firewall.dialogs.CallsSelectDialog;
import com.quazar.sms_firewall.dialogs.EnterValueDialog;
import com.quazar.sms_firewall.dialogs.SelectSourceDialog;
import com.quazar.sms_firewall.dialogs.SmsSelectDialog;
import com.quazar.sms_firewall.dialogs.listeners.DialogListener;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;
import com.quazar.sms_firewall.models.Filter.FilterType;

public class DialogUtils {
	public static void createWarningDialog(final Context context, String title,
			String message, String buttonTitle) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(message).setCancelable(true)
				.setPositiveButton(buttonTitle, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	public static void showSourceSelectPopup(final Activity activity,
			final Handler handler, final boolean forCheck) {
		SelectSourceDialog sourceSelect = new SelectSourceDialog(activity,
				new SelectListener<Integer>() {
					@Override
					public void recieveSelection(Integer selection) {
						final DataDao dao = new DataDao(activity);
						try {
							switch (selection) {
							case SelectSourceDialog.FROM_CONTACTS:
								StateManager.getContact(activity);
								break;
							case SelectSourceDialog.FROM_INBOX_SMS:
								final List<HashMap<String, Object>> sms = ContentUtils
										.getInboxSms(activity);
								if (!sms.isEmpty()) {
									SmsSelectDialog smsPopup = new SmsSelectDialog(
											activity,
											new SelectListener<HashMap<String, Object>>() {
												@Override
												public void recieveSelection(
														HashMap<String, Object> selection) {
													String value = (String) selection
															.get(ContentUtils.SMS_NUMBER);
													if (forCheck) {
														if (handler != null) {
															Message mes = handler
																	.obtainMessage(
																			1,
																			value);
															handler.dispatchMessage(mes);
														}
													} else {
														dao.insertFilter(
																FilterType.PHONE_NAME,
																value);
														if (handler != null)
															handler.dispatchMessage(new Message());
													}
												}
											}, sms);
									smsPopup.show();
								} else {
									DialogUtils.createWarningDialog(
											activity,
											activity.getResources().getString(
													R.string.warning),
											activity.getResources().getString(
													R.string.no_sms_warn),
											activity.getResources().getString(
													R.string.ok));
								}
								break;
							case SelectSourceDialog.FROM_INCOME_CALLS:
								final List<HashMap<String, Object>> sources = ContentUtils
										.getIncomeCalls(activity);
								if (!sources.isEmpty()) {
									CallsSelectDialog callsPopup = new CallsSelectDialog(
											activity,
											new SelectListener<HashMap<String, Object>>() {
												@Override
												public void recieveSelection(
														HashMap<String, Object> selection) {
													String value = (String) selection
															.get(ContentUtils.CALLS_NUMBER);
													if (forCheck) {
														if (handler != null) {
															Message mes = handler
																	.obtainMessage(
																			1,
																			value);
															handler.dispatchMessage(mes);
														}
													} else {
														dao.insertFilter(
																FilterType.PHONE_NAME,
																value);
														if (handler != null)
															handler.dispatchMessage(new Message());
													}
												}
											}, sources);
									callsPopup.show();
								} else {
									DialogUtils.createWarningDialog(activity, activity.getResources()
											.getString(R.string.warning), activity.getResources()
											.getString(R.string.no_income_calls), activity.getResources()
											.getString(R.string.ok));
								}
								break;
							case SelectSourceDialog.FROM_SUSPICIOUS_SMS:
								if (forCheck) {
									EnterValueDialog evd = new EnterValueDialog(
											activity,
											activity.getResources()
													.getString(
															R.string.enter_number_or_name),
											new SelectListener<String>() {
												@Override
												public void recieveSelection(
														String selection) {
													if (handler != null) {
														Message mes = handler
																.obtainMessage(
																		1,
																		selection);
														handler.dispatchMessage(mes);
													}
												}
											});
									evd.show();
								}
								break;
							default:
								break;
							}
						} finally {
							dao.close();
						}
					}
				}, forCheck);
		sourceSelect.show();
	}

	public static void showEnterWordFilterPopup(final Context context,
			final Handler handler) {
		EnterValueDialog popup = new EnterValueDialog(context, context
				.getResources().getString(R.string.enter_word),
				new SelectListener<String>() {
					@Override
					public void recieveSelection(String selection) {
						DataDao dao = new DataDao(context);
						try {
							dao.insertFilter(FilterType.WORD, selection);
							if (handler != null)
								handler.dispatchMessage(new Message());
						} finally {
							dao.close();
						}
					}
				});
		popup.show();
	}

	public static void showConfirmDialog(Context context, String title,
			String question, final DialogListener<Boolean> listener) {
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(question)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								listener.ok(true);
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								listener.cancel();
							}
						}).show();
	}

	public static void showErrorDialog(Context context, int errorCode) {
		showInfoDialog(context, context.getString(R.string.error_dialog_title),
				ErrorCodes.getErrorByCode(errorCode).getDescription());
	}

	public static void showInfoDialog(Context context, String title, String info) {
		AlertDialog dialog = new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(title)
				.setMessage(info)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
	}
}
