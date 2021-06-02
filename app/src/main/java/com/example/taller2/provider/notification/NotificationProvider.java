package com.example.taller2.provider.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.example.taller2.model.Notification;


import java.util.Calendar;

public class NotificationProvider {

    private static SharedPreferences settings;

    private static int alarmID = 1;

    public static Notification notificationUser = new Notification();

    public static void createAlarm(Context ctx){

        String finalHour, finalMinute;
        settings = ctx.getSharedPreferences("Taller 2", Context.MODE_PRIVATE);

        finalHour = "" + Calendar.getInstance().getTime().getHours();
        finalMinute = "" + Calendar.getInstance().getTime().getMinutes() + 2;

        Calendar today = Calendar.getInstance();

        today.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().getTime().getHours());
        today.set(Calendar.MINUTE, Calendar.getInstance().getTime().getMinutes());
        today.set(Calendar.SECOND, 0);

        SharedPreferences.Editor edit = settings.edit();
        edit.putString("hour", finalHour);
        edit.putString("minute", finalMinute);

        //SAVE ALARM TIME TO USE IT IN CASE OF REBOOT
        edit.putInt("alarmID", 1);
        edit.putLong("alarmTime", today.getTimeInMillis());

        edit.commit();

        setAlarm(1, today.getTimeInMillis(), ctx);
    }

    public static void setAlarm(int i, Long timestamp, Context ctx) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getBroadcast(ctx, i, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
        alarmIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
    }

}
