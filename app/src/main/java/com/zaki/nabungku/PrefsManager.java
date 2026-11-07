package com.zaki.nabungku;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Satu-satunya pintu akses SharedPreferences.
 * Hanya menyimpan PENGATURAN & status kecil. Data transaksi/target ada di SQLite.
 */
public class PrefsManager {

    private static final String FILE = "nabungku_prefs";
    private static final String KEY_NAMA = "nama";
    private static final String KEY_GELAP = "mode_gelap";
    private static final String KEY_SEMBUNYI_SALDO = "sembunyikan_saldo";
    private static final String KEY_PENGINGAT = "pengingat_aktif";
    private static final String KEY_NOMINAL_CEPAT = "nominal_cepat";
    private static final String KEY_TEMA = "warna_tema";
    private static final String KEY_DIBUKA = "jumlah_dibuka";
    private static final String KEY_ONBOARD = "sudah_onboarding";
    private static final String KEY_STREAK = "streak_hari";
    private static final String KEY_TGL = "tanggal_terakhir";

    private final SharedPreferences sp;

    public PrefsManager(Context context) {
        sp = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    // ---------- SIMPAN: apply() = asinkron, tidak memblokir UI ----------
    public void simpanPengaturan(String nama, boolean gelap, boolean sembunyiSaldo,
                                 boolean pengingat, String nominalCepat, String tema) {
        sp.edit()
                .putString(KEY_NAMA, nama)
                .putBoolean(KEY_GELAP, gelap)
                .putBoolean(KEY_SEMBUNYI_SALDO, sembunyiSaldo)
                .putBoolean(KEY_PENGINGAT, pengingat)
                .putString(KEY_NOMINAL_CEPAT, nominalCepat)
                .putString(KEY_TEMA, tema)
                .apply();
    }

    public void setSembunyiSaldo(boolean b) {
        sp.edit().putBoolean(KEY_SEMBUNYI_SALDO, b).apply();
    }

    public void selesaiOnboarding(String nama) {
        sp.edit().putString(KEY_NAMA, nama).putBoolean(KEY_ONBOARD, true).apply();
    }

    public void tambahHitungBuka() {
        sp.edit().putInt(KEY_DIBUKA, getDibuka() + 1).apply();
    }

    /** Streak: berapa hari berturut-turut app dibuka (disimpan sebagai angka + tanggal terakhir). */
    public void catatKunjungan() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        String hariIni = f.format(cal.getTime());
        String terakhir = sp.getString(KEY_TGL, "");
        if (hariIni.equals(terakhir)) return;
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String kemarin = f.format(cal.getTime());
        int baru = kemarin.equals(terakhir) ? getStreak() + 1 : 1;
        sp.edit().putInt(KEY_STREAK, baru).putString(KEY_TGL, hariIni).apply();
    }

    // ---------- BACA (dengan nilai default) ----------
    public String getNama()          { return sp.getString(KEY_NAMA, ""); }
    public boolean isGelap()         { return sp.getBoolean(KEY_GELAP, false); }
    public boolean isSembunyiSaldo() { return sp.getBoolean(KEY_SEMBUNYI_SALDO, false); }
    public boolean isPengingat()     { return sp.getBoolean(KEY_PENGINGAT, true); }
    public String getNominalCepat()  { return sp.getString(KEY_NOMINAL_CEPAT, "10000,20000,50000"); }
    public String getTema()          { return sp.getString(KEY_TEMA, "teal"); }
    public int getDibuka()           { return sp.getInt(KEY_DIBUKA, 0); }
    public boolean isOnboarded()     { return sp.getBoolean(KEY_ONBOARD, false); }
    public int getStreak()           { return sp.getInt(KEY_STREAK, 0); }

    public int getWarnaTema() {
        switch (getTema()) {
            case "biru":   return Color.parseColor("#2563EB");
            case "ungu":   return Color.parseColor("#7C3AED");
            case "oranye": return Color.parseColor("#C2410C");
            default:       return Color.parseColor("#0F766E");
        }
    }
}
