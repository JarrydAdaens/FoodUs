package com.maksimowiczm.foodyou.app.ui.groups.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.profile.domain.Profile
import foodyou.app.generated.resources.*
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Top card of the Groups tab: creates the device's profile on demand and shows it afterwards.
 *
 * The profile GUID is deliberately never rendered — the friend code (Story 6) is the human-facing
 * handle, and it slots into the filled state below the dates.
 */
@Composable
internal fun MyProfileCard(modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = koinViewModel()
    val profile = viewModel.profile.collectAsStateWithLifecycle().value
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.headline_my_profile),
                style = MaterialTheme.typography.titleMedium,
            )

            if (profile == null) {
                EmptyProfileContent(onCreate = { showDialog = true })
            } else {
                ProfileContent(profile = profile, onEdit = { showDialog = true })
            }
        }
    }

    if (showDialog) {
        ProfileEditDialog(
            title =
                stringResource(
                    if (profile == null) Res.string.action_create_profile
                    else Res.string.headline_edit_profile
                ),
            initialUsername = profile?.username.orEmpty(),
            onDismissRequest = { showDialog = false },
            onConfirm = { username ->
                if (profile == null) viewModel.onCreate(username) else viewModel.onRename(username)
                showDialog = false
            },
        )
    }
}

@Composable
private fun EmptyProfileContent(onCreate: () -> Unit) {
    Text(
        text = stringResource(Res.string.description_no_profile),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onCreate) { Text(stringResource(Res.string.action_create_profile)) }
}

@Composable
private fun ProfileContent(profile: Profile, onEdit: () -> Unit) {
    Text(text = profile.username, style = MaterialTheme.typography.headlineSmall)

    ProfileDateRow(
        label = stringResource(Res.string.headline_created),
        epochSeconds = profile.createdEpochSeconds,
    )
    ProfileDateRow(
        label = stringResource(Res.string.headline_last_edited),
        epochSeconds = profile.lastEditedEpochSeconds,
    )

    // The friend code (Story 6) renders here, between the dates and the edit action.

    TextButton(onClick = onEdit) { Text(stringResource(Res.string.action_edit)) }
}

@Composable
private fun ProfileDateRow(label: String, epochSeconds: Long) {
    val date =
        Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(TimeZone.currentSystemDefault()).date

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = LocalDateFormatter.current.formatDateShort(date),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
