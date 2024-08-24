// yuv2rgb-jni.cc
#include <jni.h>
#include <android/log.h>

#include "yuv2rgb.h"

extern "C" {

jboolean
Java_software_enginer_litterallyless_util_NativeYuvConvertor_yuv420toArgbNative(
        JNIEnv* env, jclass clazz, jint width, jint height, jobject y_byte_buffer,
        jobject u_byte_buffer, jobject v_byte_buffer, jint y_pixel_stride,
        jint uv_pixel_stride, jint y_row_stride, jint uv_row_stride,
        jintArray argb_array) {
    auto y_buffer = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(y_byte_buffer));
    auto u_buffer = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(u_byte_buffer));
    auto v_buffer = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(v_byte_buffer));
    jint* argb_result_array = env->GetIntArrayElements(argb_array, nullptr);
    if (argb_result_array == nullptr || y_buffer == nullptr || u_buffer == nullptr
        || v_buffer == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "YUV2NATIVE",
                            "[yuv420toArgbNative] One or more inputs are null.");
        return false;
    }

    MyProject::Yuv2Rgb(width, height, reinterpret_cast<const uint8_t*>(y_buffer),
            reinterpret_cast<const uint8_t*>(u_buffer),
            reinterpret_cast<const uint8_t*>(v_buffer),
            y_pixel_stride, uv_pixel_stride, y_row_stride, uv_row_stride,
            argb_result_array);
    return true;
}

}