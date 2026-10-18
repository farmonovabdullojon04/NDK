#include <jni.h>
#include <android/bitmap.h>

extern "C" JNIEXPORT void JNICALL
Java_com_abdullojon_ndk_MainActivity_convertToGrayNative(JNIEnv* env, jobject /* this */, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    for (int y = 0; y < info.height; y++) {
        uint32_t* line = (uint32_t*)((char*)pixels + y * info.stride);
        for (int x = 0; x < info.width; x++) {
            uint32_t pixel = line[x];

            int r = pixel & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = (pixel >> 16) & 0xff;
            int a = (pixel >> 24) & 0xff;

            int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

            line[x] = (a << 24) | (gray << 16) | (gray << 8) | gray;
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}