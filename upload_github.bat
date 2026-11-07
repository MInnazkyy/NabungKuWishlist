@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==============================================
echo   Upload NabungKu Wishlist ke GitHub
echo ==============================================
echo.

where git >nul 2>nul
if errorlevel 1 (
  echo Git BELUM terpasang. Pasang dulu dari https://git-scm.com lalu jalankan file ini lagi.
  pause
  exit /b
)

if not exist "app" (
  echo Folder "app" tidak ditemukan di sini.
  echo Pindahkan file ini ke folder NabungKuWishlist yang berisi app dan gradle, lalu jalankan lagi.
  pause
  exit /b
)

set /p EMAIL=Ketik email akun GitHub Anda lalu tekan Enter: 
git config --global user.name "Zaki Maulana Riyadi"
git config --global user.email "%EMAIL%"

(
echo .gradle/
echo .idea/
echo local.properties
echo build/
echo *.iml
echo *.apk
echo captures/
) > .gitignore

if exist ".git" rmdir /s /q ".git"
git init
git branch -M main
git add .
git commit -m "Upload proyek NabungKu Wishlist"
git remote add origin https://github.com/MInnazkyy/NabungKuWishlist.git
echo.
echo Sebentar lagi jendela browser untuk login GitHub mungkin muncul. Klik Authorize.
git push -u origin main --force

echo.
echo ==============================================
echo Selesai. Kalau tidak ada tulisan error merah di atas, buka:
echo https://github.com/MInnazkyy/NabungKuWishlist
echo ==============================================
pause
