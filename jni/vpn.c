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
                                                jint tun_fd, jstring password, jstring user_token,
                                                jstring server, jint port,
                                                jint mtu, jint concurrency) {
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
  args.concurrency = concurrency;
  args.mode = SHADOWVPN_MODE_CLIENT;

  const char *c_user_token = (*env)->GetStringUTFChars(env, user_token, NULL);
  parse_user_tokens(&args, strdup(c_user_token));

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

int parse_user_tokens(shadowvpn_args_t *args, char *value) {
  char *sp_pos;
  char *start = value;

  if (value == NULL || strlen(value) == 0) {
    return 0;
  }

  args->user_tokens_len = 1;
  args->user_tokens = calloc(1, 8);
  bzero(args->user_tokens, 8);

  int p = 0;
  while (*value && p < 8) {
    unsigned int temp;
    int r = sscanf(value, "%2x", &temp);
    if (r > 0) {
      args->user_tokens[0][p] = temp;
      value += 2;
      p ++;
    } else {
      break;
    }
  }
  free(start);
  return 0;
}
