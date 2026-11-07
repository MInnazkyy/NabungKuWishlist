package com.zaki.nabungku;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class RiwayatActivity extends BaseActivity {

    private PrefsManager prefs;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);
        prefs = new PrefsManager(this);
        db = new DatabaseHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Ui.header(findViewById(R.id.header), prefs.getWarnaTema());
        LinearLayout ll = findViewById(R.id.llRiwayat);
        ll.removeAllViews();
        Cursor c = db.getRiwayat(0);
        while (c.moveToNext()) {
            ll.addView(Ui.baris(this, c.getString(0), c.getString(1), c.getLong(2),
                    "Setor".equals(c.getString(3)), c.getString(4)));
        }
        c.close();
        Nav.pasang(this, Nav.RIWAYAT);
    }
}
