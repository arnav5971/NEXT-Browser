package com.nextbrowser.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextbrowser.domain.model.BrowserSettings

@Composable
fun BrowserSettingsDialog(
    settings: BrowserSettings,
    onThemeChange: (String) -> Unit,
    onSearchEngineChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDesktopSiteChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Browser settings") },
        text = {
            Column {
                Text("Appearance")
                listOf(
                    BrowserSettings.THEME_SYSTEM to "System",
                    BrowserSettings.THEME_LIGHT to "Light",
                    BrowserSettings.THEME_DARK to "Dark"
                ).forEach { (value, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = settings.themeMode == value, onClick = { onThemeChange(value) })
                        Text(label)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dynamic colors", modifier = Modifier.weight(1f))
                    Switch(checked = settings.dynamicColor, onCheckedChange = onDynamicColorChange)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Desktop site")
                        Text("Request desktop versions of websites", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = settings.desktopSite, onCheckedChange = onDesktopSiteChange)
                }
                Text("Search engine", modifier = Modifier.padding(top = 12.dp))
                listOf(
                    BrowserSettings.SEARCH_ENGINE_GOOGLE to "Google",
                    BrowserSettings.SEARCH_ENGINE_BING to "Bing",
                    BrowserSettings.SEARCH_ENGINE_DUCKDUCKGO to "DuckDuckGo"
                ).forEach { (value, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = settings.searchEngine == value, onClick = { onSearchEngineChange(value) })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}
