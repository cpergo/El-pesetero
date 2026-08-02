package com.pesetas

import android.app.Application
import com.pesetas.platform.AndroidPlatformSession

class PesetasApplication : Application() {
    val platformSession: AndroidPlatformSession by lazy { AndroidPlatformSession(this) }
}
