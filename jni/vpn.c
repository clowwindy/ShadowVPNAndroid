#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <shadowvpn.h>

static int initialized = 0;

jint Java_clowwindy_shadowvpn_VPN_nativeRunVPN(JNIEnv* env,
                                         jobject thiz,
                                         jint tun_fd,
                                         jstring password,
                                         jstring server,
                                         jint port,
                                         jint mtu
                                         ) {
  int r;
  shadowvpn_args_t args;
  bzero(&args, sizeof(args));
  if (!initialized) {
    crypto_init();
    initialized = 1;
  }
  const char *c_password = (*env)->GetStringUTFChars(env, password, NULL);
  if (0 != crypto_set_password(c_password, strlen(c_password))) {
    // TODO throw Exception instead
    return -1;
  }
  args.password = c_password;
  args.server = (*env)->GetStringUTFChars(env, server, NULL);
  args.port = port;
  args.mtu = mtu;
  r = run_vpn_with_tun_fd(&args, tun_fd);
  return r;
}

jint Java_clowwindy_shadowvpn_VPN_nativeStopVPN(JNIEnv* env, jobject thiz) {
  int r = stop_vpn();
  return r;
}

jint Java_clowwindy_shadowvpn_VPN_nativeGetSockFd(JNIEnv* env, jobject thiz) {
  return vpn_get_sock_fd();
}
