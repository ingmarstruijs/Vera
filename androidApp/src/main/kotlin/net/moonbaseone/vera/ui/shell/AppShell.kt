package net.moonbaseone.vera.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.blueprint.BlueprintScreen
import net.moonbaseone.vera.ui.common.PlaceholderScreen

private data class TabDest(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    blueprintViewModel: BlueprintViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val tabs = listOf(
        TabDest("timeline", UiCopy.NAV_BLUEPRINT, Icons.AutoMirrored.Outlined.EventNote),
        TabDest("decoder", UiCopy.NAV_DECODER, Icons.Outlined.Translate),
        TabDest("appointments", UiCopy.NAV_APPOINTMENTS, Icons.Outlined.CalendarMonth),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(UiCopy.APP_NAME, style = MaterialTheme.typography.titleLarge)
                        Text(
                            " ·",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                },
                actions = {
                    PrivacySentinelDot()
                },
            )
        },
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
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
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
                BlueprintScreen(viewModel = blueprintViewModel)
            }
            composable("decoder") {
                PlaceholderScreen(
                    title = UiCopy.NAV_DECODER,
                    body = UiCopy.DECODER_PLACEHOLDER,
                )
            }
            composable("appointments") {
                PlaceholderScreen(
                    title = UiCopy.NAV_APPOINTMENTS,
                    body = UiCopy.APPOINTMENTS_PLACEHOLDER,
                )
            }
        }
    }
}

@Composable
private fun PrivacySentinelDot() {
    val label = "${UiCopy.SENTINEL_LOCAL_TITLE}. ${UiCopy.SENTINEL_LOCAL_SUBTITLE}"
    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(48.dp)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(DesignTokens.SENTINEL_GREEN), CircleShape),
        )
    }
}
