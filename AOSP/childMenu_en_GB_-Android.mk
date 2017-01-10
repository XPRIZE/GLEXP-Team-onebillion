LOCAL_PATH:= $(call my-dir)

########################################
# NCI Configuration
########################################
include $(CLEAR_VARS)

# $(shell mkdir -p $(TARGET_OUT_DATA)/onebillion/)
# $(shell cp -rf $(LOCAL_PATH)/assets.obb `pwd`/$(TARGET_OUT_DATA)/onebillion)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := onebillion
LOCAL_MODULE_CLASS := APPS
LOCAL_PRIVILEGED_MODULE := true
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_PATH := $(TARGET_OUT)/priv-app
LOCAL_SRC_FILES := app-childMenu_enGB_-release.apk
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

$(shell rm -rf $(LOCAL_MODULE_PATH)/onebillion/)
$(shell mkdir -p $(LOCAL_MODULE_PATH)/onebillion/)
$(shell cp -rf $(LOCAL_PATH)/assets.obb `pwd`/$(LOCAL_MODULE_PATH)/onebillion)

# LOCAL_MODULE_PATH := $(TARGET_OUT_DATA)

include $(BUILD_PREBUILT)
