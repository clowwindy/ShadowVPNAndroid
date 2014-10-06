
#include <string.h>
#include <stdio.h>
#include <jni.h>

int crypto_init();

int Java_clowwindy_shadowvpn_VPN_runVPN( JNIEnv* env, jobject thiz ) {
    int r = crypto_init();
    return r;
}

int Java_clowwindy_shadowvpn_VPN_stopVPN( JNIEnv* env, jobject thiz ) {
    int r = crypto_init();
    return r;
}