#include <android/native_activity.h>
#include <android/native_window.h>
#include <jni.h>
#include <stddef.h>
#include <stdint.h>

static void paint_window(ANativeActivity *activity, ANativeWindow *window) {
    (void)activity;

    ANativeWindow_setBuffersGeometry(window, 0, 0, WINDOW_FORMAT_RGBX_8888);

    ANativeWindow_Buffer buffer;
    if (ANativeWindow_lock(window, &buffer, NULL) != 0) {
        return;
    }

    const uint32_t dark_red = UINT32_C(0x00201030);
    uint32_t *pixels = buffer.bits;
    for (int32_t y = 0; y < buffer.height; ++y) {
        uint32_t *row = pixels + ((size_t)y * (size_t)buffer.stride);
        for (int32_t x = 0; x < buffer.width; ++x) {
            row[x] = dark_red;
        }
    }

    ANativeWindow_unlockAndPost(window);
}

JNIEXPORT void ANativeActivity_onCreate(
    ANativeActivity *activity,
    void *saved_state,
    size_t saved_state_size
) {
    (void)saved_state;
    (void)saved_state_size;
    activity->callbacks->onNativeWindowCreated = paint_window;
}
