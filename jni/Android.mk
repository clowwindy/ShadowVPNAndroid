
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := sodium-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libsodium.a

include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE    := shadowvpn-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libshadowvpn.a

include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE    := vpn
LOCAL_SRC_FILES := vpn.c
LOCAL_CFLAGS := -IShadowVPN/shadowvpn-android-armv7/include/ -IShadowVPN/src/
LOCAL_STATIC_LIBRARIES := shadowvpn-prebuilt sodium-prebuilt
include $(BUILD_SHARED_LIBRARY)
