# app-cli

**Launchable app tester**

The launchable app is a command line tester that will:
* Select a random number of learning units from the launchable app's manifest
* Login to the Respect app using [Maestro](https://maestro.mobile.dev/),navigate to the given
  learning unit, and open it. A human or agent then needs to complete the learning unit.
* For some learning units (selected at random) download them and put the phone into airplane mode
  before opening.
* Check xAPI statements after 

The tester supports both web based learning units. 

Prerequisites:
* Install [Maestro CLI](https://docs.maestro.dev/maestro-cli/how-to-install-maestro-cli)
* Install Respect launcher app on an emulator or device

**Testing HTML/Javascript based learning unit**

```
./unzipped-path/app-cli test-launchable-app \
    --manifest https://demo.openeel.org/en-US/launchable-app-manifest.json \
    --serverurl http://192.168.1.2:8098/ \
    --username admin \
    --password adminpassword \
    --outputdir /home/myuser/test-results \
    --mode webview
```

Where:
* The launchable app manifest url is `https://demo.openeel.org/en-US/launchable-app-manifest.json`
* The respect server url is `http://192.168.1.2:8098/`
* The admin password to login to the respect server is `adminpassword`

**Testing a native app**

* Install the native app on the device

```
./unzipped-path/app-cli test-launchable-app \
    --manifest https://demo.openeel.org/en-US/launchable-app-manifest.json \
    --serverurl http://192.168.1.2:8098/ \
    --username admin \
    --password adminpassword \
    --outputdir /home/myuser/test-results \
    --mode native
```

Where:
* The launchable app manifest url is `https://demo.openeel.org/en-US/launchable-app-manifest.json`
* The respect server url is `http://192.168.1.2:8098/`
* The admin password to login to the respect server is `adminpassword`

**Optional arguments**

* `device` emulator or device id to use as per `adb devices` command output
* `numunits` the number of learning units to select
