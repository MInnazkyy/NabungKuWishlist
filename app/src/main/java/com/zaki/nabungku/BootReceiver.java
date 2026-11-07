package com.zaki.nabungku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Alarm hilang saat HP restart; jadwalkan ulang jika pengingat aktif di SharedPreferences. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && new PrefsManager(context).isPengingat()) {
            Reminder.jadwalkan(context);
        }
    }
}
