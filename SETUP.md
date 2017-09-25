#onecourse installation and setup

##Installation
To flash a device with the onecourse filesystem images, run the following commands from the directory with the images:

```
adb reboot bootloader
fastboot flash boot boot.img
fastboot flash recovery recovery.img
fastboot flash vendor vendor.img
fastboot flash system system.img
fastboot flash userdata userdata.img
fastboot flash cache cache.img
fastboot reboot
```



## Setup
After installation, the device will boot into a _setup screen_. this only needs to be done once for each tablet. ![](https://onebillion.org/img/xprize/setup-ss/screen1.png) Follow the 3 steps:

1. Check the installation was successfull. Tap the **Watch video** button to watch our short 'tablet care' video. Tap **Test onecourse** to try a random 'learning unit'. If neither load, there was an issue with the installation. Please reinstall the filesystem images and try setup again.

2. Check the current date and time of the device is correct. It will attempt to use the `ntp` server running on the `XPRIZE` Wi-Fi network to get the time and date and if successfull will display _Synced from time server_ below. If either the date or time is incorrect, please set them both manually by tapping **Set date and time**

3. Set the field trial start date. Tap **Set trial start date** to select the date in late November 2017 when the tablets will first be used by children in their villages: ![](https://onebillion.org/img/xprize/setup-ss/screen2.png) Confirm the current date & time and field trial start date are both correct. If not, tap **No, return to setup** and re-do steps _2_ and _3_ above ![](https://onebillion.org/img/xprize/setup-ss/screen3.png) If both are correct, tap **Yes, complete setup**. You will see the following screen and the device will turn itself off automatically: ![](https://onebillion.org/img/xprize/setup-ss/screen4.png)


## Ready for the child
The devices are now setup and ready for delivery to the children in their village. Please **do not turn the devices back on until all, including spares are in the village**. Turn them all on **on the same day** and press the big round button that appears. Give them to the children:
![](https://onebillion.org/img/xprize/setup-ss/screen5.png)
