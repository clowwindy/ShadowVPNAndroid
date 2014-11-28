#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#include <shadowvpn.h>

static int initialized = 0;
static int sock;
static vpn_ctx_t vpn_ctx;
shadowvpn_args_t args;

jint Java_org_shadowvpn_shadowvpn_ShadowVPN_nativeInitVPN(JNIEnv* env, jobject thiz,
                                                jint tun_fd, jstring password,
                                                jstring server, jint port,
                                                jint mtu) {
  bzero(&args, sizeof(args));
  if (!initialized) {
    crypto_init();
    initialized = 1;
  }
  const char *c_password = (*env)->GetStringUTFChars(env, password, NULL);
  if (0 != crypto_set_password(c_password, strlen(c_password))) {
    return -1;
  }
  args.password = c_password;
  args.server = (*env)->GetStringUTFChars(env, server, NULL);
  args.port = port;
  args.mtu = mtu;
  args.mode = SHADOWVPN_MODE_CLIENT;
  vpn_ctx_t *ctx = &vpn_ctx;
  bzero(ctx, sizeof(vpn_ctx_t));
  ctx->remote_addrp = (struct sockaddr *)&ctx->remote_addr;
  if (-1 == pipe(ctx->control_pipe)) {
    err("pipe");
    return -1;
  }
  ctx->tun = tun_fd;
  ctx->socks = &sock;
  ctx->nsock = 1;
  if (-1 == (sock = vpn_udp_alloc(args.mode == SHADOWVPN_MODE_SERVER,
                                  args.server, args.port,
                                  ctx->remote_addrp,
                                  &ctx->remote_addrlen))) {
    errf("failed to create UDP socket");
    close(ctx->tun);
    return -1;
  }
  ctx->args = &args;
  return 0;
}

jint Java_org_shadowvpn_shadowvpn_ShadowVPN_nativeRunVPN(JNIEnv* env, jobject thiz) {
  return vpn_run(&vpn_ctx);
}

jint Java_org_shadowvpn_shadowvpn_ShadowVPN_nativeStopVPN(JNIEnv* env, jobject thiz) {
  return vpn_stop(&vpn_ctx);
}

jint Java_org_shadowvpn_shadowvpn_ShadowVPN_nativeGetSockFd(JNIEnv* env, jobject thiz) {
  return sock;
}
