## LTECleanerFOSS
A maintained-ish improved fork of @TheRedSpy15's LTE Cleaner

![Android CI](https://github.com/mdp43140/LTECleanerFOSS/workflows/Android%20CI/badge.svg)
[![GitHub issues](https://img.shields.io/github/issues/mdp43140/LTECleanerFOSS)](/issues)
[![License](https://img.shields.io/github/license/mdp43140/LTECleanerFOSS)](/blob/master/LICENSE)

Tired of the abundance of phone cleaners on the Play Store? Tired of
them being extremely shady? Tired of them doing nothing? Tired of ads?
Tired of having to pay? Me too.

There are simply way too many apps out there that claim to "speed up your device". In reality, they don't do anything.
LTE Cleaner only aims to clean your phone by removing safe to delete files, which not only frees up a lot of space, but also improve your privacy. Since LTE Cleaner removes .log files, which well, log what you do.

__LTE Cleaner is 100% free, open source, no ads, and deletes everything it claims to.__

## Install
[GitHub release](https://github.com/MDP43140/LTECleanerFOSS/releases)
[Build it yourself](#compiling-the-app)
[Original F-Droid (outdated)](https://f-droid.org/packages/theredspy15.ltecleanerfoss)
[Original source code (outdated)](https://github.com/theredspy15/LTECleanerFOSS)
[Original GitHub release (outdated)](https://github.com/theredspy15/LTECleanerFOSS)

## Features
- Clipboard clearing
- Whitelist
- Daily cleanup

Cleans:
- Empty folders
- Logs
- Temporary files
- Caches
- Advertisement folders

To do list (not guaranteed because i'm busy irl):
- Code cleanups
- Clean SD card (has to support minimal Android 10+, hopefully we can use StorageAccessFramework to make this work, but it might be a huge work that can take days, not possible with my spare time)
- Regex whilelist
- About screen (designing the UI would be really painful, and using external dependency will also significantly enlarge file size)
- Root cleanups
- Android 13+ cant grant storage permission anymore :(
- Fragments & Back gestures :)
<!-- Scan then clean, instead of doing both at the same time (atleast on some devices that i tested on, it lags when there is so many files)-->

## Screenshots
<img src="Screenshots/ui_main.png" width="200">
<img src="Screenshots/ui_settings.png" width="200">
<img src="Screenshots/ui_wl.png" width="200">

## The Team
<a href="https://github.com/mdp43140/LTECleanerFOSS/graphs/contributors">
	<img src="https://contrib.rocks/image?repo=mdp43140/LTECleanerFOSS" />
</a>

Contribute to this project:
- Test the app with different devices
- Report issues and feature requests in GitHub's [issue tracker](https://github.com/mdp43140/LTECleanerFOSS/issues) or Codeberg's [issue tracker](https://codeberg.org/mdp43140/LTECleanerFOSS/issues)
- Create a [Pull Request](https://opensource.guide/how-to-contribute/#opening-a-pull-request)
- Translate this app into more languages

## Compiling the app
First, export some variables (for Linux users. Windows user might want to adjust this a bit):```bash
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-/home/mdp/.android/SDK}"
```
Just go to the project root directory, and run this command: ```./gradlew :app:assembleRelease``` (if you're using Windows, change `./gradlew` to `gradlew.bat`)

## License
[![GNU GPL v3](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)

LTE Cleaner is Free Software: You can use, study, share, and improve it at
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) as
published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
