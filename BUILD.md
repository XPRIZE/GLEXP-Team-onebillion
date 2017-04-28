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

3. Configure the `ASSETS_FOLDER` environment variable, download and extract the [`assets.tar.gz`](https://xprizefoundation.box.com/s/xwqy6595mvxlmtluof5z50m87lhqe7c1) file from onebillion's Box account `One Billion - 16/Supplemental/Content/assets.tar.gz`:

        export ASSETS_FOLDER=$ROOT_FOLDER/app/src/main/assets
        
        mkdir $ASSETS_FOLDER
        
        tar xf assets.tar.gz -C $ASSETS_FOLDER

4. Configure the `ANDROID_HOME` environment variable based on the location of the Android SDK:

        export ANDROID_HOME=[PATH/TO/ANDROID_SDK]
        
5. In the `ROOT_FOLDER` build `gradle` tasks

        ./gradlew tasks

6. Build the desired **onecourse** `.apk`:
 - onecourse demo for XPRIZE judges:

          ./gradlew assembleJudgeMenu

 - onecourse Swahili:

          ./gradlew assembleChildMenu

 - onecourse English:

          ./gradlew assembleChildMenu_enGB_
        



## 2. Building the Android filesystem images


1. Establish a Build environment for Android Open Source Project. [This guide](https://source.android.com/source/initializing.html) will show you how.

2. Download [ASOP source](https://source.android.com/source/downloading.html). When initializing your repo client, checkout branch `android-6.0.1_r63` (build `MXC89L`):
        
        repo init -u https://android.googlesource.com/platform/manifest -b android-6.0.1_r63

3. You will need to [download](https://developers.google.com/android/drivers) and install the required hardware-specific binaries:
 - [Google device drivers](https://dl.google.com/dl/android/aosp/google_devices-dragon-mxc89l-5452d463.tgz)
 - [Nvidia drivers](https://dl.google.com/dl/android/aosp/nvidia-dragon-mxc89l-7dd0c758.tgz)

4. Apply `onecourse-AOSP.patch` from the **onecourse source** repository to the AOSP source tree (current workign folder):

        git apply -v --index $ROOT_FOLDER/onecourse-AOSP.patch    
        git commit -m "Applied onecourse system modifications"


5. On building for the first time, clean the build folders. This is not required for subsequent builds:

        make clobber 
        
6. Copy the following files and folders from the **onecourse source** repository to your AOSP folder:

          rm -rf packages/apps/onebillion
          mkdir -p packages/apps/onebillion
          cp -r $ROOT_FOLDER/AOSP/frameworks/ .
          mkdir -p out/target/product/dragon/system/media/
          cp $ROOT_FOLDER/AOSP/bootanimation.zip out/target/product/dragon/system/media/
 
7. Copy the desired **onecourse** `.apk` and makefile from the **onecourse source** repository to your AOSP folder:
 - onecourse demo for XPRIZE judges:

          cp $ROOT_FOLDER/app/build/outputs/apk/app-judgeMenu-release.apk packages/apps/onebillion/
          cp $ROOT_FOLDER/AOSP/judgeMenu-Android.mk packages/apps/onebillion/Android.mk

 - onecourse Swahili:

          cp $ROOT_FOLDER/app/build/outputs/apk/app-childMenu-release.apk packages/apps/onebillion/
          cp $ROOT_FOLDER/AOSP/childMenu-Android.mk packages/apps/onebillion/Android.mk

 - onecourse English:

          cp $ROOT_FOLDER/app/build/outputs/apk/app-childMenu_enGB_-release.apk packages/apps/onebillion/
          cp $ROOT_FOLDER/AOSP/childMenu_en_GB_-Android.mk packages/apps/onebillion/Android.mk
              
         
        
8. Build the filesystem images. This will take several hours:

        source build/envsetup.sh
        lunch aosp_dragon-userdebug
        make -j4

9. The filesytem images will be placed in:

        /out/target/product/dragon/

10. You can now [install onecourse onto a device](INSTALL.md).
