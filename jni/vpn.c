#include <string.h>
#include <stdio.h>
#include <jni.h>

jstring
Java_clowwindy_shadowvpn_MainActivity_stringFromJNI( JNIEnv* env,
                                                     jobject thiz )
{
    int r = crypto_init();
    char buf[16];
    snprintf(buf, 16, "%d\n", r);
    return (*env)->NewStringUTF(env, buf);
}
