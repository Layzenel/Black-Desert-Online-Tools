package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_class_search
import com.layzen.bdotools.uikit.components.CloseButton
import com.layzen.bdotools.uikit.components.DropdownSelect
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.icons.title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Класс комбо: поле поиска над списком классов — список показывает только классы, в названии которых есть набранное.
 * [selected] — выбранный класс, [onSelect] — выбор другого, [tint] — цвет иконок классов.
 */
@Composable
internal fun ClassSelect(selected: CharacterClass, onSelect: (CharacterClass) -> Unit, tint: ColorFilter) {
    var query by remember { mutableStateOf("") }
    val titles = CharacterClass.entries.associateWith { stringResource(it.title) }
    val text = query.trim()
    val shown = CharacterClass.entries.filter { text.isEmpty() || titles.getValue(it).contains(text, ignoreCase = true) }
    InputField(
        query, { query = it }, Modifier.fillMaxWidth(),
        placeholder = stringResource(Res.string.combo_class_search),
        trailing = if (query.isEmpty()) null else { { CloseButton(onClick = { query = "" }) } },
    )
    DropdownSelect(
        selected, shown, onSelect, { titles.getValue(it) }, Modifier.fillMaxWidth(),
        icon = { Image(painterResource(it.icon), null, Modifier.size(ComboDimens.paletteIcon), colorFilter = tint) },
    )
}
