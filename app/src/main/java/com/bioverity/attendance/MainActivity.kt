
package com.bioverity.attendance

import android.content.Intent
import android.net.Uri
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import com.bioverity.attendance.navigation.AppNavigation
import com.bioverity.attendance.ui.theme.BioVerityAttendanceTheme

class MainActivity : ComponentActivity() {

    private var resetUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleResetIntent(intent)

        setContent {
            BioVerityAttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(
                        resetUri = resetUri,
                        onResetUriHandled = {
                            resetUri = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        setIntent(intent)

        handleResetIntent(intent)
    }

    private fun handleResetIntent(intent: Intent?) {

        val uri = intent?.data ?: return

        if (
            uri.scheme == "bioverity" &&
            uri.host == "reset-password"
        ) {
            resetUri = uri
        }
    }
}

