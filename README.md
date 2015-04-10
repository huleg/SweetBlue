<b>|</b>&nbsp;<a href='#why'>Why?</a>
<b>|</b>&nbsp;<a href='#features'>Features</a>
<b>|</b>&nbsp;<a href='#getting-started'>Getting Started</a>
<b>|</b>&nbsp;<a href='#licensing'>Licensing</a>
<b>|</b>
<a href="http://75.144.199.157:7117/job/SweetBlue%20Library/">
  <img align="right" src="https://img.shields.io/badge/version-1.26.10-blue.svg" />
  <img align="right" src="https://github.com/iDevicesInc/SweetBlue/blob/master/scripts/assets/whitespace.bmp" />
  <img align="right" src="http://75.144.199.157:7117/buildStatus/icon?job=SweetBlue%20Library"/>
</a>
<p align="center">
  <br>
  <a href="https://idevicesinc.com/sweetblue">
    <img src="https://github.com/iDevicesInc/SweetBlue/blob/master/scripts/assets/sweetblue_logo.png" />
  </a>
</p>
Why?
====

Android's BLE stack has some...issues...

* https://github.com/iDevicesInc/SweetBlue/wiki/Android-BLE-Issues
* https://code.google.com/p/android/issues/detail?id=58381
* http://androidcommunity.com/nike-blames-ble-for-their-shunning-of-android-20131202/
* http://stackoverflow.com/questions/17870189/android-4-3-bluetooth-low-energy-unstable

SweetBlue is a blanket abstraction that shoves all that troublesome behavior behind a clean interface and gracefully degrades when the underlying stack becomes too unstable for even it to handle.

It’s built on the hard-earned experience of several commercial BLE projects and provides so many transparent workarounds to issues both annoying and fatal that it’s frankly impossible to imagine writing an app without it. It also supports many higher-level constructs, things like atomic transactions for coordinating authentication handshakes and firmware updates, flexible scanning configurations, read polling, transparent retries for transient failure conditions, and, well, the list goes on. The API is dead simple, with usage dependence on a few plain old Java objects and link dependence on standard Android classes. It offers conveniences for debugging and analytics and error handling that will save you months of work - last mile stuff you didn't even know you had to worry about.

Features
========

