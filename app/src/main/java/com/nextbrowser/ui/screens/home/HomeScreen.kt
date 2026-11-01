package com.nextbrowser.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onSearch: (String) -> Unit,
    onShowTabs: () -> Unit,
    tabCount: Int,
    onNewTab: () -> Unit = onShowTabs,
    onShowBookmarks: () -> Unit,
    onShowHistory: () -> Unit,
    onShowSettings: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    val ink = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val blue = MaterialTheme.colorScheme.primary

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Browser tab strip
        Row(
            Modifier.fillMaxWidth().height(34.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)).padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f).fillMaxHeight().padding(top = 5.dp)
            ) {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, tint = blue)
                    Spacer(Modifier.width(12.dp))
                    Text("New tab", color = ink, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = onShowTabs, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Close, "Close tab", tint = muted)
                    }
                }
            }
            IconButton(onClick = onNewTab) { Icon(Icons.Default.Add, "New tab", tint = muted) }
            IconButton(onClick = onShowTabs) { Icon(Icons.Default.Tab, "Tabs", tint = muted) }
            Text(tabCount.toString(), color = muted, style = MaterialTheme.typography.labelMedium)
        }

        // Navigation / address toolbar
        Row(
            Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.ArrowBack, "Back", tint = muted) }
            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.ArrowForward, "Forward", tint = muted) }
            IconButton(onClick = { searchText = "" }, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Refresh, "Refresh", tint = muted) }
            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Home, "Home", tint = muted) }
            Surface(
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .75f)
            ) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = muted)
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { if (searchText.isNotBlank()) onSearch(searchText) }),
                        decorationBox = { inner ->
                            if (searchText.isEmpty()) Text("Search or enter", color = muted, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                            inner()
                        }
                    )
                    IconButton(onClick = onShowBookmarks, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.StarBorder, "Bookmarks", tint = muted)
                    }
                }
            }
            IconButton(onClick = onNewTab, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Add, "New tab", tint = muted) }
            IconButton(onClick = onShowSettings, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.MoreVert, "More options", tint = muted) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f))

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(34.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(25.dp).clip(CircleShape).background(blue.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(ColorGreen))
                }
                Spacer(Modifier.width(10.dp))
                Text("Y O U R   S P A C E,   Y O U R   P A C E", color = muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }

            Spacer(Modifier.height(34.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(30.dp), color = blue.copy(alpha = .12f), modifier = Modifier.size(58.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, null, tint = blue, modifier = Modifier.size(34.dp))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "A little more room to\nroam.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-1.5).sp),
                fontWeight = FontWeight.Bold,
                color = ink
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "A calmer, faster place to explore the web.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = muted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                placeholder = { Text("What are you looking for?", color = muted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = muted) },
                trailingIcon = {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        IconButton(onClick = { if (searchText.isNotBlank()) onSearch(searchText) }, modifier = Modifier.size(42.dp)) {
                            Text("↵", color = blue, fontSize = 20.sp)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { if (searchText.isNotBlank()) onSearch(searchText) }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = blue,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = ColorGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("Private by design", color = muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(22.dp))
                Icon(Icons.Default.Smartphone, null, tint = ColorGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("Search or enter a URL", color = muted, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.height(48.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Quick access", style = MaterialTheme.typography.titleLarge, color = ink, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Your favorite corners of the internet.", color = muted, style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = onShowBookmarks) {
                    Text("All bookmarks  →", color = blue, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuickAccessTile(Modifier.weight(1f), Icons.Default.Book, "Wikipedia", blue) { onSearch("https://www.wikipedia.org") }
                QuickAccessTile(Modifier.weight(1f), Icons.Default.PlayArrow, "YouTube", MaterialTheme.colorScheme.error) { onSearch("https://m.youtube.com") }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuickAccessTile(Modifier.weight(1f), Icons.Default.Code, "GitHub", ink) { onSearch("https://github.com") }
                QuickAccessTile(Modifier.weight(1f), Icons.Default.Forum, "Reddit", ColorGreen) { onSearch("https://www.reddit.com") }
            }
            Spacer(Modifier.height(28.dp))
        }

        // Persistent bottom navigation
        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                Modifier.fillMaxWidth().height(58.dp).navigationBarsPadding().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomItem(Icons.Default.Home, "Home", true, onClick = {})
                BottomItem(Icons.Default.BookmarkBorder, "Saved", false, onClick = onShowBookmarks)
                BottomItem(Icons.Default.History, "History", false, onClick = onShowHistory)
                BottomItem(Icons.Default.Language, "AI", false, onClick = onShowSettings)
                BottomItem(Icons.Default.Tab, "Tabs", false, onClick = onShowTabs, badge = tabCount)
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(72.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Surface(shape = RoundedCornerShape(14.dp), color = tint.copy(alpha = .12f), modifier = Modifier.size(38.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(23.dp)) }
            }
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun BottomItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: Int? = null
) {
    Column(
        Modifier.width(64.dp).clickable(onClick = onClick).padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
            if (badge != null && badge > 0) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.TopEnd).offset(x = 9.dp, y = (-5).dp).size(17.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(badge.toString(), color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp) }
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

private val ColorGreen = androidx.compose.ui.graphics.Color(0xFF218A68)
