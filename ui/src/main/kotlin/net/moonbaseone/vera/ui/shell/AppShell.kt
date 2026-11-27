package net.moonbaseone.vera.ui.shell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.appointments.ContactsViewModel
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.decoder.DecoderViewModel
import net.moonbaseone.vera.shared.navigation.AppDestination
import net.moonbaseone.vera.shared.pocket.PocketGuideViewModel
import net.moonbaseone.vera.shared.symptoms.SymptomsViewModel
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.appointments.AppointmentsScreen
import net.moonbaseone.vera.ui.blueprint.BlueprintScreen
import net.moonbaseone.vera.ui.blueprint.DebugDatasetPickerDialog
import net.moonbaseone.vera.ui.contacts.ContactsScreen
import net.moonbaseone.vera.ui.decoder.DecoderScreen
import net.moonbaseone.vera.ui.pocket.PocketGuideScreen
import net.moonbaseone.vera.ui.symptoms.SymptomRecordSheetContent
import net.moonbaseone.vera.ui.symptoms.SymptomsScreen
import org.koin.compose.getKoin

private data class TabDest(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppShell(
    blueprintViewModel: BlueprintViewModel,
    decoderViewModel: DecoderViewModel,
    appointmentsViewModel: AppointmentsViewModel,
    contactsViewModel: ContactsViewModel,
    symptomsViewModel: SymptomsViewModel,
    isDebug: Boolean = false,
    navController: NavHostController = rememberNavController(),
) {
    val tabs = listOf(
        TabDest(AppDestination.CarePlan, UiCopy.NAV_CARE_PLAN, Icons.AutoMirrored.Outlined.EventNote),
        TabDest(AppDestination.Decoder, UiCopy.NAV_DECODER, Icons.Outlined.Translate),
        TabDest(AppDestination.Appointments, UiCopy.NAV_APPOINTMENTS, Icons.Outlined.CalendarMonth),
        TabDest(AppDestination.Contacts, UiCopy.NAV_CONTACTS, Icons.Outlined.Call),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val route = currentDestination?.route
    val onPocketRoute = route?.startsWith("pocketGuide") == true
    val onSymptomsRoute = route == AppDestination.Symptoms.route
    var showRecordSheet by remember { mutableStateOf(false) }
    var showDatasetPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val headerTitle = when (route) {
        AppDestination.CarePlan.route -> UiCopy.BLUEPRINT_TITLE
        AppDestination.Decoder.route -> UiCopy.DECODER_TITLE
        AppDestination.Appointments.route -> UiCopy.APPOINTMENTS_TITLE
        AppDestination.Contacts.route -> UiCopy.APPOINTMENTS_CONTACTS_TITLE
        AppDestination.Symptoms.route -> UiCopy.SYMPTOMS_TITLE
        else -> UiCopy.APP_NAME
    }
    // Debug builds only: long-press the care-plan title to switch demo datasets.
    val titleModifier = if (isDebug && route == AppDestination.CarePlan.route) {
        Modifier.combinedClickable(
            onClick = {},
            onLongClick = { showDatasetPicker = true },
        )
    } else {
        Modifier
    }

    fun closeSymptoms() {
        if (!navController.popBackStack()) {
            navController.navigate(AppDestination.CarePlan.route) {
                launchSingleTop = true
            }
        }
    }

    BackHandler(enabled = onSymptomsRoute) { closeSymptoms() }

    if (isDebug && showDatasetPicker) {
        DebugDatasetPickerDialog(
            viewModel = blueprintViewModel,
            onDismiss = { showDatasetPicker = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        // TopAppBar already accounts for status bars; avoid double top inset padding.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!onPocketRoute) {
                TopAppBar(
                    expandedHeight = 48.dp,
                    windowInsets = WindowInsets.statusBars,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    navigationIcon = {
                        if (onSymptomsRoute) {
                            IconButton(
                                onClick = { closeSymptoms() },
                                modifier = Modifier.semantics {
                                    contentDescription = UiCopy.APPOINTMENTS_CLOSE_FULL
                                },
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = UiCopy.APPOINTMENTS_CLOSE_FULL,
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            headerTitle,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = titleModifier.semantics { heading() },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        if (!onSymptomsRoute) {
                            IconButton(
                                onClick = { showRecordSheet = true },
                                modifier = Modifier.semantics {
                                    contentDescription = UiCopy.SYMPTOMS_RECORD_CTA
                                },
                            ) {
                                Icon(Icons.Outlined.MicNone, contentDescription = UiCopy.SYMPTOMS_RECORD_CTA)
                            }
                            IconButton(
                                onClick = {
                                    navController.navigate(AppDestination.Symptoms.route) {
                                        launchSingleTop = true
                                    }
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = UiCopy.SYMPTOMS_TITLE
                                },
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = UiCopy.SYMPTOMS_TITLE)
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            // Pocket + symptoms are overlays; use ✕ / system back to leave symptoms.
            if (!onPocketRoute && !onSymptomsRoute) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
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
                AppointmentsScreen(
                    viewModel = appointmentsViewModel,
                    onOpenPocketGuide = { id ->
                        navController.navigate(AppDestination.PocketGuide.routeFor(id))
                    },
                )
            }
            composable(AppDestination.Contacts.route) {
                ContactsScreen(viewModel = contactsViewModel)
            }
            composable(AppDestination.Symptoms.route) {
                SymptomsScreen(viewModel = symptomsViewModel)
            }
            composable(
                route = AppDestination.PocketGuide.routePattern,
                arguments = listOf(navArgument("appointmentId") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("appointmentId").orEmpty()
                val koin = getKoin()
                val pocketVm = remember(id) { koin.get<PocketGuideViewModel>() }
                PocketGuideScreen(
                    appointmentId = id,
                    viewModel = pocketVm,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }

    if (showRecordSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                symptomsViewModel.cancelListening()
                showRecordSheet = false
            },
            sheetState = sheetState,
        ) {
            SymptomRecordSheetContent(
                viewModel = symptomsViewModel,
                onFinished = {
                    symptomsViewModel.cancelListening()
                    showRecordSheet = false
                },
            )
        }
    }
}
