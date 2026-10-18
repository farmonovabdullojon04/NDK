<div align="center">

# ⚡ NDK Grayscale Benchmark

**Kotlin vs C++ (Android NDK) — qaysi biri tezroq?**
Rasmni grayscale ga o'tkazish orqali ikkala yondashuvni real vaqtda o'lchaydigan Android ilova.

<br>

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![C++](https://img.shields.io/badge/C++-00599C?style=for-the-badge&logo=cplusplus&logoColor=white)
![CMake](https://img.shields.io/badge/CMake-064F8C?style=for-the-badge&logo=cmake&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

![minSdk](https://img.shields.io/badge/minSdk-24-blue?style=flat-square)
![targetSdk](https://img.shields.io/badge/targetSdk-37-blue?style=flat-square)
![NDK](https://img.shields.io/badge/NDK-JNI-orange?style=flat-square)
![Status](https://img.shields.io/badge/status-active-success?style=flat-square)

[Imkoniyatlar](#-imkoniyatlar) •
[Qanday ishlaydi](#-qanday-ishlaydi) •
[Ishga tushirish](#-ishga-tushirish) •
[Tuzilma](#-loyiha-tuzilmasi) •
[Nimalarni o'rgandim](#-nimalarni-organdim)

</div>

---

## 🎯 Loyiha haqida

Bu loyiha **Android NDK** va **JNI** bilan amaliy ishlashni ko'rsatish uchun yaratilgan. Bitta rasm ustida bir xil algoritm ikki usulda bajariladi va har birining vaqti ms da ko'rsatiladi:

| | 🟣 Kotlin | 🔵 C++ (NDK) |
|---|---|---|
| **Kirish usuli** | `Bitmap.getPixels()` / `setPixels()` | `AndroidBitmap_lockPixels()` |
| **Xotira** | Qator-qator nusxalash | To'g'ridan-to'g'ri piksel buferi |
| **Fayl** | `MainActivity.kt` | `native-lib.cpp` |

## 🎯 Skrinshot
<img width="720" height="1560" alt="image" src="https://github.com/user-attachments/assets/d00e2428-a9b3-4ee6-a084-500a698e5f00" />


## ✨ Imkoniyatlar

- 🖼️ **3 ta rasm** `RecyclerView` ro'yxatida
- ⚔️ Har bir rasm uchun **Kotlin** va **C++** tugmalari yonma-yon
- ⏱️ Aniq **vaqt o'lchash** (`measureTimeMillis`)
- 🔄 **Qayta tiklash** — barcha natijalarni bir bosishda asl holatga qaytarish
- 🧠 **Xotira tejash** — `inSampleSize` bilan ~1080×1080 gacha kichraytirib yuklash
- 🛡️ `OutOfMemoryError` va boshqa xatoliklardan himoya

## 🔬 Qanday ishlaydi

```mermaid
flowchart LR
    A[🖼️ Rasm] --> B{Tugma}
    B -->|Kotlin| C[getPixels → hisoblash → setPixels]
    B -->|C++| D[JNI → lockPixels → hisoblash → unlockPixels]
    C --> E[⏱️ Vaqt + natija]
    D --> E
```

Grayscale yorqinlik (luminance) formulasi bo'yicha hisoblanadi, alfa kanal saqlanadi:

```
gray = 0.299 · R + 0.587 · G + 0.114 · B
```

<details>
<summary><b>🔗 JNI ulanishi (kodni ko'rish)</b></summary>

**Kotlin:**
```kotlin
external fun convertToGrayNative(bitmap: Bitmap)

companion object {
    init { System.loadLibrary("ndk") }
}
```

**C++:**
```cpp
extern "C" JNIEXPORT void JNICALL
Java_com_abdullojon_ndk_MainActivity_convertToGrayNative(JNIEnv* env, jobject, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels;
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    // piksellarni qayta ishlash...
    AndroidBitmap_unlockPixels(env, bitmap);
}
```

</details>

## 🚀 Ishga tushirish

### Talablar

- Android Studio (so'nggi barqaror versiya)
- **NDK (Side by side)** va **CMake 3.22.1** — *SDK Manager → SDK Tools*
- JDK 11+
- Android 7.0+ (API 24) qurilma yoki emulyator

### O'rnatish

```bash
git clone https://github.com/<username>/<repo>.git
cd <repo>
./gradlew assembleDebug
```

Yoki Android Studio da: **File → Open** → loyihani tanlang → **Run ▶**

## 📱 Foydalanish

1. Ilovani oching va rasmlar ro'yxatini ko'ring
2. **Kotlin** yoki **C++** tugmasini bosing
3. Grayscale natija va `TIME: N ms` ni solishtiring
4. Yuqori o'ngdagi **Qayta tiklash** bilan qaytadan boshlang

<!-- 📸 Skrinshot qo'shing:
<p align="center">
  <img src="docs/screenshot.png" width="300" alt="Ilova skrinshoti">
</p>
-->

## 📁 Loyiha tuzilmasi

```
NDK/
├── app/src/main/
│   ├── cpp/
│   │   ├── native-lib.cpp        # C++ grayscale (JNI)
│   │   └── CMakeLists.txt        # libndk.so build
│   ├── java/com/abdullojon/ndk/
│   │   └── MainActivity.kt       # UI + Kotlin grayscale + JNI chaqiruvi
│   └── res/                      # layout, menyu, rasmlar, mavzular
├── build.gradle.kts
└── settings.gradle.kts
```

## 🧰 Texnologiyalar

| Soha | Texnologiya |
|---|---|
| Til | Kotlin, C++ |
| Native | Android NDK, JNI, `jnigraphics` |
| Build | Gradle (Kotlin DSL), CMake 3.22.1 |
| UI | RecyclerView, ViewBinding, Material Components, ConstraintLayout |

## 🎓 Nimalarni o'rgandim

- [x] **Android NDK** va **CMake** bilan native kutubxona (`libndk.so`) yaratish
- [x] **JNI** orqali Kotlin ↔ C++ o'rtasida ma'lumot almashish
- [x] `jnigraphics` bilan `Bitmap` piksellariga native darajada kirish
- [x] Kotlin va C++ samaradorligini `measureTimeMillis` bilan o'lchash
- [x] Katta rasmlarda xotirani boshqarish (`inSampleSize`, `OutOfMemoryError`)
- [x] `RecyclerView`, `ViewBinding` va Material Components bilan UI

## 👤 Muallif

**Abdullojon Farmonov**

**farmonovabdullojon04@gmail.com**

<div align="center">

⭐ Loyiha foydali bo'lsa, yulduzcha qo'yishni unutmang!

</div>
