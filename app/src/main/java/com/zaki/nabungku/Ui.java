package com.zaki.nabungku;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import java.io.File;
import java.util.Locale;

public class Ui {

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static String rupiah(long v) {
        return "Rp " + String.format(Locale.US, "%,d", v).replace(',', '.');
    }

    /** Persen progres, dibatasi 0..100 supaya tidak meluap kalau terkumpul melebihi sasaran. */
    public static int persen(long terkumpul, long sasaran) {
        long p = terkumpul * 100 / Math.max(sasaran, 1);
        return (int) Math.max(0, Math.min(p, 100));
    }

    /** Header berwarna tema dengan sudut bawah membulat. */
    public static void header(View v, int warna) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(warna);
        float r = dp(v.getContext(), 28);
        g.setCornerRadii(new float[]{0, 0, 0, 0, r, r, r, r});
        v.setBackground(g);
    }

    /** Progress bar bergerak halus dari nilai sekarang ke persen baru. */
    public static void animasiProgres(ProgressBar pb, int persen) {
        ObjectAnimator a = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), persen);
        a.setDuration(700);
        a.setInterpolator(new DecelerateInterpolator());
        a.start();
    }

    /** Tombol utama membulat, berwarna tema. */
    public static void tombolUtama(Button b, int warna) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(warna);
        g.setCornerRadius(dp(b.getContext(), 14));
        b.setBackground(g);
        b.setTextColor(0xFFFFFFFF);
        b.setAllCaps(false);
    }

    /** Muat foto dari path, dikecilkan sesuai kebutuhan. null kalau tidak ada. */
    public static Bitmap bitmap(String path, int sisiPx) {
        if (path == null || !new File(path).exists()) return null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        int sisi = Math.max(o.outWidth, o.outHeight);
        int s = 1;
        while (sisi / (s * 2) >= sisiPx) s *= 2;
        o = new BitmapFactory.Options();
        o.inSampleSize = s;
        return BitmapFactory.decodeFile(path, o);
    }

    /** Kotak foto kecil; kalau belum ada foto, tampil placeholder 🖼. Parent harus LinearLayout. */
    public static View thumb(Context c, String path, int ukuranDp) {
        int px = dp(c, ukuranDp);
        Bitmap bm = bitmap(path, px * 2);
        View v;
        if (bm == null) {
            TextView t = new TextView(c);
            t.setText("🖼");
            t.setTextSize(22);
            t.setGravity(Gravity.CENTER);
            t.setBackgroundResource(R.drawable.bg_chip);
            v = t;
        } else {
            ImageView iv = new ImageView(c);
            iv.setImageBitmap(bm);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundResource(R.drawable.bg_chip);
            iv.setClipToOutline(true);
            v = iv;
        }
        v.setLayoutParams(new LinearLayout.LayoutParams(px, px));
        return v;
    }

    /** Tampilkan foto ukuran besar di dialog. */
    public static void tampilFoto(Context c, String path) {
        Bitmap bm = bitmap(path, 1400);
        if (bm == null) return;
        ImageView iv = new ImageView(c);
        iv.setImageBitmap(bm);
        iv.setAdjustViewBounds(true);
        new AlertDialog.Builder(c).setView(iv).setPositiveButton("Tutup", null).show();
    }

    /** Satu baris transaksi: [foto bukti] judul + waktu di kiri, nominal di kanan. */
    public static View baris(Context c, String judul, String waktu, long nominal,
                             boolean masuk, String bukti) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int p = dp(c, 10);
        row.setPadding(0, p, 0, p);

        if (bukti != null && new File(bukti).exists()) {
            View t = thumb(c, bukti, 44);
            ((LinearLayout.LayoutParams) t.getLayoutParams()).setMarginEnd(dp(c, 12));
            t.setOnClickListener(v -> tampilFoto(c, bukti));
            row.addView(t);
        }

        LinearLayout kiri = new LinearLayout(c);
        kiri.setOrientation(LinearLayout.VERTICAL);
        row.addView(kiri, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView a = new TextView(c);
        a.setText(judul);
        a.setTextSize(15);
        a.setTextColor(c.getColor(R.color.teks));
        TextView b = new TextView(c);
        b.setText(waktu);
        b.setTextSize(12);
        b.setTextColor(c.getColor(R.color.teks_redup));
        kiri.addView(a);
        kiri.addView(b);

        TextView n = new TextView(c);
        n.setText((masuk ? "+" : "-") + rupiah(nominal));
        n.setTextSize(15);
        n.setTypeface(null, Typeface.BOLD);
        n.setTextColor(c.getColor(masuk ? R.color.hijau : R.color.merah));
        row.addView(n);
        return row;
    }
}
