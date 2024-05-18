package com.example.ewaykollect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class connectivityReceiver : BroadcastReceiver() {

    var status: String? = ""
    override fun onReceive(context: Context?, intent: Intent?) {
         status = Networkstatus().getConnectivityStatusString(context)

        Toast.makeText(context, status, Toast.LENGTH_SHORT).show()

    }
}