package com.stack.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.stack.core.ui.theme.StackTheme
import com.stack.player.navigation.StackNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isExpanded = isExpandedWindow()

            StackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StackNavHost(isExpanded = isExpanded)
                }
            }
        }
    }
}

@Composable
private fun isExpandedWindow(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 840
}
