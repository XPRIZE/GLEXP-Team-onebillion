# Building onecourse

## Overview
Building **onecourse** is a two-step process:


1. Compiling the desired version of the **onecouse** app.
2. Building [AOSP](https://source.android.com) for the Pixel C, integrating the **onecourse** app and generating a set of filesystem images.




## 1. Building the onecourse app

1. Ensure you have **Android Studio** installed. You can [follow the steps in this helpful guide](https://developer.android.com/studio/install.html).

2. Configure the `ROOT_FOLDER` environment variable and clone the **onecourse source** repository:

        export ROOT_FOLDER=onecourse
        
        git clone https://github.com/XPRIZE/GLEXP-Team-onebillion.git $ROOT_FOLDER/

3. Configure the `ANDROID_HOME` environment variable based on the location of the Android SDK:

        export ANDROID_HOME=[PATH/TO/ANDROID_SDK]
        
4. In the `ROOT_FOLDER` build `gradle` tasks

        ./gradlew tasks

5. Build the desired **onecourse** `.apk`:

	onecourse Swahili:

          ./gradlew assembleSw_community_release -Pandroid.injected.signing.store.file=$ROOT_FOLDER/platform.keystore -Pandroid.injected.signing.store.password=android -Pandroid.injected.signing.key.alias=onebillion_platform -Pandroid.injected.signing.key.password=android

	onecourse English:

          ./gradlew assembleEnGB_community_release -Pandroid.injected.signing.store.file=$ROOT_FOLDER/platform.keystore -Pandroid.injected.signing.store.password=android -Pandroid.injected.signing.key.alias=onebillion_platform -Pandroid.injected.signing.key.password=android
        


## 2. Building the Android filesystem images


1. Establish a Build environment for Android Open Source Project. [This guide](https://source.android.com/source/initializing.html) will show you how.

2. Download [ASOP source](https://source.android.com/source/downloading.html). When initializing your repo client, checkout branch `android-6.0.1_r63` (build `MXC89L`):
        
        repo init -u https://android.googlesource.com/platform/manifest -b android-6.0.1_r63

3. You will need to [download](https://developers.google.com/android/drivers) and install the required hardware-specific binaries:
 - [Google device drivers](https://dl.google.com/dl/android/aosp/google_devices-dragon-mxc89l-5452d463.tgz)
 - [Nvidia drivers](https://dl.google.com/dl/android/aosp/nvidia-dragon-mxc89l-7dd0c758.tgz)

4. Apply the `AOSP/onecourse-AOSP.patch` from the **onecourse source** repository to the AOSP source tree (current working folder).


5. On building for the first time, clean the build folders. This is not required for subsequent builds:

        make clobber 
        
6. Copy the following files and folders from the **onecourse source** repository to your AOSP folder:

          rm -rf packages/apps/onebillion
          mkdir -p packages/apps/onebillion
          cp $ROOT_FOLDER/AOSP/bootanimation.zip packages/apps/onebillion/
          

 
7. Copy the makefile from the **onecourse source** repository to your AOSP folder:

			cp $ROOT_FOLDER/AOSP/Android.mk packages/apps/onebillion/Android.mk
              
              
8. Copy the the desired **onecourse** `.apk` to your AOSP folder:

	onecourse Swahili:

          cp $ROOT_FOLDER/app/build/outputs/apk/sw_community_/release/app-sw_community_-release.apk packages/apps/onebillion/app-release.apk

	onecourse English:

          cp $ROOT_FOLDER/app/build/outputs/apk/enGB_community_/release/app-enGB_community_-release.apk packages/apps/onebillion/app-release.apk
         
         
9. Download and extract the `assets.tar.gz` file from onebillion's Box account and extract into your AOSP folder:

        
        tar xf assets.tar.gz -C packages/apps/onebillion/

        
10. Build the filesystem images. This will take several hours:

        source build/envsetup.sh
        lunch aosp_dragon-userdebug
        make -j4

11. The filesytem images will be placed in:

        /out/target/product/dragon/

12. You can now [install onecourse onto a device](INSTALL.md).
