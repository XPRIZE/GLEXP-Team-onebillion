# Localization of onecourse


## Process
There are 8 key steps to localize onecourse. We work with local liguistic experts at each stage:

1. **Analyse target language and culture**. Examine the target language, and build a clear description of it, including: linguistic structure, alphabet, phonetic structure, order and frequency of graphemes, list of high frequency words, set of common first and last names.  Carry out a comprehensive analysis to understand the target area, in order to culturally adapt the course to that specific geographical area.
2. **Cross check target language**. Check the description of the new language against our existing learning activities. Develop new components or modes, if beneficial to the child.
3. **Story selection**. Select appropriate stories from our extensive book library, and write or curate further stories as required.
4. **Translation**. Translate the full database of words, instruction scripts for each activity, chosen stories, and numeracy learning units.
5. **Writing**. If there is any additional material required for the target language, such as simple phrases and sentences, it will be written at this stage.
6. **Images**. Creation of any new images required for learning units and stories.
7. **Recording**. Recording of all audio material required, using several voice artists.
8. **Final build**. With all assets prepared, build onecourse in the new target language.


## Overview
Each localized version of onecourse requires a language pack for a _locale_. A locale is a pairing of a language and an optional region (e.g. Tanzanian Swahili, British English). A locale is represented using abbreviated codes, so Swahili would be `sw` and British English `en_GB`. A language pack consists of eight elements:

1. The alphabet
2. Phonemes, syllables and words
3. Audio and images
4. Stories
5. Component audio
6. Learning journey
7. Fonts
8. Video subtitles


## Language Pack

### 1. Alphabet
The file `assets/oc-literacy-gen/local/LOCALE/letters.xml` defines the individual letters that constitute the language’s alphabet.
Each `letter` has a unique id and an optional set of `tags`. Possible values for the tag attribute vary across language families, with `vowel` being used in both English and Swahili. The letters _a_, _b_ and _d_ are represented like this:

```xml	
<letters>
	<letter id="a" tags="vowel"></letter>
	<letter id="b"></letter>
	<letter id="d"></letter>
</letters>
```

### 2. Phonemes, syllables and words
The file `assets/oc-literacy-gen/local/LOCALE/wordcomponents.xml` defines the key phonemes, consonant clusters and syllables present in the language. It also contains a curated set of high-frequency and culturally specfic words for learning the language.

Each `phoneme` has a unique `id` with the prefix `is`. In Swahili, the phonemes _a, th_ and _ng'_ are represented like this:

```xml		
<phonemes>
	<phoneme id="is_a">a</phoneme>
	<phoneme id="is_th">th</phoneme>
	<phoneme id="is_ng_apost_">ng'</phoneme>
</phonemes>
```

Next come syllables. Each `syllable` is made from one or more `phoneme` and has a unique `id` with the prefix `isyl`. The individual phonemes in each syllable are delimited by `/`. In Swahili, the syllables _ju_ and _zwe_ are represented like this:
	
```xml
<syllables>
	<syllable id="isyl_ju">j/u</syllable>
	<syllable id="isyl_zwe">zw/e</syllable>
</syllables>
```
	
Finally, the set of words. Each `word` is made from one or more `phonemes` or `syllables` and has a unique `id` with the prefix `fc`. The phonemes or syllables in each word are delimited by `/`. In Swahili, the words _paka_ and _rafiki_ are represented like this:

```xml
<words>
	<word id="fc_cat">pa/ka</word>
	<word id="fc_friend">ra/fi/ki</word>
</words>
```    


### 3. Audio and images

Each `letter`, `phoneme`, `syllable` and `word` is recorded by a native speaker and an _aac_ compressed version is stored in an _.m4a_ file, named by it's English `id`. For example, in Swahili, the words _nywele, jani_ and _kima_ exist as recorded audio files: `fc_hair.m4a`, `fc_leaf.m4a` and `fc_monkey.m4a`. These reside in the `assets/oc-literacy-gen/local/LOCALE/` directory.

Where a word is broken down into `syllables` or `phonemes`, the recording has an accompanying _.epta_ file which specifies the start time of each phoneme or syllable in the audio file. For example, the English word _kick_, recorded in the file `fc_let_kick.m4a` has the following phoneme breakdowns in `fc_let_kick.etpa`:

