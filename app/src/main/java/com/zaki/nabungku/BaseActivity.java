package com.zaki.nabungku;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/** Induk semua layar: menyediakan pemilih foto (harus didaftarkan di onCreate). */
public abstract class BaseActivity extends AppCompatActivity {

    protected FotoPicker foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        foto = new FotoPicker(this);
    }
}
