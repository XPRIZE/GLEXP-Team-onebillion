# Building onecourse

Last updated: May 6, 2019
## Overview
The installation steps below will allow you to set-up the application as a standalone application alongside your current filesystem without the need to flash custom filesystem images. If you instead want to build custom filesystem images (i.e., clear the file system and have a single application remain on the tablet) for onecourse application, follow the steps detailed in [BUILD](https://github.com/XPRIZE/GLEXP-Team-onebillion/blob/master/BUILD.md) file.

**Note:** OS-specific commands are mentioned where required. All other commands are OS independent (i.e., they work on both Windows and Mac/Linux)

## Requirements
1. Ensure you have _adb_ installed on your computer. To do this, you can [follow the steps in this helpful guide](https://www.androidpit.com/how-to-install-adb-and-fastboot).
2. Connect your device to the computer via USB.

## 1. Device preparation

##### Enable developer mode on your device
1. Go to **Settings --> About tablet**.
2. Tap **Build Number** 7 times until it displays _"You are now a developer"_.

##### Enable USB Debugging on your device
1. Go to **Settings --> Developer** options.
2. Tap **Enable USB debugging**.
3. Tap **OK** when prompted by the disclaimer.
4. You should see a dialog box with your computer's _"RSA key fingerprint"_.
5. Tap _"Always allow from this computer"_.
6. Tap **OK**.

## 2. Building the onecourse app

1. Ensure you have **Android Studio** installed. To do this, you can [follow the steps in this helpful guide](https://developer.android.com/studio/install.html).

2. Configure the _ROOT_FOLDER_ environment variable and _ANDROID_HOME_ environment variable based on the location of the Android SDK:  
   
   Follow these steps for macOS (Mavericks or later) or Linux-based system:
	
		export ROOT_FOLDER=onecourse
		
		export ANDROID_HOME=[PATH/TO/ANDROID_SDK]
		
   Follow these steps for Windows-based system:

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
		  
6. Download the language-specific assets from the [releases](https://github.com/XPRIZE/GLEXP-Team-onebillion/releases/tag/v3.0.0) section and extract into your _ROOT_FOLDER_.
		
## 3. Installing onecourse

1. Install the application via adb:

	onecourse English:

		adb install app/build/outputs/apk/enGB_community_/debug/app-enGB_community_-debug.apk
		
	onecourse Swahili:
	
		adb install app/build/outputs/apk/sw_community_/debug/app-sw_community_-debug.apk
		
2. Copy the assets to the Android device:

		adb push -p assets/ /sdcard/onebillion/assets
		
3. Once the assets are transferred, open the application and grant all the required permissions. It is now ready to use.  
**Note:** Grant the _Allow modify system settings_ permission to allow the application to manage brightness setting.
