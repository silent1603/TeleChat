package com.ET.telechat.Models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ET.telechat.Services.Guardian

class Boot : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Guardian.initiate(context)
        }
    }
}