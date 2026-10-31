package com.nextbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nextbrowser.ui.screens.browser.BrowserScreen
import com.nextbrowser.ui.screens.home.HomeScreen
import com.nextbrowser.ui.theme.NextBrowserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextBrowserTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NextBrowserApp()
                }
            }
        }
    }
}

@Composable
private fun NextBrowserApp() {
    var activeUrl by remember { mutableStateOf<String?>(null) }

    if (activeUrl == null) {
        HomeScreen(
            onSearch = { query -> activeUrl = query }
        )
    } else {
        BrowserScreen(
            initialUrl = activeUrl.orEmpty(),
            onGoHome = { activeUrl = null }
        )
    }
}
