# Building onecourse

## Overview
The installation steps mentioned below will allow you to setup the application as a standalone application alongside your current filesystem without having to flash custom filesystem images. If you instead want to build custom filesystem images for onecourse application follow the steps mentioned in [BUILD](https://github.com/XPRIZE/GLEXP-Team-onebillion/blob/master/BUILD.md) file.

## Requirements
1. Ensure you have _adb_ installed on your computer. You can [follow the steps in this helpful guide](https://www.androidpit.com/how-to-install-adb-and-fastboot).
2. Connect your device to the computer via USB.

## 1. Device preparation

##### Enable developer mode
1. Go to **Settings > About tablet**.
2. Tap **Build Number** 7 times until it displays _"You are now a developer"_.

##### Enable USB Debugging
1. Go to **Settings > Developer** options.
2. Tap **Enable USB debugging**.
3. Tap **OK** when prompted by the disclaimer.
4. You should see dialog with you computer's _"RSA key fingerprint"_.
5. Tap _"Always allow from this computer"_.
6. Tap **OK**.

## 2. Building the onecourse app

1. Ensure you have **Android Studio** installed. You can [follow the steps in this helpful guide](https://developer.android.com/studio/install.html).

2. Configure the _ROOT_FOLDER_ environment variable and _ANDROID_HOME_ environment variable based on the location of the Android SDK:  
   
   Follow these steps for Mac/Linux based system:
	
		export ROOT_FOLDER=onecourse
		
		export ANDROID_HOME=[PATH/TO/ANDROID_SDK]
		
   Follow these steps for Windows based system:

		Set-Variable -Name "ROOT_FOLDER" -Value "onecourse"
		
		Set-Variable -Name "ANDROID_HOME" -Value "PATH/TO/ANDROID_SDK"
		
3. Clone the **onecourse source** repository:
		
		git clone https://github.com/XPRIZE/GLEXP-Team-onebillion.git $ROOT_FOLDER/
		
4. In the _ROOT_FOLDER_ build _gradle_ tasks:

        ./gradlew tasks
		
5. Build the **onecourse** _.apk_:

	onecourse English:

          ./gradlew assembleEnGB_community_debug
	  
	onecourse Swahili:
	
          ./gradlew assembleSw_community_debug
		  
6. Download the language specific assets from [releases](https://github.com/XPRIZE/GLEXP-Team-onebillion/releases/tag/v3.0.0) section and extract into your _ROOT_FOLDER_.
		
## 3. Installing onecourse

1. Install the application via adb:

	onecourse English:

		adb install app/build/outputs/apk/enGB_community_/debug/app-enGB_community_-debug.apk
		
	onecourse Swahili:
	
		adb install app/build/outputs/apk/sw_community_/debug/app-sw_community_-debug.apk
		
2. Copy the assets to Android device:

		adb push -p assets/ /sdcard/onebillion/assets
		
3. Once the assets are transferred, open the application and grant all the required permissions.

4. Check if installation was successfull. Tap the **Watch video** button to watch our short 'tablet care' video. Tap **Test onecourse** to try a random 'learning unit'. If neither load, there was an issue with the installation. Please reinstall the application by following steps 1 to 3 again.
	<img src="https://onebillion.org/img/xprize/setup-ss/screen1.png" width="850" height="500">

5. Set the field trial start date. Tap **Set trial start date** and select the current date: <img src="https://onebillion.org/img/xprize/setup-ss/screen2.png" width="850" height="500">
  
   Next, tap **Yes, complete setup**. You will see the following screen and the application will exit itself automatically:  
	<img src="https://onebillion.org/img/xprize/setup-ss/screen4.png" width="850" height="500">

6. Restart the application after which it is ready to be used.
