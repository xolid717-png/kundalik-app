# Kundalik — Android ilova (Kotlin, Jetpack Compose)

Login+parol+captcha bilan kirib, kunlik baholar, davr o'rtachasi va dars
jadvalini ko'rsatadigan ilova. `backend/` papkasidagi API serverga ulanadi.

## Nima kerak

- [Android Studio](https://developer.android.com/studio) (bepul, so'nggi versiya)
- Backend server ishga tushirilgan va manzili (`https://...`) tayyor bo'lishi
  kerak — avval `backend/README.md`'ga qarang.

## Kompyuterisiz, faqat telefondan APK yasash (GitHub Actions orqali)

Kompyuter va Android Studio bo'lmasa ham, bu loyiha ichida
`.github/workflows/build.yml` fayli bor — GitHub'ga yuklasangiz, u
avtomatik ravishda bulutda (serverda) `.apk` faylni yasab beradi, siz
faqat tayyor faylni yuklab olasiz. Qadamlar:

1. **GitHub'da hisob oching** (bepul): telefon brauzerida github.com
   → Sign up.
2. **Termux**'ni o'rnating: Play Store'dagi eski versiya emas,
   [F-Droid](https://f-droid.org/packages/com.termux/) orqali eng
   so'nggisini oling (Play Store versiyasi endi yangilanmaydi).
3. Termux'ni oching va quyidagilarni ketma-ket yozing:
   ```
   pkg update -y
   pkg install git openssh unzip -y
   termux-setup-storage
   ```
   (Ruxsat so'ralsa — "Allow" bosing, bu Termux'ga telefon xotirasiga
   (Downloads papkasiga) kirish huquqini beradi.)
4. Bu ilova yuklab bergan `kundalik-android-app.zip` fayli odatda
   **Downloads** papkasida bo'ladi. Uni chiqaramiz:
   ```
   cd ~/storage/downloads
   unzip kundalik-android-app.zip -d kundalik_android
   cd kundalik_android
   ```
5. **BASE_URL'ni sozlang** (backend serveringiz manziliga) — Termux'da:
   ```
   pkg install nano -y
   nano app/src/main/java/uz/kundalik/app/ApiClient.kt
   ```
   `BASE_URL` qatorini toping, `https://SIZNING-SERVERINGIZ.uz` o'rniga
   haqiqiy manzilingizni yozing, so'ng `Ctrl+O` (saqlash) → `Enter` →
   `Ctrl+X` (chiqish).
6. GitHub'da yangi bo'sh repository yarating (github.com → "+" → "New
   repository", nomini masalan `kundalik-app` deb qo'ying, "Public"
   qoldiring, hech narsani belgilamasdan "Create" bosing).
7. GitHub sizdan parol so'rmaydi — **Personal Access Token** kerak:
   github.com → rasmingiz (yuqori o'ngda) → **Settings** →
   **Developer settings** → **Personal access tokens** →
   **Tokens (classic)** → **Generate new token** → "repo" belgisini
   belgilang → token yarating va **nusxalab oling** (bu faqat bir marta
   ko'rsatiladi!).
7. Termux'da (hali `kundalik_android` papkasida turib):
   ```
   git init
   git add .
   git commit -m "birinchi yuklash"
   git branch -M main
   git remote add origin https://github.com/FOYDALANUVCHI_NOMI/kundalik-app.git
   git push -u origin main
   ```
   `git push` so'ragan **username** o'rniga GitHub login'ingizni,
   **parol** o'rniga esa 7-qadamda olgan tokenni kiriting.
8. Push tugagach, github.com'da repositoriyangizga kiring → yuqoridagi
   **Actions** bo'limini oching — build avtomatik boshlanadi (2-4
   daqiqa davom etadi, sabr qiling).
9. Build yashil belgi bilan tugagach, o'sha sahifada pastroqda
   **Artifacts** bo'limida `kundalik-debug-apk` degan fayl chiqadi —
   shuni bosib yuklab oling, ichida `app-debug.apk` bor. Shu faylni
   telefoningizga o'rnatasiz (noma'lum manbalardan o'rnatishga ruxsat
   berish so'ralishi mumkin).

> Keyinchalik `ApiClient.kt`dagi `BASE_URL`ni o'zgartirsangiz, xuddi
> shu papkada `git add . && git commit -m "yangilash" && git push`
> qilsangiz bo'ldi — GitHub yana avtomatik yangi APK yasab beradi.

## Kompyuter bo'lsa: Android Studio orqali ochish va build qilish

1. Android Studio'ni oching → **Open** → shu `android_app` papkasini tanlang.
2. Birinchi ochishda Android Studio Gradle wrapper faylini o'zi yaratib/
   yuklab oladi (agar so'rasa — "OK"/"Sync Now" bosing). Internet kerak
   bo'ladi, chunki kutubxonalar (Compose, Retrofit va h.k.) yuklanadi.
3. `app/src/main/java/uz/kundalik/app/ApiClient.kt` faylini oching va
   `BASE_URL`ni o'z serveringiz manziliga almashtiring:
   ```kotlin
   const val BASE_URL = "https://api.sizningdomeningiz.uz"
   ```
4. Yuqorida **Sync Project with Gradle Files** (fil ikonkasi) bosing.
5. Telefoningizni USB orqali ulang (Developer mode + USB debugging yoqilgan
   holda) yoki emulyator yarating, so'ng **Run ▶** bosing — ilova
   qurilmangizda ochiladi.

## Haqiqiy .apk fayl olish

Android Studio menyusidan:

**Build → Build Bundle(s) / APK(s) → Build APK(s)**

Tugagach, pastda chiqqan bildirishnomadagi **locate** havolasini bosing —
`.apk` fayl shu yerda: `app/build/outputs/apk/debug/app-debug.apk`

Bu faylni istalgan Android telefonga o'tkazib o'rnatishingiz mumkin
(noma'lum manbalardan o'rnatishga ruxsat berish kerak bo'lishi mumkin).

> Play Store'ga chiqarish uchun esa **imzolangan (signed) release APK/AAB**
> kerak bo'ladi — buni ham shu menyudan (**Build → Generate Signed Bundle
> / APK**) qilish mumkin, lekin bu odatda faqat rasman tarqatish uchun
> kerak, shaxsiy foydalanish uchun oddiy debug APK yetarli.

## Loyiha tuzilishi

```
app/src/main/java/uz/kundalik/app/
  MainActivity.kt       - navigatsiya (login -> captcha -> dashboard)
  ApiClient.kt           - backend bilan gaplashuvchi HTTP klient
  Models.kt               - JSON data klasslari
  TokenStore.kt           - login tokenini shifrlangan saqlash
  screens/
    LoginScreen.kt        - login/parol kiritish
    CaptchaScreen.kt      - captcha rasm + kod kiritish
    DashboardScreen.kt    - kunlik baholar / davr o'rtachasi / dars jadvali
```

## Xavfsizlik haqida eslatma

- Ilova login/parolni telefonda saqlamaydi — faqat backend bergan tokenni
  (shifrlangan holda, Android Keystore orqali) saqlaydi.
- `BASE_URL` **albatta `https://`** bo'lishi kerak — aks holda login/parol
  ochiq tarmoqdan o'qilib qolishi mumkin.