*	Full-coverage API documentation: http://idevicesinc.com/sweetblue/docs/api
*	Sample applications.
*	Battle-tested in commercial apps.
*	Plain old Java with zero API-level dependencies.
*	Rich, queryable state tracking that makes UI integration a breeze.
*	Automatic service discovery.
*	Easy RSSI tracking with built-in polling and caching, including distance and friendly signal strength calculations.
*	Highly configurable scanning with min/max time limits, periodic bursts, advanced filtering, and more.
*	Continuous scanning mode that saves battery and defers to more important operations by stopping and starting as needed under the hood.
*	Atomic transactions for easily coordinating authentication handshakes, initialization, and firmware updates.
*	Automatic striping of characteristic writes greater than [MTU](http://en.wikipedia.org/wiki/Maximum_transmission_unit) size of 20 bytes.
*	Undiscovery based on last time seen.
*	Clean leakage of underlying native stack objects in case of emergency.
*	Wraps Android API level checks that gate certain methods.
*	Verbose logging that outputs human-readable thread IDs, UUIDs, status codes and states instead of alphabet soup.
*	Wrangles a big bowl of thread spaghetti so you don’t have to - make a call on main thread, get a callback on main thread.
*	Internal priority job queue that ensures serialization of all operations so native stack doesn’t get overloaded and important stuff gets done first.
*	Optimal coordination of the BLE stack when connected to multiple devices.
*	Detection and correction of dozens of BLE failure conditions.
*	Numerous manufacturer-specific workarounds and hacks all hidden from you.
*	Built-in polling for read characteristics with optional change-tracking to simulate notifications.
*	Transparent retries for transient failure conditions related to connecting, getting services, and scanning.
*	Comprehensive callback system with clear enumerated reasons when something goes wrong like connection or read/write failures.
*	Distills dozens of lines of boilerplate, booby-trapped, native API usages into single method calls.
*	Transparently falls back to Bluetooth Classic for certain BLE failure conditions.
*	On-the-fly-configurable reconnection loops started automatically when random disconnects occur, e.g. from going out of range.
*	Retention and automatic reconnection of devices after BLE off->on cycle or even complete app reboot.
*	One convenient method to completely unwind and reset the Bluetooth stack.
*	Detection and reporting of BLE failure conditions that user should take action on, such as restarting the Bluetooth stack or even the entire phone.
*	Runtime analytics for tracking average operation times, total elapsed times, and time estimates for long-running operations like firmware updates.


Getting Started
===============
1. If using **Eclipse**...
  1. [Download](https://github.com/iDevicesInc/SweetBlue/releases) the latest release to a subfolder of your project such as `MyApp/libs/`.
  2. Open the `Package Explorer` view.
  3. Expand `MyApp/libs/sweetblue/`.
  4. If building with source...
    1. Right-click on the `src` folder.
    2. Hover over `Build Path->`.
    3. Click `Use as Source Folder`.
  5. Else if building with JAR...
    1. Expand the `jars` folder.
    2. Right click on `sweetblue_{version}.jar`.
    3. Hover over `Build Path->`.
    4. Click `Add to Build Path`.
2. Else if using **Android Studio** or **Gradle**...
  1. [Download](http://github.com/iDevicesInc/SweetBlue/releases) the latest release to a subfolder of your project such as `MyApp/src/main/lib/.`
  2. Open the app module's `build.gradle` file.
  3. If building with source, add the following to `sourceSets`:
 
    ```gradle
    android {
        ...
        sourceSets {
            ...
            main.java.srcDirs += 'src/main/lib/sweetblue/src'
        }
    }
    ```

  4. Else if building with JAR, add the following to `dependencies`:

    ```gradle
    dependencies {
        ...
        compile fileTree(dir: 'libs', include: '*.jar')
    }
    ```

3. Now add these to the root of `MyApp/AndroidManifest.xml`:

```xml
<uses-sdk android:minSdkVersion="18" android:targetSdkVersion="21" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

4. From your `Activity` or `Service` or `Application` instance, this is all it takes to discover a device, connect to it, and read a characteristic:
```java
BleManager.get(this).startScan(new DiscoveryListener()
{
    @Override public void onEvent(DiscoveryEvent e)
    {
        if( e.was(LifeCycle.DISCOVERED) )
        {
            e.device().connect(new StateListener()
            {
                @Override public void onEvent(StateEvent e)
                {
                    if( e.didEnter(BleDeviceState.INITIALIZED) )
                    {
                        e.device().read(Uuids.BATTERY_LEVEL, new ReadWriteListener()
                        {
                            @Override public void onEvent(ReadWriteEvent e)
                            {
                                if( e.wasSuccess() )
                                {
                                    Log.i("", "Battery level is " + e.data_byte() + "%");
                                }
                            }
                        });
                    }
                }
            });
        }
    }
});
```

Licensing
=========

SweetBlue is released here under the [GPLv3](http://www.gnu.org/copyleft/gpl.html). Please visit http://idevicesinc.com/sweetblue for proprietary licensing options. In a nutshell, if you're developing a for-profit commercial app you may use this library for free for evaluation purposes, but most likely your use case will require purchasing a proprietary license before you can release your app to the public. See the [FAQ](https://github.com/iDevicesInc/SweetBlue/wiki/FAQ) for more details and https://tldrlegal.com/license/gnu-general-public-license-v3-%28gpl-3%29 for a general overview of the GPL.
<p align="center"><a href="https://idevicesinc.com/sweetblue"><img src="https://github.com/iDevicesInc/SweetBlue/blob/master/scripts/assets/sweetblue_logo.png" /></a></p>
