package com.zaki.nabungku;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.io.File;

/** Bottom navigation bersama + dialog Setor (dengan foto bukti) / Tarik. */
public class Nav {

    public static final int BERANDA = 0, TARGET = 1, SETOR = 2, RIWAYAT = 3, PROFIL = 4;

    public static void pasang(Activity a, int aktif) {
        PrefsManager prefs = new PrefsManager(a);
        int warna = prefs.getWarnaTema();
        int[] ids = {R.id.navBeranda, R.id.navTarget, R.id.navSetor, R.id.navRiwayat, R.id.navProfil};
        Class<?>[] tujuan = {BerandaActivity.class, TargetActivity.class, null,
                RiwayatActivity.class, ProfilActivity.class};

        for (int i = 0; i < ids.length; i++) {
            final int idx = i;
            TextView t = a.findViewById(ids[i]);

            if (i == SETOR) {
                GradientDrawable oval = new GradientDrawable();
                oval.setShape(GradientDrawable.OVAL);
                oval.setColor(warna);
                t.setBackground(oval);
                t.setElevation(Ui.dp(a, 6));
                t.setOnClickListener(v -> tampilTransaksi(a, true));
                continue;
            }

            int warnaItem = i == aktif ? warna : a.getColor(R.color.teks_redup);
            t.setTextColor(warnaItem);
            t.setCompoundDrawableTintList(ColorStateList.valueOf(warnaItem));
            t.setOnClickListener(v -> {
                if (idx == aktif) return;
                Intent in = new Intent(a, tujuan[idx]);
                in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                if (tujuan[idx] == BerandaActivity.class) {
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }
                a.startActivity(in);
                if (!(a instanceof BerandaActivity)) a.finish();
            });
        }
    }

    private static void hapusFile(String path) {
        if (path != null) new File(path).delete();
    }

    /** Dialog: pilih target + isi nominal (+ bukti foto untuk setor) -> simpan ke SQLite. */
    public static void tampilTransaksi(Activity a, boolean setor) {
        DatabaseHelper db = new DatabaseHelper(a);
        Cursor c = db.getTargets();
        final long[] ids = new long[c.getCount()];
        final String[] nama = new String[c.getCount()];
        final String[] label = new String[c.getCount()];
        int i = 0;
        while (c.moveToNext()) {
            ids[i] = c.getLong(0);
            nama[i] = c.getString(1);
            label[i] = setor ? nama[i] : nama[i] + " (" + Ui.rupiah(c.getLong(3)) + ")";
            i++;
        }
        c.close();
        if (ids.length == 0) {
            Toast.makeText(a, "Buat target dulu di tab Target", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(a, 20), Ui.dp(a, 8), Ui.dp(a, 20), 0);
        Spinner sp = new Spinner(a);
        sp.setAdapter(new ArrayAdapter<>(a, android.R.layout.simple_spinner_dropdown_item, label));
        EditText et = new EditText(a);
        et.setHint(setor ? "Nominal setor (Rp)" : "Nominal tarik (Rp)");
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addView(sp);
        box.addView(et);

        final String[] bukti = {null};
        if (setor) {
            Button btn = new Button(a);
            btn.setText("📎 Lampirkan bukti (opsional)");
            btn.setAllCaps(false);
            ImageView prev = new ImageView(a);
            prev.setAdjustViewBounds(true);
            prev.setMaxHeight(Ui.dp(a, 140));
            prev.setVisibility(View.GONE);
            btn.setOnClickListener(v -> {
                if (!(a instanceof BaseActivity)) return;
                ((BaseActivity) a).foto.pilih(path -> {
                    hapusFile(bukti[0]);
                    bukti[0] = path;
                    prev.setImageBitmap(Ui.bitmap(path, 600));
                    prev.setVisibility(View.VISIBLE);
                    btn.setText("📎 Ganti bukti");
                });
            });
            box.addView(btn);
            box.addView(prev);
        }

        new AlertDialog.Builder(a)
                .setTitle(setor ? "Setor tabungan" : "Tarik dari amplop")
                .setView(box)
                .setPositiveButton(setor ? "Setor" : "Tarik", (d, w) -> {
                    String s = et.getText().toString().trim();
                    if (s.isEmpty() || s.length() > 12) return;
                    long nominal = Long.parseLong(s);
                    if (nominal <= 0) return;
                    int pos = sp.getSelectedItemPosition();
                    if (setor) {
                        db.setor(ids[pos], nominal, bukti[0]); // path foto ikut tersimpan
                    } else if (!db.tarik(ids[pos], nominal)) {
                        Toast.makeText(a, "Isi amplop " + nama[pos] + " tidak cukup",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(a, (setor ? "Setor " : "Tarik ") + Ui.rupiah(nominal)
                            + (setor ? " ke " : " dari ") + nama[pos], Toast.LENGTH_SHORT).show();
                    a.recreate();
                })
                .setNegativeButton("Batal", (d, w) -> hapusFile(bukti[0]))
                .setOnCancelListener(d -> hapusFile(bukti[0]))
                .show();
    }
}
