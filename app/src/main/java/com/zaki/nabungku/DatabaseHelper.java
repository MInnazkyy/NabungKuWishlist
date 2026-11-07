package com.zaki.nabungku;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SQLite: data BESAR & berulang (target, transaksi).
 * Foto TIDAK disimpan di sini; kolom foto/bukti hanya berisi PATH file di penyimpanan internal.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final long HARI = 24L * 60 * 60 * 1000;

    public DatabaseHelper(Context c) { super(c, "nabungku.db", null, 4); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE target(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nama TEXT, sasaran INTEGER, terkumpul INTEGER DEFAULT 0, foto TEXT)");
        db.execSQL("CREATE TABLE transaksi(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "target_id INTEGER, jenis TEXT, nominal INTEGER, waktu TEXT, ts INTEGER,"
                + "bukti TEXT)");

        // Database kosong: pengguna membuat target sendiri (layar Selamat datang / tab Target)
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS transaksi");
        db.execSQL("DROP TABLE IF EXISTS target");
        onCreate(db);
    }

    private void masukkanTrx(SQLiteDatabase db, long targetId, String jenis, long nominal,
                             long ts, String bukti) {
        ContentValues v = new ContentValues();
        v.put("target_id", targetId);
        v.put("jenis", jenis);
        v.put("nominal", nominal);
        v.put("waktu", new SimpleDateFormat("dd MMM, HH:mm", new Locale("id", "ID"))
                .format(new Date(ts)));
        v.put("ts", ts);
        v.put("bukti", bukti); // path foto bukti (boleh null)
        db.insert("transaksi", null, v);
    }

    public void setor(long targetId, long nominal) {
        setor(targetId, nominal, null);
    }

    /** Setor ke target (id): catat transaksi (+ path foto bukti) dan tambah terkumpul, atomik. */
    public void setor(long targetId, long nominal, String bukti) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            masukkanTrx(db, targetId, "Setor", nominal, System.currentTimeMillis(), bukti);
            db.execSQL("UPDATE target SET terkumpul = terkumpul + ? WHERE id = ?",
                    new Object[]{nominal, targetId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Tarik dari target. Return false kalau isi amplop tidak cukup. */
    public boolean tarik(long targetId, long nominal) {
        long ada = satuAngka("SELECT terkumpul FROM target WHERE id = ?",
                new String[]{String.valueOf(targetId)});
        if (ada < nominal) return false;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            masukkanTrx(db, targetId, "Tarik", nominal, System.currentTimeMillis(), null);
            db.execSQL("UPDATE target SET terkumpul = terkumpul - ? WHERE id = ?",
                    new Object[]{nominal, targetId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public void tambahTarget(String nama, long sasaran, String foto) {
        ContentValues v = new ContentValues();
        v.put("nama", nama);
        v.put("sasaran", sasaran);
        v.put("foto", foto);
        getWritableDatabase().insert("target", null, v);
    }

    /** Ubah target. foto null = foto lama dipertahankan. */
    public void ubahTarget(long id, String nama, long sasaran, String foto) {
        getWritableDatabase().execSQL(
                "UPDATE target SET nama = ?, sasaran = ?, foto = COALESCE(?, foto) WHERE id = ?",
                new Object[]{nama, sasaran, foto, id});
    }

    /** Hapus target beserta transaksinya, lalu hapus file foto impian & foto bukti-nya. */
    public void hapusTarget(long id) {
        SQLiteDatabase db = getWritableDatabase();
        java.util.List<String> file = new java.util.ArrayList<>();
        Cursor c = db.rawQuery("SELECT foto FROM target WHERE id = ? UNION ALL "
                + "SELECT bukti FROM transaksi WHERE target_id = ?",
                new String[]{String.valueOf(id), String.valueOf(id)});
        while (c.moveToNext()) if (!c.isNull(0)) file.add(c.getString(0));
        c.close();
        db.beginTransaction();
        try {
            db.delete("transaksi", "target_id = ?", new String[]{String.valueOf(id)});
            db.delete("target", "id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        for (String f : file) new File(f).delete();
    }

    public long getSaldo() {
        return satuAngka("SELECT IFNULL(SUM(terkumpul),0) FROM target", null);
    }

    public long getSetorMingguIni() {
        long batas = System.currentTimeMillis() - 7 * HARI;
        return satuAngka("SELECT IFNULL(SUM(nominal),0) FROM transaksi "
                + "WHERE jenis = 'Setor' AND ts >= ?", new String[]{String.valueOf(batas)});
    }

    private long satuAngka(String sql, String[] args) {
        Cursor c = getReadableDatabase().rawQuery(sql, args);
        c.moveToFirst();
        long s = c.getLong(0);
        c.close();
        return s;
    }

    /** Kolom: 0 id, 1 nama, 2 sasaran, 3 terkumpul, 4 foto. */
    public Cursor getTargets() {
        return getReadableDatabase().rawQuery(
                "SELECT id, nama, sasaran, terkumpul, foto FROM target ORDER BY id", null);
    }

    /** Kolom: 0 judul, 1 waktu, 2 nominal, 3 jenis, 4 bukti. limit <= 0 berarti semua. */
    public Cursor getRiwayat(int limit) {
        String sql = "SELECT g.nama || ' · ' || t.jenis, t.waktu, t.nominal, t.jenis, t.bukti "
                + "FROM transaksi t JOIN target g ON g.id = t.target_id ORDER BY t.ts DESC";
        if (limit > 0) sql += " LIMIT " + limit;
        return getReadableDatabase().rawQuery(sql, null);
    }
}
