package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.uikit.components.ColorPalette
import com.layzen.bdotools.uikit.components.ContentFrame
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.components.ToolIntro
import com.layzen.bdotools.uikit.components.TooltipPanel
import com.layzen.bdotools.uikit.components.TooltipSection
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_intro_first
import com.layzen.bdotools.uikit.resources.showcase_intro_image
import com.layzen.bdotools.uikit.resources.showcase_intro_second
import com.layzen.bdotools.uikit.resources.showcase_page_apply
import com.layzen.bdotools.uikit.resources.showcase_page_body
import com.layzen.bdotools.uikit.resources.showcase_page_menu
import com.layzen.bdotools.uikit.resources.showcase_page_open
import com.layzen.bdotools.uikit.resources.showcase_palette_default
import com.layzen.bdotools.uikit.resources.showcase_tooltip_section
import com.layzen.bdotools.uikit.resources.showcase_tooltip_section_body
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val FRAME_TILES = 4

/** Пример страницы «по центру»: подложка с заголовком, сеткой плиток и строкой действий под ней, ниже — `ToolIntro`, `ColorPalette` и `TooltipPanel` с `TooltipSection`. */
@Composable
internal fun ContentFrameSample(modifier: Modifier) {
    var paletteChoice by remember { mutableStateOf<Int?>(null) }
    ContentFrame(modifier) {
        SectionHeader(stringResource(Res.string.showcase_page_menu), description = stringResource(Res.string.showcase_page_body))
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            repeat(FRAME_TILES) { Tile(Modifier.size(ShowcaseDimens.tileSize)) { Skeleton(Modifier.matchParentSize()) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            MenuAction(stringResource(Res.string.showcase_page_open), onClick = {}, modifier = Modifier.weight(1f))
            MenuAction(stringResource(Res.string.showcase_page_apply), onClick = {}, modifier = Modifier.weight(1f))
        }
        ToolIntro(
            points = listOf(stringResource(Res.string.showcase_intro_first), stringResource(Res.string.showcase_intro_second)),
            image = painterResource(CharacterClass.entries.first().icon),
            imageDescription = stringResource(Res.string.showcase_intro_image),
            modifier = Modifier.width(ShowcaseDimens.introWidth),
        )
        ColorPalette(
            colors = listOf(AppTheme.colors.accentGold, AppTheme.colors.textNote, AppTheme.colors.accentBlue, AppTheme.colors.accentRed),
            selected = paletteChoice,
            onSelect = { paletteChoice = it },
            defaultColor = AppTheme.colors.textPrimary,
            defaultLabel = stringResource(Res.string.showcase_palette_default),
        )
        TooltipPanel {
            TooltipSection(Modifier.width(ShowcaseDimens.introWidth), title = stringResource(Res.string.showcase_tooltip_section)) {
                DescriptionText(stringResource(Res.string.showcase_tooltip_section_body))
            }
        }
    }
}
