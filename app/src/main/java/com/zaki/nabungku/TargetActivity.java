package com.zaki.nabungku;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.io.File;

public class TargetActivity extends BaseActivity {

    private PrefsManager prefs;
    private DatabaseHelper db;
    private LinearLayout llTarget;
    private TextView tvSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);
        prefs = new PrefsManager(this);
        db = new DatabaseHelper(this);
        llTarget = findViewById(R.id.llTarget);
        tvSub = findViewById(R.id.tvSub);
        findViewById(R.id.btnBuat).setOnClickListener(v -> dialogTarget(-1, "", 0, null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Ui.header(findViewById(R.id.header), prefs.getWarnaTema());
        tampil();
        Nav.pasang(this, Nav.TARGET);
    }

    private void tampil() {
        llTarget.removeAllViews();
        Cursor c = db.getTargets();
        tvSub.setText(c.getCount() + " amplop aktif · ketuk kartu untuk ubah / hapus");
        while (c.moveToNext()) {
            llTarget.addView(kartu(c.getLong(0), c.getString(1), c.getLong(2),
                    c.getLong(3), c.getString(4)));
        }
        c.close();
    }

    private LinearLayout kartu(long id, String nama, long sasaran, long terkumpul, String fotoPath) {
        int persen = Ui.persen(terkumpul, sasaran);

        LinearLayout k = new LinearLayout(this);
        k.setOrientation(LinearLayout.HORIZONTAL);
        k.setGravity(Gravity.CENTER_VERTICAL);
        k.setBackgroundResource(R.drawable.bg_card);
        k.setElevation(Ui.dp(this, 2));
        int p = Ui.dp(this, 16);
        k.setPadding(p, p, p, p);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(this, 12);
        k.setLayoutParams(lp);
        k.setOnClickListener(v -> dialogTarget(id, nama, sasaran, fotoPath));

        // Foto impian (placeholder kalau belum ada)
        android.view.View thumb = Ui.thumb(this, fotoPath, 64);
        ((LinearLayout.LayoutParams) thumb.getLayoutParams()).setMarginEnd(Ui.dp(this, 14));
        k.addView(thumb);

        LinearLayout isi = new LinearLayout(this);
        isi.setOrientation(LinearLayout.VERTICAL);
        k.addView(isi, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout baris = new LinearLayout(this);
        baris.setGravity(Gravity.CENTER_VERTICAL);
        TextView tn = new TextView(this);
        tn.setText(nama);
        tn.setTextSize(17);
        tn.setTypeface(null, Typeface.BOLD);
        tn.setTextColor(getColor(R.color.teks));
        baris.addView(tn, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView tp = new TextView(this);
        tp.setText(persen + "%");
        tp.setTextSize(17);
        tp.setTypeface(null, Typeface.BOLD);
        tp.setTextColor(prefs.getWarnaTema());
        baris.addView(tp);
        isi.addView(baris);

        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        Ui.animasiProgres(pb, persen);
        pb.setProgressTintList(ColorStateList.valueOf(prefs.getWarnaTema()));
        LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pl.topMargin = Ui.dp(this, 8);
        isi.addView(pb, pl);

        TextView tj = new TextView(this);
        tj.setText(Ui.rupiah(terkumpul) + " / " + Ui.rupiah(sasaran));
        tj.setTextColor(getColor(R.color.teks_redup));
        isi.addView(tj);
        return k;
    }

    /** id < 0 = buat baru; selain itu = ubah target (nama, sasaran, foto impian). */
    private void dialogTarget(long id, String namaAwal, long sasaranAwal, String fotoAwal) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(this, 20), Ui.dp(this, 8), Ui.dp(this, 20), 0);

        EditText etNama = new EditText(this);
        etNama.setHint("Nama impian (mis. Laptop)");
        EditText etSasaran = new EditText(this);
        etSasaran.setHint("Sasaran (Rp)");
        etSasaran.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (id >= 0) {
            etNama.setText(namaAwal);
            etSasaran.setText(String.valueOf(sasaranAwal));
        }

        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackgroundResource(R.drawable.bg_chip);
        preview.setClipToOutline(true);
        android.graphics.Bitmap bm = Ui.bitmap(fotoAwal, 600);
        if (bm != null) preview.setImageBitmap(bm);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 140));
        pp.topMargin = Ui.dp(this, 8);

        final String[] fotoBaru = {null};
        Button btnFoto = new Button(this);
        btnFoto.setText("🖼 Pilih foto impian");
        btnFoto.setAllCaps(false);
        btnFoto.setOnClickListener(v -> foto.pilih(path -> {
            if (fotoBaru[0] != null) new File(fotoBaru[0]).delete();
            fotoBaru[0] = path;
            preview.setImageBitmap(Ui.bitmap(path, 600));
        }));

        box.addView(etNama);
        box.addView(etSasaran);
        box.addView(preview, pp);
        box.addView(btnFoto);

        new AlertDialog.Builder(this)
                .setTitle(id < 0 ? "Buat target baru" : "Ubah target")
                .setView(box)
                .setPositiveButton("Simpan", (d, w) -> {
                    String nama = etNama.getText().toString().trim();
                    String s = etSasaran.getText().toString().trim();
                    if (nama.isEmpty() || s.isEmpty() || s.length() > 12) return;
                    long sasaran = Long.parseLong(s);
                    if (sasaran < 1000) {
                        Toast.makeText(this, "Sasaran minimal Rp 1.000", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (id < 0) {
                        db.tambahTarget(nama, sasaran, fotoBaru[0]);
                    } else {
                        db.ubahTarget(id, nama, sasaran, fotoBaru[0]);
                        if (fotoBaru[0] != null && fotoAwal != null) new File(fotoAwal).delete();
                    }
                    tampil();
                })
                .setNeutralButton(id < 0 ? null : "Hapus", (d, w) -> {
                    if (fotoBaru[0] != null) new File(fotoBaru[0]).delete();
                    new AlertDialog.Builder(this)
                            .setTitle("Hapus target \"" + namaAwal + "\"?")
                            .setMessage("Semua setoran/penarikan dan foto target ini ikut terhapus. "
                                    + "Tidak bisa dibatalkan.")
                            .setPositiveButton("Hapus", (d2, w2) -> {
                                db.hapusTarget(id);
                                tampil();
                                Toast.makeText(this, "Target dihapus", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                })
                .setNegativeButton("Batal", (d, w) -> {
                    if (fotoBaru[0] != null) new File(fotoBaru[0]).delete();
                })
                .setOnCancelListener(d -> {
                    if (fotoBaru[0] != null) new File(fotoBaru[0]).delete();
                })
                .show();
    }
}