```xml
	<timings text="k - i - ck">
		<timing id="0" start="0.000" end="0.207" startframe="0" framelength="9108" text="k"/>
		<timing id="1" start="0.609" end="0.924" startframe="26844" framelength="13915" text="i"/>
		<timing id="2" start="1.324" end="1.486" startframe="58401" framelength="7121" text="ck"/>
	</timings>
```


For each `word`, an optional _.png_ image exists. In Swahili, the words _mbu, kiazi_ and _shule_ have image files: `fc_mosquito.png`, `fc_potato.png` and `fc_school.png`.


### 4. Stories
Localised stories each have an `id` and reside in `assets/oc-reading/books/xr-[id]/`. Each story has configuration file `book.xml`.
Presentational apsects of the story are defined on the `book` element. Each `page` contains one or more localised `para`. A `page` can have an optional `picjustify` attribute to specify the page layout.

In lower level stories, there is a syllable breakdown for each word, delimited by `/`. The title and first page of the _A very tall man_ story in Swahili are represented like this:

```xml	
<book id="xr-averytallmanSW" indent="N" lineheight="1.5" paraheight="1.33" letterspacing="1" fontsize="50" noparas="true">
	<page pageno="0">
		<para>M/tu m/re/fu sa/na</para>
	</page>
	<page pageno="1">
		<para>Mwa/nga/li/e m/tu hu/yu. Je/mbe la/ke ni fu/pi m/no.</para>
	</page>
…
```


Every `para` has a corresponding _.m4a_ recorded audio file and an accompanying _.etpa_ file which specifices the start time of each word in the audio file. For example, the breakdown of the first page of the story above can be seen in `p1_1.etpa`:


```xml
<xml>
	<timings text="Mwangalie mtu huyu. Jembe lake ni fupi mno.">
		<timing id="0" start="0.000" end="0.513" startframe="0" framelength="22630" text="Mwangalie"/>
		<timing id="1" start="0.513" end="0.872" startframe="22630" framelength="15820" text="mtu"/>
		<timing id="2" start="0.872" end="1.235" startframe="38450" framelength="16018" text="huyu"/>
…
```

