package net.moonbaseone.vera.ui.shell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
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
import net.moonbaseone.vera.shared.i18n.LocaleController
import net.moonbaseone.vera.shared.i18n.StringCatalog
import net.moonbaseone.vera.shared.pocket.PocketGuideViewModel
import net.moonbaseone.vera.shared.symptoms.SymptomsViewModel
import net.moonbaseone.vera.shared.translation.DatasetContentLocalizer
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay
import net.moonbaseone.vera.ui.appointments.AppointmentsScreen
import net.moonbaseone.vera.ui.blueprint.BlueprintScreen
import net.moonbaseone.vera.ui.blueprint.DebugDatasetPickerDialog
import net.moonbaseone.vera.ui.common.SoftRoundIconButton
import net.moonbaseone.vera.ui.common.SoftScreenBackground
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
    val headerSubtitle = when (route) {
        AppDestination.CarePlan.route -> UiCopy.BLUEPRINT_SUBTITLE
        AppDestination.Decoder.route -> UiCopy.DECODER_SUBTITLE
        AppDestination.Appointments.route -> UiCopy.APPOINTMENTS_NEXT_LABEL
        AppDestination.Contacts.route -> UiCopy.APPOINTMENTS_CONTACTS_SUBTITLE
        else -> null
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

    val appointmentsState by appointmentsViewModel.state.collectAsState()
    val nextAppointment = appointmentsState.next
    val locale by StringCatalog.activeLocaleFlow.collectAsState()
    val localeController = koinInject<LocaleController>()
    val contentLocalizer = koinInject<DatasetContentLocalizer>()
    val contentStatus by contentLocalizer.status.collectAsState()
    val decoderState by decoderViewModel.state.collectAsState()
    val switchPending by localeController.switchInProgress.collectAsState()

    val localizationBusy = contentStatus.isActive ||
        decoderState.isTranslating ||
        decoderState.downloadProgress != null
    val showLocaleDialog = switchPending || localizationBusy
    val localeProgress = when {
        decoderState.downloadProgress != null -> decoderState.downloadProgress
        decoderState.translationProgress != null -> decoderState.translationProgress
        contentStatus.progress != null -> contentStatus.progress
        else -> null
    }
    val localeDialogMessage = when {
        decoderState.downloadProgress != null -> UiCopy.DECODER_DOWNLOADING_MODEL
        else -> UiCopy.DECODER_TRANSLATING
    }

    LaunchedEffect(locale) {
        appointmentsViewModel.load()
    }
    // Close the global switch dialog once workers have started and finished.
    LaunchedEffect(switchPending, localizationBusy) {
        if (!switchPending) return@LaunchedEffect
        if (localizationBusy) return@LaunchedEffect
        // Brief grace so parallel jobs can register as active after setLocale.
        delay(150)
        if (!contentLocalizer.status.value.isActive) {
            val decoder = decoderViewModel.state.value
            if (!decoder.isTranslating && decoder.downloadProgress == null) {
                localeController.markSwitchFinished()
            }
        }
    }

    if (showLocaleDialog) {
        LocaleSwitchProgressDialog(
            message = localeDialogMessage,
            progress = localeProgress,
        )
    }

    // One continuous wash behind header + content so the app header matches the page.
    SoftScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (!onPocketRoute) {
                    VeraAppHeader(
                        title = headerTitle,
                        subtitle = headerSubtitle,
                        titleModifier = titleModifier,
                        showClose = onSymptomsRoute,
                        onClose = { closeSymptoms() },
                        actions = {
                            if (!onSymptomsRoute) {
                                SoftRoundIconButton(
                                    icon = Icons.Outlined.MicNone,
                                    contentDescription = UiCopy.SYMPTOMS_RECORD_CTA,
                                    onClick = { showRecordSheet = true },
                                )
                                SoftRoundIconButton(
                                    icon = Icons.Outlined.FavoriteBorder,
                                    contentDescription = UiCopy.SYMPTOMS_TITLE,
                                    onClick = {
                                        navController.navigate(AppDestination.Symptoms.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                )
                            }
                        },
                    )
                }
            },
            bottomBar = {
                if (!onPocketRoute && !onSymptomsRoute) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)) {
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
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
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
                    BlueprintScreen(
                        viewModel = blueprintViewModel,
                        nextAppointmentWhen = nextAppointment?.appointment?.startIso
                            ?.takeIf { it.isNotBlank() }
                            ?.let { DateDisplay.formatIso(it) },
                        nextAppointmentTitle = nextAppointment?.appointment?.serviceType,
                        onOpenAppointments = {
                            navController.navigate(AppDestination.Appointments.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
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
                onFinished = { showRecordSheet = false },
            )
        }
    }
}

@Composable
private fun LocaleSwitchProgressDialog(
    message: String,
    progress: Float?,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(UiCopy.SETTINGS_LANGUAGE_LABEL) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(message, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {},
    )
}

/** Single app header: title + subtitle left, actions right — one visual band. */
@Composable
private fun VeraAppHeader(
    title: String,
    subtitle: String?,
    titleModifier: Modifier,
    showClose: Boolean,
    onClose: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showClose) {
            SoftRoundIconButton(
                icon = Icons.Outlined.Close,
                contentDescription = UiCopy.APPOINTMENTS_CLOSE_FULL,
                onClick = onClose,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = titleModifier.semantics { heading() },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}
