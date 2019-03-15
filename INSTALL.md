# Installing onecourse

## Overview
Each of the three versions of **onecourse** has as a set of Android filesytem images for the Google Pixel C. Follow the steps belows to install **onecourse**. If your Pixel C is already unlocked, skip straight to [the flashing step](#flash-filesystem-images).

## Requirements
1. Ensure you have `adb` and `fastboot` installed on your computer. You can [follow the steps in this helpful guide](https://www.androidpit.com/how-to-install-adb-and-fastboot).
2. Connect your computer to the device via USB.


## Device preparation

##### Enable developer mode
1. Go to **Settings > About tablet**.
2. Tap **Build Number** 7 times until it displays _"You are now a developer"_.


##### Enable OEM Unlocking
1. Go to **Settings > Developer** options.
2. Enable OEM Unlocking.
3. Tap **Enable** when prompted by the disclaimer.

##### Enable USB Debugging
1. Go to **Settings > Developer** options.
2. Tap **Enable USB debugging**.
3. Tap **OK** when prompted by the disclaimer.
4. You should see dialog with you computer's _"RSA key fingerprint"_.
5. Tap _"Always allow from this computer"_.
6. Tap **OK**.

## Unlock the bootloader
1. On you computer, reboot the device into the bootloader by running:

        adb reboot-bootloader
 
2. The device will now boot into the bootloader and display _Waiting for fastboot command…_.

3. Unlock the bootloader by running:
 
        fastboot flashing unlock

4. The device will display a warning about unlocking the bootloader.
5. Press the **power** button on the device to unlock the bootloader.
6. The device will reboot into the bootloader and display _Waiting for fastboot command…_.
7. Reboot the device back into Android by running:
 
        fastboot reboot

8. Since your device will be reset, you will need to [enable USB debugging](#enable-usb-debugging) once more.



## Flash filesystem images

1. Download the set of filesystem images for the version of onecourse you wish to install:
 - onecourse Swahili [part 1](https://github.com/XPRIZE/GLEXP-Team-onebillion/releases/download/v3.0.0/onecourse-swahili-v3.0.0.tar.gz.aa), [part 2](https://github.com/XPRIZE/GLEXP-Team-onebillion/releases/download/v3.0.0/onecourse-swahili-v3.0.0.tar.gz.ab)
 - onecourse English [part 1](https://github.com/XPRIZE/GLEXP-Team-onebillion/releases/download/v3.0.0/onecourse-english-v3.0.0.tar.gz.aa), [part 2](https://github.com/XPRIZE/GLEXP-Team-onebillion/releases/download/v3.0.0/onecourse-english-v3.0.0.tar.gz.ab)

2. Reboot the device into the bootloader by running:
 
        adb reboot-bootloader
 
3. The device will now boot into the bootloader and display _Waiting for fastboot command…_.


4. On your computer, set the `ANDROID_PRODUCT_OUT` enviroment variable to the directory containing the filesytem images by running:
 
        ANDROID_PRODUCT_OUT=[PATH/TO/FILESYTEM_IMAGES]

5. Flash the device with the filesystem images by running:

        fastboot flash boot boot.img
        fastboot flash recovery recovery.img
        fastboot flash vendor vendor.img
        fastboot flash system system.img
        fastboot flash userdata userdata.img
        fastboot format cache
        fastboot reboot


6. After flashing is complete, the device will boot into **onecourse** setup. Follow the [setup instructions](SETUP.md).
