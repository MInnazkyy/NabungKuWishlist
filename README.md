# NabungKu Wishlist
Studi kasus SharedPreferences (Tugas P10 Pemrograman Mobile, UMKO) - Zaki Maulana Riyadi, 2459201129, 5D.

## Layar
Selamat datang (sekali saja) · Tarik dari amplop · Pengingat 19:00 · Beranda · Target dan amplop · Riwayat · Profil, plus dialog Setor (tombol + di tengah).

## Pembagian data
- SharedPreferences (PrefsManager): nama, mode gelap, sembunyikan saldo, pengingat, nominal setor cepat, warna tema, status onboarding, streak harian, jumlah dibuka
- SQLite (DatabaseHelper): target, transaksi, riwayat; saldo = SUM dari SQLite

## Cara pakai
1. Android Studio > Open folder ini, tunggu Gradle sync, Run (minSdk 24).
2. Ubah pengaturan di Profil > Simpan. Kembali ke Beranda: berubah (dibaca di onResume()).
3. Tombol setor cepat memakai nominal dari SharedPreferences.
4. Bukti: Force Stop, buka lagi, pengaturan tetap ada.
5. Reset: Clear data di pengaturan Android supaya layar Selamat datang muncul lagi dengan data kosong.

## Foto (v5)
- Foto impian per target (layar Target: ketuk kartu; atau saat Selamat datang) dan foto bukti setiap setoran (dialog Setor > Lampirkan bukti).
- Foto dipilih dari galeri, dikecilkan, lalu disalin ke penyimpanan internal app (files/foto). SQLite hanya menyimpan PATH (kolom target.foto, transaksi.bukti).
- Database naik ke versi 4: data lama ter-reset sekali.

## Tampilan (v6)
Ikon vektor di bottom navigation, progress bar beranimasi, kartu berbayangan, tombol berwarna tema. Tidak ada data bawaan: nama impian, sasaran, dan foto diisi sendiri oleh pengguna di layar Selamat datang atau tab Target.

## Perbaikan (v8)
- NIM di dokumen diperbaiki menjadi 2459201129.
- Jumlah "dibuka" dan streak hanya bertambah saat app benar-benar dibuka, bukan saat layar dibuat ulang (ganti tema, setor, rotasi).
- Nominal setor cepat harus lebih dari 0.
- Pengingat 19:00 dijadwalkan ulang otomatis setelah HP restart (BootReceiver).
- Warna chip "minggu ini" di Beranda mengikuti warna tema.

## Kosong + hapus target (v7)
- Tidak ada data bawaan; nama impian dan sasaran (minimal Rp 1.000) wajib diisi pengguna.
- Tab Target: ketuk kartu > Ubah / Hapus. Hapus target ikut menghapus transaksi dan file fotonya.
- Persen progres dibatasi maksimal 100%.
