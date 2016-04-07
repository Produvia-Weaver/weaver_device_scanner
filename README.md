# Weaver Device Scanner and Android SDK
![WeaverDeviceScanner](https://lh3.googleusercontent.com/-r5k2wEjSqZ0RC83Jaee9P5o2xZ7i_Q9ujlYS8qNLGwLzdS6skgKlSXtJjutApEPm38=w300-rw)
##### Weaver Device Scanner is a device and service scanner reference application
##### powered by the [Weaver SDK]

## 
## [Weaver SDK]
##### [Weaver SDK] is a cloud service and SDK, that lets developers scan and identify devices in the network
###
### Description
##### Weaver Device Scanner uses the Weaver Android SDK in order to:
  - Manage users (login and registration)
  - Scan and remember devices and services inside the network (including BLE devices)
  - Scan behind IoT hubs (such as philips hue or Lightify zigbee hubs)
   
##### Weaver Device Scanner Use cases:
###### Weaver Device scanner provides a crazy amount of usecases you can implement. For example:
  - Check if a device is disconnected
  - Quickly find IPs and MACs
  - Build a security app that notifies you whenever a new device enters your network
  - Build a Who's home app by monitoring which Cell-Phones are inside the network



##### [Checkout Weaver Device Scanner at Google Play] 
![Weaver Device Scanner Image1](https://lh3.googleusercontent.com/68K9Y-_mBabn1huNFA_wXBBfuYAUn_Dj0w9U2Qd98FICW4aR-EbzCMLNmeK8_uJg5moq=h310-rw) ![Weaver Device Scanner Image2](https://lh3.googleusercontent.com/IjeB_veSjMgXiCnwTbQgvUNqxE77fv0bVKSBW-x4e4MhNkRI5VcJOAH6r_xY_1aEb-fa=h900-rw) ![Weaver Device Scanner Image3](https://lh3.googleusercontent.com/8ABu_y9PuqbCaul6jprdfYI-O00XacE90EgcBMsNsQlABS01fsfHxZixIldo39eifKQ=h900-rw) 

### How To Use Weaver SDK
- Simply add the android SDK library to your build.gradle
- [Join our beta program] to receive your Weaver-SDK API KEY
- Also checkout the [Android Documentation]


### Installation

Add WeaverSDK android library to your build.gradle dependencies:

```sh
  compile 'produvia.com.weaverandroidsdk:weaverandroidsdk:0.0.30'
```
> The goal of the WeaverSDK is to let developers concentrate on developing their UI without the need to bother with maintaining tons of APIs.
> Weaver uses a simple JSON api in order to scan for connected devices.
> Whenever new device support is added, apps will usually automatically support the new devices without the need to update the App!


### Documentation
- [Android Documentation]
- [Also check out our Weaver REST Server]
- [And Weaver Lights]

License
----

The Weaver Device Scanner app is distributed under the MIT License



   [JSON API]: <http://weavingthings.com/weaver-sdk-reference/>
   [Join our beta program]: <https://produvia-net.com/developers>
   [Android Documentation]: <http://weavingthings.com/weaver-sdk-reference/>
   [Weaver SDK]: <http://weavingthings.com>
   [Checkout Weaver Device Scanner at Google Play]: <https://play.google.com/store/apps/details?id=produvia.com.scanner&hl=en>
   [Also check out our Weaver REST Server]: <https://github.com/Produvia-Weaver/WeaverRest>
   [And Weaver Lights]: <https://github.com/Produvia-Weaver/weaver_lights>
   
