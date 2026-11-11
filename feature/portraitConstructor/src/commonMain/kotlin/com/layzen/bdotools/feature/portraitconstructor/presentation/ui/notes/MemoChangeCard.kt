package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.MemoChange
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitTexture
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_added
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val PORTRAIT_RATIO = PortraitTexture.WIDTH.toFloat() / PortraitTexture.HEIGHT

/**
 * Строка списка изменений в стиле окна «Просмотр персонажей аккаунта» игры: мини-портрет вместо иконки класса,
 * новая заметка оранжевым, рядом зачёркнутая прежняя (или пометка, что заметки не было).
 */
@Composable
internal fun MemoChangeCard(change: MemoChange, portrait: ImageBitmap?) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundRaised)
            .border(dimens.borderWidth, colors.border)
            .padding(horizontal = dimens.spaceS, vertical = dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(PortraitsDimens.noteThumbWidth).aspectRatio(PORTRAIT_RATIO)) {
            if (portrait == null) {
                Skeleton(Modifier.matchParentSize())
            } else {
                Image(
                    portrait,
                    contentDescription = change.characterId,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Medium,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceS), verticalAlignment = Alignment.CenterVertically) {
            Text(
                change.new,
                style = AppTheme.typography.sectionTitle,
                color = colors.textNote,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (change.old.isEmpty()) {
                Text(stringResource(Res.string.portraits_notes_added), style = AppTheme.typography.caption, color = colors.textDisabled)
            } else {
                Text(
                    change.old,
                    style = AppTheme.typography.caption.copy(textDecoration = TextDecoration.LineThrough),
                    color = colors.textDisabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}
