#include <string.h>
#include <stdio.h>
#include <jni.h>
#include <shadowvpn.h>

static int initialized = 0;

jint Java_clowwindy_shadowvpn_VPN_runVPN( JNIEnv* env, jobject thiz ) {
  int r;
  if (!initialized) {
    crypto_init();
  }
  return r;
}

jint Java_clowwindy_shadowvpn_VPN_stopVPN( JNIEnv* env, jobject thiz ) {
  int r = stop_vpn();
  return r;
}
