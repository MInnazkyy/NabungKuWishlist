package com.zaki.nabungku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Baca pengaturan dari SharedPreferences: hanya tampil kalau pengingat aktif
        if (new PrefsManager(context).isPengingat()) {
            Reminder.tampilkan(context);
        }
    }
}
