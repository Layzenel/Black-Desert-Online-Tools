package com.layzen.bdotools.composable.overlay

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
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

/** Окно меню оверлея: двигается отдельно от кнопки. */
internal const val PANEL_ID = "menuPanel"

/** Нижний край окна у [OverlayAnchor.BottomCenter] — доля высоты экрана: выше полоски здоровья. */
private const val BOTTOM_SHARE = 0.78f

/** Отступ окна у [OverlayAnchor.TopRight] от правого верхнего угла экрана. */
private const val TOP_RIGHT_MARGIN = 220f

/** Отступ окна у [OverlayAnchor.CenterLeft] от левого края экрана. */
private const val LEFT_MARGIN = 40f

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
        OverlayAnchor.CenterLeft -> WindowPosition(LEFT_MARGIN.dp, (screen.height.dp - size.height) / 2)
    }
}

/** Кнопка меню: где её оставили, а если не двигали — слева от оверлея комбо, на уровне его верха. */
internal fun savedMenuPosition(): WindowPosition {
    saved(MENU_ID)?.let { return it }
    val combo = savedPosition(COMBO_ID, OverlayAnchor.BottomCenter, OverlayDimens.comboWindow)
    return WindowPosition(combo.x - OverlayDimens.menuButton - OverlayDimens.menuGap, combo.y)
}

/**
 * Меню оверлея (левый верхний угол): где его оставили, а если не двигали — под кнопкой, в правой половине экрана
 * по её правому краю.
 */
internal fun savedPanelPosition(): DpOffset {
    saved(PANEL_ID)?.let { return DpOffset(it.x, it.y) }
    val button = savedMenuPosition()
    val screenWidth = Toolkit.getDefaultToolkit().screenSize.width.dp
    val rightHalf = button.x + OverlayDimens.menuButton / 2 > screenWidth / 2
    val x = if (rightHalf) button.x + OverlayDimens.menuButton - OverlayDimens.panelWidth else button.x
    return DpOffset(x, button.y + OverlayDimens.menuButton + OverlayDimens.menuGap)
}

/** Запоминает место окна [id] после перетаскивания. */
internal fun savePosition(id: String, x: Dp, y: Dp) {
    val (xKey, yKey) = keys(id)
    preferences.putFloat(xKey, x.value)
    preferences.putFloat(yKey, y.value)
    preferences.flush()
}
