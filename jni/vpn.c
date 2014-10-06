#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#include <shadowvpn.h>
#include <sys/types.h>
#include <sys/select.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <net/if.h>
#include <linux/if_tun.h>
#include "shadowvpn.h"

#undef err
#undef errf

#define errf(s...) \
    __android_log_print(ANDROID_LOG_ERROR, "ShadowVPNJNI", s)

#define err(s) \
    __android_log_print(ANDROID_LOG_ERROR, "ShadowVPNJNI", "%s: %s", s, strerror(errno))

static int running = 0;
static int control_pipe[2];
static int sock = 0;

static int j_vpn_get_sock_fd() {
  return sock;
}

static int tun_alloc(const char *dev) {
  struct ifreq ifr;
  int fd, e;

  if ((fd = open("/dev/net/tun", O_RDWR)) < 0) {
    err("open");
    errf("can not open /dev/net/tun");
    return -1;
  }

  memset(&ifr, 0, sizeof(ifr));

  /* Flags: IFF_TUN   - TUN device (no Ethernet headers) 
   *        IFF_TAP   - TAP device  
   *
   *        IFF_NO_PI - Do not provide packet information  
   */ 
  ifr.ifr_flags = IFF_TUN | IFF_NO_PI; 
  if(*dev)
    strncpy(ifr.ifr_name, dev, IFNAMSIZ);

  if((e = ioctl(fd, TUNSETIFF, (void *) &ifr)) < 0){
    err("ioctl");
    errf("can not setup tun device: %s", dev);
    close(fd);
    return -1;
  }
  // strcpy(dev, ifr.ifr_name);
  return fd;
}

static int udp_alloc(int if_bind, const char *host, int port,
                     struct sockaddr *addr, socklen_t* addrlen) {
  struct addrinfo hints;
  struct addrinfo *res;
  int r, flags;

  memset(&hints, 0, sizeof(hints));
  hints.ai_socktype = SOCK_DGRAM;
  hints.ai_protocol = IPPROTO_UDP;
  if (0 != (r = getaddrinfo(host, NULL, &hints, &res))) {
    errf("getaddrinfo: %s", gai_strerror(r));
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "getaddrinfo");
    return -1;
  }

  if (res->ai_family == AF_INET)
    ((struct sockaddr_in *)res->ai_addr)->sin_port = htons(port);
  else if (res->ai_family == AF_INET6)
    ((struct sockaddr_in6 *)res->ai_addr)->sin6_port = htons(port);
  else {
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "unknown ai_family");
    errf("unknown ai_family %d", res->ai_family);
    return -1;
  }
  memcpy(addr, res->ai_addr, res->ai_addrlen); 
  *addrlen = res->ai_addrlen;

  if (-1 == (sock = socket(res->ai_family, SOCK_DGRAM, IPPROTO_UDP))) {
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "socket");
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "%s: %s", "socket", strerror(errno));
    err("socket");
    errf("can not create socket");
    return -1;
  }

  if (if_bind) {
    if (0 != bind(sock, res->ai_addr, res->ai_addrlen)) {
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "bind");
      err("bind");
      errf("can not bind %s:%d", host, port);
      return -1; 
    }
    freeaddrinfo(res);
  }
  flags = fcntl(sock, F_GETFL, 0);
  if (flags != -1) {
    if (-1 != fcntl(sock, F_SETFL, flags | O_NONBLOCK))
      return sock;
  }
  close(sock);
  err("fcntl");
    __android_log_print(ANDROID_LOG_INFO, "JNIMsg", "fcntl");
  return -1;
}


static int j_run_vpn(shadowvpn_args_t *args) {
  return run_vpn_with_tun_fd(args, 0);
}