Each word within a `para` is also individually recorded, along with a version split into syllables for lower-level stories.
Use [PhraseAnal](https://github.com/XPRIZE/GLEXP-Team-onebillion-PhraseAnal), to assist with generating _.etpa_ files from audio files.


### 5. Component audio
Component localizations consist of a set of _.m4a_ recorded audio files. The file names tend to correspond to the english localization.
For example in the numeracy component _Add and subtract_, `assets/oc-addsubtract/local/sw/q_sevenbees.m4a` is the Swahili translation of the phrase _"Seven bees"_. In the reading component _Making plurals_, `assets/oc-makingplurals/local/en_GB/mp2_goodtheyreinorder.m4a` is the english recording of _"Good, they are in order"_.
We have provided mappings of all English audio to _.m4a_ filenames. These are xml files inside the `assets/localization` directory.


### 6. Learning journey

The child's _learning journey_ is an ordered set of `learning units` to be worked through. A `learning unit` is a `component` with parameters assigned from the underlying language pack as well as any required visual, audio or configuration assets.  It is specific to a particular localization. There are three variants, all residing in the `assets/masterlists/[community|playzone|library]` directory:

#### Community

This is defined in the file `community_LOCALE/units.xml`. An example of the first part of the onecourse Swahili _learning journey_ from `community_swunits.xml`is shown below. The First unit is an introduction to using the tablet, the second a _flashcard_ reading activity:

```xml
<level id="1">
	<unit	id="0002.OC_SectionIT"
			target="OC_SectionIT"
			params="eventit"
			config="oc-introduction"
			lang="sw"
			targetDuration="120"
			passThreshold="0.5"
			icon="icon_0002"/>
	<unit id="0003.OC_Sm2"
			target="OC_Sm2"
			params="sm2/
					demo=true/
					demotype=a1/
					noscenes=8/
					words=fc_prize,fc_animals,fc_sugar,fc_donkey,fc_pineapple,fc_glasses,fc_shorts,fc_minibus,fc_address,fc_basin,fc_cooking_pot,fc_sister,fc_mango,fc_hoe,fc_button,fc_drum,fc_pump_for_well,fc_friends,fc_rabbit,fc_soil,fc_children,fc_heron"
			config="oc-lettersandsounds"
			lang="sw"
			targetDuration="120"
			passThreshold="0.5"
			icon="icon_0003"/>
…
```


##### Levels
A _learning journey_ consists of a number of `levels`. These are ordered groups of 105 `learning units`. They represent _weeks_ in the course, with 15 units per day.

##### Learning units
A `learning unit` has the following parameters:

- `id` unique identifier.
- `target` component the unit is using.
- `param` list of component-specfic parameters in `key=value` form, each delimited by `/`.
- `config` configuration directory for audio and video assets used by the component.
- `lang` language pack to be used.
- `targetDuration` upper bound on the time to should take an average child to complete the unit.
- `passThreshold` the ratio of correct:incorrect answers which constitute the child _passing_ the unit successfully.
- `icon` the image shown to the child before beginning the unit. These are `png` files stored in `assets/masterlists/[community|library|playzone]_LOCALE/icons/`

In the following example, the _letter tracing_ component `OC_LetterTrace` is being used. The `params` indicate:

- `lt` single letter mode.
- `intro=false` no introductory audio.
- `letter=i` use the letter `i` from the Swahili `language pack`
- `notraces=4` the letter is to be traced 4 times.

```xml

<unit	id="0052.OC_LetterTrace"
		target="OC_LetterTrace"
		params="lt/intro=false/letter=i/notraces=4"
		config="oc-lettersandsounds"
		lang="sw"
		targetDuration="120"
		passThreshold="0.5"
		icon="icon_0052"
/>
```

#### Play Zone
This is defined in the file `playzone_LOCALE/units.xml`. It specifies which units appear in the _play zone_ for each level (week).

#### Library
This is defined in the file `library_LOCALE/units.xml`. It specifies all of the stories in the story library for the locale. Here, _level_ represents the relative complexity of a set of stories.

### 7. Fonts
onecourse by default uses two fonts, `onebillionreader-Regular.otf` and `onebillionwriter-Regular.otf`. These can be replaced by identically named alternative fonts in `app/src/main/fonts/`. Please note onecourse does not currently support right-to-left scripts.

### 8. Video Subtitles
For video clips in the onecourse _play zone_, optional subtitles can be added. These are standard `.srt` text files placed in the `assets/oc-video/local/LOCALE/` directory. Each subtitle entry within a file consists of four parts:

1. A numeric counter identifying each sequential subtitle.
2. The time that the subtitle should appear on the screen, followed by `-->` and the time it should disappear.
3. The subtitle itself on one or more lines.
4. A blank line indicating the end of this subtitle.

For example, the Swahili subtitle for the video _Origami Elephant_ in `assets/oc-video/local/sw/origami_elephant.srt`:

```
1
00:00:00,000 --> 00:09:08,990
Tutengeneze tembo wa karatasi!

```





## Build

Apply the following configurations to files in the onecourse source code directory

###  Settings

Create a settings `.plist` file for the new locale by copying the English:

`cp app/src/main/config/settings_community_enGB.plist app/src/main/config/settings_community_LOCALE.plist`

Edit the new settings file, replacing the value of the following keys:

```
<key>app_masterlist</key>
<string>community_LOCALE</string>

<key>app_masterlist_playzone</key>
<string>playzone_LOCALE</string>

<key>app_masterlist_library</key>
<string>library_LOCALE</string>

```

### Build target

Append the following configuration to `build.gradle`:

```gradle
LOCALE_community_ {
    applicationId ‘org.onebillion.onecourse.child.LOCALE'
    versionCode 1
    versionName '1.0'
    resValue "string", "app_name", "onecourse - Child"
    resValue "string", "test_only", "false"
    buildConfigField 'String', 'SETTINGS_FILE', '"settings_community_LOCALE.plist"'
    manifestPlaceholders = [
            appIcon: "@mipmap/icon_child"
    ]
}
```

Follow the [build instructions](BUILD.md) to compile onecourse.
