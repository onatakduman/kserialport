package com.onatakduman.kserialport.app

import android.app.Application
import com.onatakduman.kserialport.app.ads.AdManager

class KSerialPortApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}
