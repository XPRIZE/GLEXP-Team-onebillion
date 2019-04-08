# Localization instructions for onecourse

Localizing the onecourse application requires updates to the assets available in [releases](https://github.com/XPRIZE/GLEXP-Team-onebillion/releases/tag/v3.0.0) section.

## Directory structure of assets

```
assets
└─── section_name
    └─── config
    └─── img
    └─── local
    │   └─── en_GB
    │   └─── sw
    └─── sfx
```

Each section in assets directory consist of the following folders:  
  * __config__: Contains XML files with image size, location, position, audio file name mapping  
  * __img__: Contains images used for a particular section  
  * __local__: Contains audio files, etpa files which store timestamps for words and XML file which stores the section specific content  
  * __sfx__: Contains language independent SFX audio files  

## Steps to localize the application

1. Localize the icons available under following folders:  
    * onecourse\assets\masterlists\community_enGB\icons  
    * onecourse\assets\masterlists\library_enGB\icons  
    * onecourse\assets\masterlists\playzone_enGB\icons  
2. Replace/add the audio files in _assets\local\en_GB_ folder with localized audio and update/create the corresponding timestamp in _'.etpa'_ files. (Contact onebillion team for the approach to update timestamp)
3. Update/add the audio file names in the XML files present in assets\config folder.
4. If any audio file in the assets is renamed\added, a corresponding change is required in the following files:
    * onecourse\assets\masterlists\community_enGB\original.xml
    * onecourse\assets\masterlists\community_enGB\units.xml
    * onecourse\assets\masterlists\playzone_enGB\units.xml
    * onecourse\assets\masterlists\test_enGB\original.xml
    * onecourse\assets\masterlists\test_enGB\units.xml
5. Replace the images in _assets\img_ folder with localized images having same name.
