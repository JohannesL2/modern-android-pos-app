#include <jni.h>
#include "escpos_engine.h"
#include <span>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_test_1design_printer_PrinterBridge_buildPrintJob(
        JNIEnv* env,
        jobject,
        jintArray pixels,
        jint width,
        jint height
) {
    jint* raw = env->GetIntArrayElements(pixels, nullptr);
    jsize len = env->GetArrayLength(pixels);

    std::span<const int32_t> span(reinterpret_cast<const int32_t*>(raw), static_cast<size_t>(len));

    auto result = EscPosCore::prepareRasterData(span, width, height);

    env->ReleaseIntArrayElements(pixels, raw, JNI_ABORT);

    jbyteArray output = env->NewByteArray(static_cast<jsize>(result.size()));
    env->SetByteArrayRegion(output, 0, static_cast<jsize>(result.size()), reinterpret_cast<const jbyte*>(result.data()));

    return output;
}