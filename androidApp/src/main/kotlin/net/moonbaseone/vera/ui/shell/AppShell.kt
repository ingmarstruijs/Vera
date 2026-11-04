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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.decoder.DecoderViewModel
import net.moonbaseone.vera.shared.navigation.AppDestination
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.appointments.AppointmentsScreen
import net.moonbaseone.vera.ui.blueprint.BlueprintScreen
import net.moonbaseone.vera.ui.decoder.DecoderScreen

private data class TabDest(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    blueprintViewModel: BlueprintViewModel,
    decoderViewModel: DecoderViewModel,
    appointmentsViewModel: AppointmentsViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val tabs = listOf(
        TabDest(AppDestination.CarePlan, UiCopy.NAV_CARE_PLAN, Icons.AutoMirrored.Outlined.EventNote),
        TabDest(AppDestination.Decoder, UiCopy.NAV_DECODER, Icons.Outlined.Translate),
        TabDest(AppDestination.Appointments, UiCopy.NAV_APPOINTMENTS, Icons.Outlined.CalendarMonth),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Text(
                        UiCopy.APP_NAME,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                },
                actions = {
                    PrivacySentinelDot()
                },
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                tabs.forEach { tab ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == tab.destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.destination.route) {
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
            startDestination = AppDestination.CarePlan.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppDestination.CarePlan.route) {
                BlueprintScreen(viewModel = blueprintViewModel)
            }
            composable(AppDestination.Decoder.route) {
                DecoderScreen(viewModel = decoderViewModel)
            }
            composable(AppDestination.Appointments.route) {
                AppointmentsScreen(viewModel = appointmentsViewModel)
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
