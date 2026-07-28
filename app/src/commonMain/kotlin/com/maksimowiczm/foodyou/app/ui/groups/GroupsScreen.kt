package com.maksimowiczm.foodyou.app.ui.groups

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.shell.PlaceholderTabScreen
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Groups tab. A stub for now: Story 2 adds the My Profile card, Story 7 the Friends card, and
 * Story 9 the group cards.
 */
@Composable
internal fun GroupsScreen(modifier: Modifier = Modifier) {
    PlaceholderTabScreen(title = stringResource(Res.string.tab_groups), modifier = modifier)
}
