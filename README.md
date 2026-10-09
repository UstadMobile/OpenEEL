# Open Educational Experience Launcher (OpenEEL)

The Open Educational Experience Launcher uses open source and open standards 
([xAPI](https://www.xapi.com/), [OPDS](https://opds.io/), and 
[WebPub Manifest](https://readium.org/webpub-manifest/)) to make it easy for teachers and students
to access apps, online or offline, with a single account and keep their personal data secure on a 
server of their choice.

Developers can connect native Android apps and HTML/Javascript-based apps: see 
[README_ADD_YOUR_APP.md](README_ADD_YOUR_APP.md) for info for developers who want to connect their
own app.

## Development environment setup:

These instructions are intended for developers who wish to build/run from source code. Development
tested on Ubuntu Linux, however it should work on Windows, other Linux versions, or MacOS. 

This is a Kotlin Multiplatform project. This repository contains the Android app and
backend server source code in its modules. Android Studio is the development environment for the
entire project. 

*  __Step 1: Download and install Android Studio__: If you don't already have the latest version, download
   from [https://developer.android.com/studio](https://developer.android.com/studio). The latest 
* [stable channel version](https://developer.android.com/studio/releases) is recommended.

* __Step 2: Install dependencies__
    * JDK21

Ubuntu/Debian Linux:
```
sudo apt-get install openjdk-21-jdk
```

Windows:
Download and install the Microsoft OpenJDK build from 
[https://learn.microsoft.com/en-us/java/openjdk/install#install-on-windows](https://learn.microsoft.com/en-us/java/openjdk/install#install-on-windows).

* __Step 3: Import the project in Android Studio__: Select File, New, Project from Version Control. Enter
  https://github.com/UstadMobile/OpenEel.git and wait for the project to import.

* __Step 4: Run the server__: Run the server using Gradle:

Run the server from source using Gradle:
```
./gradlew app-server:run
```
_Note: On the windows command line the ./ should be omitted_

* __Step 5: Add a [school](ARCHITECTURE.md#schools)__ - each school has its own users, classes, etc.
  Each school instance has its own database (e.g. database for school1, school2, etc).

  OpenEel supports virtual hosting enabling multiple schools to run within a single JVM instance, eg
  as school1.example.org, school2.example.org etc.

e.g.
```
./gradlew app-server:run --args='addschool --url http://10.1.2.3:8098/ --name devschool --adminpassword secret' 
```
Note: localhost _won't_ work on Android emulators and devices because localhost refers to the 
emulator/device itself _not_ the PC running on the server.

To see all available command line options (including database options etc):
```
./gradlew app-server:run --args='addschool --help'
```

Note: in order for the search by school name to work you must add your server to the app directory
list (default or local)
(e.g. http://10.1.2.3:8098/ as above) in [directories](lib-shared/src/androidMain/resources/directories)

* __Step 6: Build/run and Android app__: In Android Studio use the run/debug button to run the 
 ```app-android``` module. See [app-android](app-android/) for further
 details on running via the command line etc. You can login to your school using the Android app
 by tapping ```Other options```, entering the school URL, then use the username admin and the
 password you set in step 5.

## Contributing

We welcome community contributions including code, bug reports, feature requests, localization and more. Please see [CONTRIBUTING.md](CONTRIBUTING.md).

## Community

Join our [Community Slack Space](https://join.slack.com/t/respectdevelopers/shared_invite/zt-3t8dpyxxs-0nSsFsLGau5MjVZQzrlvqA).

## Build environment variables

The following environment variables can be set:

```
DEFAULT_APPLIST - the default list of launchable app manifest URLs e.g. https://respect.world/respect-ds/manifestlist.json
```

## Publishing

As per [Kotlin docs](https://kotlinlang.org/docs/multiplatform/multiplatform-publish-lib-setup.html#publishing-to-a-local-maven-repository)

If the environment variable ```GIT_TAG_NAME``` is set (eg by [Jenkins plugin](https://plugins.jenkins.io/git-tag-message/)), 
this will be used as the version, otherwise the version will be read from the ```gradle.properties``` file.

## Legal and license

Copyright 2024-2025 UstadMobile FZ-LLC. This code is substantially derived from [UstadMobile](https://www.github.com/UstadMobile/UstadMobile/).
Documentation: [CC-BY](https://creativecommons.org/licenses/by/4.0/) license.
Code and all other works: [AGPLv3](LICENSE) license.

‘RESPECT™’ and ‘RESPECT compatible™’ are trademarks of the Spix Foundation.
All other trademarks and registered trademarks are the properties of their respective owners.
