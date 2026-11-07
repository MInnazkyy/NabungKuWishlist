package com.zaki.nabungku;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ProfilActivity extends BaseActivity {

    private final int[] idTema = {R.id.temaTeal, R.id.temaBiru, R.id.temaUngu, R.id.temaOranye};
    private final String[] namaTema = {"teal", "biru", "ungu", "oranye"};
    private final String[] hexTema = {"#0F766E", "#2563EB", "#7C3AED", "#C2410C"};

    private PrefsManager prefs;
    private CheckBox cbSembunyi, cbGelap, cbPengingat;
    private EditText etNama, etN1, etN2, etN3;
    private TextView tvInfo, tvAvatar, tvNamaHeader;
    private String temaTerpilih;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        prefs = new PrefsManager(this);

        etNama      = findViewById(R.id.etNama);
        cbSembunyi  = findViewById(R.id.cbSembunyiSaldo);
        cbGelap     = findViewById(R.id.cbModeGelap);
        cbPengingat = findViewById(R.id.cbPengingat);
        etN1 = findViewById(R.id.etNominal1);
        etN2 = findViewById(R.id.etNominal2);
        etN3 = findViewById(R.id.etNominal3);
        tvInfo = findViewById(R.id.tvInfoPrefs);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvNamaHeader = findViewById(R.id.tvNamaHeader);

        // Isi form dari nilai tersimpan
        etNama.setText(prefs.getNama());
        cbSembunyi.setChecked(prefs.isSembunyiSaldo());
        cbGelap.setChecked(prefs.isGelap());
        cbPengingat.setChecked(prefs.isPengingat());
        String[] n = prefs.getNominalCepat().split(",");
        etN1.setText(n[0]);
        etN2.setText(n[1]);
        etN3.setText(n[2]);
        temaTerpilih = prefs.getTema();

        for (int i = 0; i < idTema.length; i++) {
            final int idx = i;
            View v = findViewById(idTema[i]);
            GradientDrawable oval = new GradientDrawable();
            oval.setShape(GradientDrawable.OVAL);
            oval.setColor(Color.parseColor(hexTema[i]));
            v.setBackground(oval);
            v.setOnClickListener(x -> {
                temaTerpilih = namaTema[idx];
                sorotTema();
                Ui.header(findViewById(R.id.header), Color.parseColor(hexTema[idx])); // pratinjau
                Ui.tombolUtama(findViewById(R.id.btnSimpan), Color.parseColor(hexTema[idx]));
            });
        }

        findViewById(R.id.btnSimpan).setOnClickListener(v -> simpan());
        findViewById(R.id.btnTes).setOnClickListener(v -> {
            if (!Reminder.punyaIzin(this)) {
                Reminder.mintaIzin(this);
                Toast.makeText(this, "Izinkan notifikasi, lalu tekan lagi", Toast.LENGTH_SHORT).show();
            } else {
                Reminder.tampilkan(this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Ui.header(findViewById(R.id.header), prefs.getWarnaTema());
        Ui.tombolUtama(findViewById(R.id.btnSimpan), prefs.getWarnaTema());
        tampilHeader();
        sorotTema();
        tampilkanInfo();
        Nav.pasang(this, Nav.PROFIL);
    }

    private void sorotTema() {
        for (int i = 0; i < idTema.length; i++) {
            findViewById(idTema[i]).setAlpha(namaTema[i].equals(temaTerpilih) ? 1f : 0.35f);
        }
    }

    private void tampilHeader() {
        String nama = prefs.getNama();
        tvNamaHeader.setText(nama);
        tvAvatar.setText(nama.isEmpty() ? "?" : nama.substring(0, 1).toUpperCase());
    }

    private void simpan() {
        String nama = etNama.getText().toString().trim();
        if (nama.isEmpty()) nama = prefs.getNama();
        String nominal = etN1.getText().toString().trim() + ","
                + etN2.getText().toString().trim() + ","
                + etN3.getText().toString().trim();
        if (!nominalValid(nominal)) {
            Toast.makeText(this, "Isi ketiga nominal dengan angka lebih dari 0", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.simpanPengaturan(nama, cbGelap.isChecked(), cbSembunyi.isChecked(),
                cbPengingat.isChecked(), nominal, temaTerpilih);

        AppCompatDelegate.setDefaultNightMode(cbGelap.isChecked()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        if (cbPengingat.isChecked()) {
            if (!Reminder.punyaIzin(this)) Reminder.mintaIzin(this);
            Reminder.jadwalkan(this);
        } else {
            Reminder.batalkan(this);
        }

        Toast.makeText(this, "Pengaturan disimpan", Toast.LENGTH_SHORT).show();
        Ui.header(findViewById(R.id.header), prefs.getWarnaTema());
        tampilHeader();
        tampilkanInfo();
        Nav.pasang(this, Nav.PROFIL);
    }

    private boolean nominalValid(String s) {
        String[] p = s.split(",", -1);
        if (p.length != 3) return false;
        for (String x : p) {
            if (x.isEmpty() || x.length() > 9) return false;
            for (char ch : x.toCharArray()) if (!Character.isDigit(ch)) return false;
            if (Long.parseLong(x) <= 0) return false; // nominal 0 tidak boleh
        }
        return true;
    }

    // Isi SharedPreferences ditampilkan sebagai bukti di video
    private void tampilkanInfo() {
        tvInfo.setText("nama = " + prefs.getNama() + " | gelap = " + prefs.isGelap()
                + "\nsembunyikan_saldo = " + prefs.isSembunyiSaldo()
                + "\nnominal_cepat = " + prefs.getNominalCepat()
                + "\ntema = " + prefs.getTema() + " | dibuka = " + prefs.getDibuka() + "x"
                + "\nstreak = " + prefs.getStreak() + " hari");
    }
}
