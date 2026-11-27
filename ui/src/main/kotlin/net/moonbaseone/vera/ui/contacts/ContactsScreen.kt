package net.moonbaseone.vera.ui.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.appointments.CareContact
import net.moonbaseone.vera.shared.appointments.ContactsViewModel
import net.moonbaseone.vera.shared.i18n.AppLocale
import net.moonbaseone.vera.shared.i18n.LocaleController
import net.moonbaseone.vera.shared.i18n.StringCatalog
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.common.SoftIconCircle
import net.moonbaseone.vera.ui.common.VeraCard
import net.moonbaseone.vera.ui.theme.lavenderWash
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    localeController: LocaleController = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val activeLocale by StringCatalog.activeLocaleFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        state.errorMessage?.let { msg ->
            item {
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (state.contacts.isEmpty()) {
            item {
                Text(
                    UiCopy.APPOINTMENTS_NO_DATA,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            items(state.contacts, key = { it.id }) { contact ->
                ContactCard(
                    contact = contact,
                    onCall = {
                        if (contact.phone.isNotBlank()) {
                            val dial = contact.phone.filter { it.isDigit() || it == '+' }
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dial")),
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                UiCopy.SETTINGS_LANGUAGE_LABEL,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppLocale.entries.forEach { locale ->
                    FilterChip(
                        selected = locale == activeLocale,
                        onClick = { localeController.select(locale) },
                        label = { Text(locale.displayName) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ContactCard(
    contact: CareContact,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VeraCard(modifier = modifier.padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SoftIconCircle(
                icon = Icons.Outlined.Person,
                size = 48.dp,
                wash = lavenderWash(),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                if (contact.role.isNotBlank()) {
                    Text(
                        contact.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (contact.organization.isNotBlank()) {
                    Text(
                        contact.organization,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (contact.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        contact.phone,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        if (contact.phone.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Call,
                    contentDescription = UiCopy.APPOINTMENTS_CALL,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(UiCopy.APPOINTMENTS_CALL)
            }
        }
    }
}
