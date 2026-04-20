package com.tanim.omniguard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tanim.omniguard.presentation.navigation.NavGraph
import com.tanim.omniguard.presentation.theme.OmniGuardTheme
import com.tanim.omniguard.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Ensure channel is created for notifications
        NotificationHelper.createNotificationChannel(this)

        setContent {
            OmniGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Handle deep link from notification
                    LaunchedEffect(intent) {
                        handleNotificationIntent(intent) { route ->
                            navController.navigate(route)
                        }
                    }

                    NavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?, navigate: (String) -> Unit) {
        val targetScreen = intent?.getStringExtra("TARGET_SCREEN")
        if (targetScreen != null) {
            navigate(targetScreen)
        }
    }
}