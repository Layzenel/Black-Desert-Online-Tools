package com.layzen.bdotools.composable.overlay

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import java.awt.Toolkit
import java.util.prefs.Preferences

/** Места окон поверх игры; узел прежний, чтобы места, сохранённые раньше, не сбились. */
private val preferences: Preferences = Preferences.userRoot().node("bdo-tools/combo-overlay")

/** Окно оверлея комбо: его место лежит под старыми ключами `x` и `y`. */
private const val COMBO_ID = "combo"

/** Окно кнопки меню оверлея. */
internal const val MENU_ID = "menu"

/** Нижний край окна у [OverlayAnchor.BottomCenter] — доля высоты экрана: выше полоски здоровья. */
private const val BOTTOM_SHARE = 0.78f

/** Отступ окна у [OverlayAnchor.TopRight] от правого верхнего угла экрана. */
private const val TOP_RIGHT_MARGIN = 220f

private fun keys(id: String): Pair<String, String> = if (id == COMBO_ID) "x" to "y" else "${id}X" to "${id}Y"

/** Сохранённое место окна [id] или `null`, если его ещё не двигали. */
private fun saved(id: String): WindowPosition? {
    val (xKey, yKey) = keys(id)
    val x = preferences.getFloat(xKey, Float.NaN)
    val y = preferences.getFloat(yKey, Float.NaN)
    return if (x.isNaN() || y.isNaN()) null else WindowPosition(x.dp, y.dp)
}

/** Где встаёт окно [id] размером [size]: где его оставили, а если не двигали — у [anchor]. */
internal fun savedPosition(id: String, anchor: OverlayAnchor, size: DpSize): WindowPosition {
    saved(id)?.let { return it }
    val screen = Toolkit.getDefaultToolkit().screenSize
    return when (anchor) {
        OverlayAnchor.BottomCenter -> WindowPosition((screen.width.dp - size.width) / 2, screen.height.dp * BOTTOM_SHARE - size.height)
        OverlayAnchor.TopRight -> WindowPosition((screen.width - TOP_RIGHT_MARGIN * 2).dp, TOP_RIGHT_MARGIN.dp)
    }
}

/** Кнопка меню: где её оставили, а если не двигали — слева от оверлея комбо, на уровне его верха. */
internal fun savedMenuPosition(): WindowPosition {
    saved(MENU_ID)?.let { return it }
    val combo = savedPosition(COMBO_ID, OverlayAnchor.BottomCenter, OverlayDimens.comboWindow)
    return WindowPosition(combo.x - OverlayDimens.menuButton - OverlayDimens.menuGap, combo.y)
}

/** Запоминает место окна [id] после перетаскивания. */
internal fun savePosition(id: String, x: Dp, y: Dp) {
    val (xKey, yKey) = keys(id)
    preferences.putFloat(xKey, x.value)
    preferences.putFloat(yKey, y.value)
    preferences.flush()
}
