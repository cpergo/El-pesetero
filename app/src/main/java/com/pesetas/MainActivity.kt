package com.pesetas

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.pesetas.platform.AndroidPlatformSession
import com.pesetas.ui.AppRoot

class MainActivity : FragmentActivity() {

    private val platformSession: AndroidPlatformSession
        get() = (application as PesetasApplication).platformSession

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        platformSession.attach(this)
        setContent {
            AppRoot(
                container = platformSession.container,
                onRestartRequired = ::restart,
            )
        }
    }

    private fun restart() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}
