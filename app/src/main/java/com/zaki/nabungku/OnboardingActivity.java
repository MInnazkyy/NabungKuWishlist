package com.zaki.nabungku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Layar Selamat datang: hanya tampil sekali.
 * Nama + status onboarding -> SharedPreferences. Impian pertama (nama, sasaran, foto) diisi pengguna sendiri -> SQLite.
 */
public class OnboardingActivity extends BaseActivity {

    private String fotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        PrefsManager prefs = new PrefsManager(this);
        EditText etNama = findViewById(R.id.etNama);
        EditText etImpian = findViewById(R.id.etImpian);
        EditText etSasaran = findViewById(R.id.etSasaran);
        ImageView ivFoto = findViewById(R.id.ivFoto);
        Ui.tombolUtama(findViewById(R.id.btnMulai), prefs.getWarnaTema());

        findViewById(R.id.btnFoto).setOnClickListener(v -> foto.pilih(path -> {
            fotoPath = path;
            ivFoto.setImageBitmap(Ui.bitmap(path, 600));
            ivFoto.setVisibility(View.VISIBLE);
        }));

        findViewById(R.id.btnMulai).setOnClickListener(v -> {
            String nama = etNama.getText().toString().trim();
            if (nama.isEmpty()) {
                etNama.setError("Isi nama dulu ya");
                return;
            }
            String impian = etImpian.getText().toString().trim();
            if (impian.isEmpty()) {
                etImpian.setError("Isi nama impianmu dulu ya");
                return;
            }

            String s = etSasaran.getText().toString().trim();
            if (s.isEmpty() || s.length() > 12 || Long.parseLong(s) < 1000) {
                etSasaran.setError("Sasaran minimal Rp 1.000");
                return;
            }
            long sasaran = Long.parseLong(s);

            new DatabaseHelper(this).tambahTarget(impian, sasaran, fotoPath);
            prefs.selesaiOnboarding(nama);
            startActivity(new Intent(this, BerandaActivity.class));
            finish();
        });
    }
}
