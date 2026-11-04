package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.components.Toast
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_feedback_error
import com.layzen.bdotools.uikit.resources.showcase_feedback_message
import com.layzen.bdotools.uikit.resources.showcase_feedback_show
import com.layzen.bdotools.uikit.resources.showcase_feedback_show_error
import com.layzen.bdotools.uikit.resources.showcase_feedback_title
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val SKELETON_TILES = 4

/** Пример ожидания и обратной связи: плитки-скелетоны и всплывающее сообщение. */
@Composable
internal fun FeedbackSample(modifier: Modifier) {
    var toast by remember { mutableStateOf<Boolean?>(null) }
    GameWindow(
        title = stringResource(Res.string.showcase_feedback_title),
        modifier = modifier,
        footer = {
            ActionButton(stringResource(Res.string.showcase_feedback_show_error), onClick = { toast = true })
            ActionButton(stringResource(Res.string.showcase_feedback_show), onClick = { toast = false })
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceM)) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                repeat(SKELETON_TILES) {
                    Tile(Modifier.size(ShowcaseDimens.tileSize)) { Skeleton(Modifier.matchParentSize()) }
                }
            }
        }
    }
    val isError = toast == true
    Toast(
        text = toast?.let { stringResource(if (isError) Res.string.showcase_feedback_error else Res.string.showcase_feedback_message) },
        onDismiss = { toast = null },
        isError = isError,
    )
}
