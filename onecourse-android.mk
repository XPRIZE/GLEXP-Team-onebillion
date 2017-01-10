LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := onebillion
LOCAL_MODULE_CLASS := APPS
LOCAL_PRIVILEGED_MODULE := true
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_PATH := $(TARGET_OUT)/priv-app
LOCAL_SRC_FILES := (APP).apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

$(shell rm -rf $(LOCAL_MODULE_PATH)/onebillion/)
$(shell mkdir -p $(LOCAL_MODULE_PATH)/onebillion/)
$(shell cp -rf $(LOCAL_PATH)/assets.obb `pwd`/$(LOCAL_MODULE_PATH)/onebillion)

include $(BUILD_PREBUILT)
