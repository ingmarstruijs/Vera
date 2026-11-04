package net.moonbaseone.vera.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import net.moonbaseone.vera.R
import net.moonbaseone.vera.shared.VeraShared
import net.moonbaseone.vera.shared.platformName

private data class TabDest(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun VeraApp() {
    val tabs = listOf(
        TabDest("timeline", R.string.tab_timeline, Icons.AutoMirrored.Outlined.EventNote),
        TabDest("decoder", R.string.tab_decoder, Icons.Outlined.Translate),
        TabDest("appointments", R.string.tab_appointments, Icons.Outlined.CalendarMonth),
    )
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "timeline",
            modifier = Modifier.padding(padding),
        ) {
            composable("timeline") {
                PlaceholderScreen(
                    title = stringResource(R.string.tab_timeline),
                    body = "Blueprint timeline placeholder. Shared: ${VeraShared.APP_NAME} on ${platformName()}.",
                )
            }
            composable("decoder") {
                PlaceholderScreen(
                    title = stringResource(R.string.tab_decoder),
                    body = "Plain-language decoder placeholder. No feature port yet (P2+).",
                )
            }
            composable("appointments") {
                PlaceholderScreen(
                    title = stringResource(R.string.tab_appointments),
                    body = "Appointment navigator placeholder. No sync tab in v1.",
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
