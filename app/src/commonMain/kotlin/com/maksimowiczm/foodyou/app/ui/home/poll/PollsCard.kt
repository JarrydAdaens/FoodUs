package com.maksimowiczm.foodyou.app.ui.home.poll

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.poll.domain.entity.LinkPoll
import com.maksimowiczm.foodyou.poll.domain.entity.Poll
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PollsCard(modifier: Modifier = Modifier) {
    val viewModel: PollsViewModel = koinViewModel()
    val uriHandler = LocalUriHandler.current

    val polls by viewModel.polls.collectAsStateWithLifecycle()

    PollsCard(
        polls = polls,
        onDismiss = { poll -> viewModel.dismissPoll(poll.id) },
        onOpen = { poll ->
            when (poll) {
                is LinkPoll -> uriHandler.openUri(poll.url)
            }
        },
        modifier = (modifier).uiName("PollsCard.PollsCard.1", "com.maksimowiczm.foodyou.app.ui.home.poll.PollsCard", uiInspectionSource, "PollsCard"),
    )
}

@Composable
private fun PollsCard(
    polls: List<Poll>,
    onDismiss: (Poll) -> Unit,
    onOpen: (Poll) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (polls.isEmpty()) {
        return
    }

    Column(modifier = (modifier).uiName("PollsCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "PollsCard"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        polls.forEach { poll ->
            when (poll) {
                is LinkPoll ->
                    LinkPollCard(
                        poll = poll,
                        onDismiss = { onDismiss(poll) },
                        onOpen = { onOpen(poll) }, modifier = Modifier.uiName("PollsCard.LinkPollCard.1", "com.maksimowiczm.foodyou.app.ui.home.poll.LinkPollCard", uiInspectionSource, "PollsCard")
                    )
            }
        }
    }
}

@Composable
private fun LinkPollCard(
    poll: LinkPoll,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = (modifier).uiName("LinkPollCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "LinkPollCard"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(
            modifier = (Modifier.padding(16.dp)).uiName("LinkPollCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LinkPollCard"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = poll.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.uiName("LinkPollCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LinkPollCard"))
            Text(text = poll.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("LinkPollCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "LinkPollCard"))
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("LinkPollCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "LinkPollCard"),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onDismiss, modifier = Modifier.uiName("LinkPollCard.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "LinkPollCard")) { Text(stringResource(Res.string.action_dont_show_again), modifier = Modifier.uiName("LinkPollCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "LinkPollCard")) }
                Button(onOpen, modifier = Modifier.uiName("LinkPollCard.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "LinkPollCard")) { Text(stringResource(Res.string.action_open_poll), modifier = Modifier.uiName("LinkPollCard.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "LinkPollCard")) }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.poll",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/poll/PollsCard.kt",
)
