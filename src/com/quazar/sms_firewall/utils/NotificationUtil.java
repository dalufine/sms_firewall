package com.quazar.sms_firewall.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.quazar.sms_firewall.activities.MainActivity;

public class NotificationUtil{

	public static void warning(Context context, int iconId, int titleId, String text){
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context).setSmallIcon(iconId).setContentTitle(
						context.getResources().getString(titleId)).setContentText(text).setTicker(text);
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(777, builder.build());
	}
}
