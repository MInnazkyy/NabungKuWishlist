package com.zaki.nabungku;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

/** Menerapkan mode gelap & jadwal pengingat dari SharedPreferences sejak app dijalankan. */
public class NabungKuApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PrefsManager prefs = new PrefsManager(this);
        AppCompatDelegate.setDefaultNightMode(prefs.isGelap()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        if (prefs.isPengingat()) {
            Reminder.jadwalkan(this);
        } else {
            Reminder.batalkan(this);
        }
    }
}
