package com.zaki.nabungku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Memilih foto dari galeri, lalu MENYALIN ke penyimpanan internal app (files/foto).
 * Yang disimpan di SQLite hanya path-nya, bukan isi fotonya.
 */
public class FotoPicker {

    public interface Hasil { void siap(String path); }

    private final Context ctx;
    private final ActivityResultLauncher<String> launcher;
    private Hasil menunggu;

    public FotoPicker(ComponentActivity a) {
        ctx = a;
        launcher = a.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            Hasil h = menunggu;
            menunggu = null;
            if (uri == null || h == null) return;
            String path = salin(uri);
            if (path != null) {
                h.siap(path);
            } else {
                Toast.makeText(ctx, "Foto gagal dibuka", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pilih(Hasil h) {
        menunggu = h;
        launcher.launch("image/*");
    }

    private String salin(Uri uri) {
        try {
            // 1) ukur dulu, supaya tidak memuat foto penuh ke memori
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, o);
            }
            int sisi = Math.max(o.outWidth, o.outHeight);
            int sample = 1;
            while (sisi / (sample * 2) >= 1280) sample *= 2;

            // 2) decode versi kecil
            o = new BitmapFactory.Options();
            o.inSampleSize = sample;
            Bitmap bmp;
            try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                bmp = BitmapFactory.decodeStream(in, null, o);
            }
            if (bmp == null) return null;

            // 3) putar sesuai EXIF (kalau ada)
            int derajat = 0;
            try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                ExifInterface ex = new ExifInterface(in);
                int ori = ex.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                if (ori == ExifInterface.ORIENTATION_ROTATE_90) derajat = 90;
                else if (ori == ExifInterface.ORIENTATION_ROTATE_180) derajat = 180;
                else if (ori == ExifInterface.ORIENTATION_ROTATE_270) derajat = 270;
            } catch (Exception ignore) { }
            if (derajat != 0) {
                Matrix m = new Matrix();
                m.postRotate(derajat);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
            }

            // 4) simpan ke penyimpanan internal app
            File dir = new File(ctx.getFilesDir(), "foto");
            dir.mkdirs();
            File f = new File(dir, "f" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(f)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
            }
            return f.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }
}
