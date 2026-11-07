package com.zaki.nabungku;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BerandaActivity extends BaseActivity {

    private PrefsManager prefs;
    private DatabaseHelper db;
    private TextView tvSapaan, tvStreak, tvSaldo, tvMinggu, tvTarget, tvProgres;
    private ProgressBar pb;
    private Button btn1, btn2, btn3;
    private LinearLayout llTerbaru, llFotoUtama;
    private long targetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new PrefsManager(this);

        // Pertama kali dibuka -> layar Selamat datang (status dari SharedPreferences)
        if (!prefs.isOnboarded()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_beranda);
        db = new DatabaseHelper(this);
        tvSapaan  = findViewById(R.id.tvSapaan);
        tvStreak  = findViewById(R.id.tvStreak);
        tvSaldo   = findViewById(R.id.tvSaldo);
        tvMinggu  = findViewById(R.id.tvMinggu);
        tvTarget  = findViewById(R.id.tvTarget);
        tvProgres = findViewById(R.id.tvProgres);
        pb        = findViewById(R.id.pbTarget);
        btn1 = findViewById(R.id.btnSetor1);
        btn2 = findViewById(R.id.btnSetor2);
        btn3 = findViewById(R.id.btnSetor3);
        llTerbaru = findViewById(R.id.llTerbaru);
        llFotoUtama = findViewById(R.id.llFotoUtama);

        findViewById(R.id.btnMata).setOnClickListener(v -> {
            prefs.setSembunyiSaldo(!prefs.isSembunyiSaldo()); // langsung tersimpan
            tampilkanData();
        });

        findViewById(R.id.btnTarik).setOnClickListener(v -> Nav.tampilTransaksi(this, false));

        // Pertama kali dibuka: minta izin notifikasi untuk pengingat 19:00
        // savedInstanceState == null -> app benar-benar dibuka. Kalau tidak null, layar hanya
        // dibuat ulang (ganti tema, setor, rotasi) dan TIDAK boleh dihitung sebagai "dibuka".
        if (savedInstanceState == null) {
            if (prefs.getDibuka() == 0 && !Reminder.punyaIzin(this)) {
                Reminder.mintaIzin(this);
            }
            prefs.tambahHitungBuka();
            prefs.catatKunjungan();
        }
    }

    // Pengaturan dibaca ulang tiap layar kembali tampil -> perubahan di Profil langsung terasa
    @Override
    protected void onResume() {
        super.onResume();
        if (tvSapaan == null) return; // sedang pindah ke onboarding

        Ui.header(findViewById(R.id.header), prefs.getWarnaTema());
        pb.setProgressTintList(ColorStateList.valueOf(prefs.getWarnaTema()));
        tvMinggu.setTextColor(prefs.getWarnaTema());
        tvSapaan.setText("Halo, " + prefs.getNama());
        tvStreak.setText("🔥 " + prefs.getStreak() + " hari");

        // Tombol setor cepat: nominalnya dari SharedPreferences
        String[] n = prefs.getNominalCepat().split(",");
        pasangSetor(btn1, n[0]);
        pasangSetor(btn2, n[1]);
        pasangSetor(btn3, n[2]);

        tampilkanData();
        Nav.pasang(this, Nav.BERANDA);
    }

    private void pasangSetor(Button b, String nominalStr) {
        final long nominal = Long.parseLong(nominalStr.trim());
        b.setText("+" + Ui.rupiah(nominal));
        b.setOnClickListener(v -> {
            if (targetId <= 0) {
                Toast.makeText(this, "Buat target dulu di tab Target", Toast.LENGTH_SHORT).show();
                return;
            }
            db.setor(targetId, nominal);
            Toast.makeText(this, "Setor " + Ui.rupiah(nominal), Toast.LENGTH_SHORT).show();
            tampilkanData();
        });
    }

    private void tampilkanData() {
        // Saldo & target dari SQLite; tampilannya diatur SharedPreferences
        tvSaldo.setText(prefs.isSembunyiSaldo() ? "Rp ••••••••" : Ui.rupiah(db.getSaldo()));
        tvMinggu.setText("+" + Ui.rupiah(db.getSetorMingguIni()) + " minggu ini");

        Cursor t = db.getTargets();
        if (t.moveToFirst()) {
            targetId = t.getLong(0);
            long sasaran = t.getLong(2), terkumpul = t.getLong(3);
            int persen = Ui.persen(terkumpul, sasaran);
            tvTarget.setText("Target utama: " + t.getString(1));
            tvProgres.setText(persen + "%  ·  " + Ui.rupiah(terkumpul) + " / " + Ui.rupiah(sasaran));
            Ui.animasiProgres(pb, persen);
            llFotoUtama.removeAllViews();
            llFotoUtama.addView(Ui.thumb(this, t.getString(4), 64)); // foto impian
        }
        else {
            targetId = 0;
            tvTarget.setText("Belum ada target");
            tvProgres.setText("Buat target di tab Target");
            pb.setProgress(0);
            llFotoUtama.removeAllViews();
        }
        t.close();

        llTerbaru.removeAllViews();
        Cursor c = db.getRiwayat(3);
        while (c.moveToNext()) {
            llTerbaru.addView(Ui.baris(this, c.getString(0), c.getString(1), c.getLong(2),
                    "Setor".equals(c.getString(3)), c.getString(4)));
        }
        c.close();
    }
}
