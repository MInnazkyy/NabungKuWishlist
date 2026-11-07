package com.zaki.nabungku;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

/** Pengingat nabung harian jam 19:00 (aktif/tidaknya diatur dari SharedPreferences). */
public class Reminder {

    private static final String CHANNEL = "pengingat_nabung";

    public static void jadwalkan(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 19);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent(c));
    }

    public static void batalkan(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(alarmIntent(c));
    }

    private static PendingIntent alarmIntent(Context c) {
        Intent i = new Intent(c, ReminderReceiver.class);
        return PendingIntent.getBroadcast(c, 1, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static boolean punyaIzin(Context c) {
        return Build.VERSION.SDK_INT < 33
                || ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void mintaIzin(Activity a) {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(a,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7);
        }
    }

    public static void tampilkan(Context c) {
        if (!punyaIzin(c)) return;
        NotificationManager nm =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(
                    CHANNEL, "Pengingat nabung", NotificationManager.IMPORTANCE_DEFAULT));
        }
        PendingIntent tap = PendingIntent.getActivity(c, 2,
                new Intent(c, BerandaActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new NotificationCompat.Builder(c, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Waktunya nabung 🎯")
                .setContentText("Hai " + new PrefsManager(c).getNama() + ", sudah setor hari ini?")
                .setContentIntent(tap)
                .setAutoCancel(true)
                .build();
        nm.notify(1, n);
    }
}