static int j_run_vpn_with_tun_fd(shadowvpn_args_t *args, int user_tun_fd) {
  fd_set readset;
  int tun, sock, max_fd;
  ssize_t r;
  unsigned char *tun_buf;
  unsigned char *udp_buf;
  struct sockaddr_storage remote_addr;
  struct sockaddr *remote_addrp = (struct sockaddr *)&remote_addr;
  socklen_t remote_addrlen;

  if (running) {
    errf("can not start, already running");
    return -2;
  }

  if (-1 == pipe(control_pipe)) {
    err("pipe");
    return -3;
  }

  if (user_tun_fd == 0) {
    if (-1 == (tun = tun_alloc(args->intf))) {
      errf("failed to create tun device");
      return -4;
    }
  } else {
    tun = user_tun_fd;
  }
  if (-1 == (sock = udp_alloc(args->mode == SHADOWVPN_MODE_SERVER,
                              args->server, args->port,
                              remote_addrp, &remote_addrlen))) {
    errf("failed to create UDP socket");
    close(tun);
    return -1;
  }

  running = 1;

  // protect the socket first; send data later
  // this is stupid; we should do it better
  sleep(3);
  // shell_up(args);

  tun_buf = malloc(args->mtu + SHADOWVPN_ZERO_BYTES);
  udp_buf = malloc(args->mtu + SHADOWVPN_ZERO_BYTES);
  memset(tun_buf, 0, SHADOWVPN_ZERO_BYTES);
  memset(udp_buf, 0, SHADOWVPN_ZERO_BYTES);

  logf("VPN started");

  while (running) {
    FD_ZERO(&readset);
    FD_SET(control_pipe[0], &readset);
    FD_SET(tun, &readset);
    FD_SET(sock, &readset);

    // we assume that pipe fd is always less than tun and sock fd which are
    // created later
    max_fd = max(tun, sock) + 1;

    if (-1 == select(max_fd, &readset, NULL, NULL, NULL)) {
      if (errno == EINTR)
        continue;
      err("select");
      break;
    }
    if (FD_ISSET(control_pipe[0], &readset)) {
      char pipe_buf;
      (void)read(control_pipe[0], &pipe_buf, 1);
      break;
    }
    if (FD_ISSET(tun, &readset)) {
      errf("tun set");
      r = read(tun, tun_buf + SHADOWVPN_ZERO_BYTES, args->mtu); 
      if (r == -1) {
        if (errno == EAGAIN || errno == EWOULDBLOCK) {
          // do nothing
        } else if (errno == EPERM || errno == EINTR) {
          // just log, do nothing
          err("read from tun");
        } else {
          err("read from tun");
          break;
        }
      }
      if (remote_addrlen) {
        crypto_encrypt(udp_buf, tun_buf, r);
        r = sendto(sock, udp_buf + SHADOWVPN_PACKET_OFFSET,
                   SHADOWVPN_OVERHEAD_LEN + r, 0,
                   remote_addrp, remote_addrlen);
        if (r == -1) {
          if (errno == EAGAIN || errno == EWOULDBLOCK) {
            // do nothing
          } else if (errno == ENETUNREACH || errno == ENETDOWN ||
                     errno == EPERM || errno == EINTR || errno == EMSGSIZE) {
            // just log, do nothing
            err("sendto");
          } else {
            err("sendto");
            // TODO rebuild socket
            break;
          }
        }
      }
    }
    if (FD_ISSET(sock, &readset)) {
      errf("sock set");
      // only change remote addr if decryption succeeds
      struct sockaddr_storage temp_remote_addr;
      socklen_t temp_remote_addrlen = sizeof(temp_remote_addr);
      r = recvfrom(sock, udp_buf + SHADOWVPN_PACKET_OFFSET,
                   SHADOWVPN_OVERHEAD_LEN + args->mtu, 0,
                   (struct sockaddr *)&temp_remote_addr,
                   &temp_remote_addrlen);
      if (r == -1) {
        if (errno == EAGAIN || errno == EWOULDBLOCK) {
          // do nothing
        } else if (errno == ENETUNREACH || errno == ENETDOWN ||
                    errno == EPERM || errno == EINTR) {
          // just log, do nothing
          err("recvfrom");
        } else {
          err("recvfrom");
          // TODO rebuild socket
          break;
        }
      }
      if (r == 0)
        continue;

      if (-1 == crypto_decrypt(tun_buf, udp_buf,
                               r - SHADOWVPN_OVERHEAD_LEN)) {
        errf("dropping invalid packet, maybe wrong password");
      } else {
        if (args->mode == SHADOWVPN_MODE_SERVER) {
          // if we are running a server, update server address from recv_from
          memcpy(remote_addrp, &temp_remote_addr, temp_remote_addrlen);
          remote_addrlen = temp_remote_addrlen;
        }

        if (-1 == write(tun, tun_buf + SHADOWVPN_ZERO_BYTES,
              r - SHADOWVPN_OVERHEAD_LEN)) {
          if (errno == EAGAIN || errno == EWOULDBLOCK) {
            // do nothing
          } else if (errno == EPERM || errno == EINTR || errno == EINVAL) {
            // just log, do nothing
            err("write to tun");
          } else {
            err("write to tun");
            break;
          }
        }
      }
    }
  }
  return -7;
  free(tun_buf);
  free(udp_buf);

  shell_down(args);

  close(tun);
  close(sock);

  running = 0;
  return -6;
}

static int j_stop_vpn() {
  logf("shutting down by user");
  if (!running) {
    errf("can not stop, not running");
    return -1;
  }
  running = 0;
  char buf = 0;
  if (-1 == write(control_pipe[1], &buf, 1)) {
    err("write");
    return -1;
  }
  return 0;
}

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
  args.mode = SHADOWVPN_MODE_CLIENT;
  r = j_run_vpn_with_tun_fd(&args, tun_fd);
  return r;
}

jint Java_clowwindy_shadowvpn_VPN_nativeStopVPN(JNIEnv* env, jobject thiz) {
  int r = j_stop_vpn();
  return r;
}

jint Java_clowwindy_shadowvpn_VPN_nativeGetSockFd(JNIEnv* env, jobject thiz) {
  return j_vpn_get_sock_fd();
}
