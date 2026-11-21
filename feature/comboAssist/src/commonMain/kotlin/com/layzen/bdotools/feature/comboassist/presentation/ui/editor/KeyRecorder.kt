package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.layzen.bdotools.core.skill.domain.KeyBindings
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_activation_recording
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Запись команды умения: поверх всего окна, чтобы клик мыши в любом месте попал в запись (Shift + ЛКМ ловился только
 * над полем). Игрок зажимает клавиши и кнопки мыши, а когда отпустил все — [onDone] получает действия игры, которые они
 * значат по раскладке игры по умолчанию ([KeyBindings.Default]), в порядке нажатия. Клавиши без действия игры не
 * записываются. Esc — отмена (пустой набор). [label] — подпись действия.
 */
@Composable
internal fun KeyRecorder(label: (String) -> String, onDone: (Set<String>) -> Unit) {
    val actionOf = remember { KeyBindings.Default.keyCodes.entries.associate { (action, code) -> code to action } }
    var actions by remember { mutableStateOf(listOf<String>()) }
    var held by remember { mutableStateOf(setOf<Int>()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    fun press(code: Int) {
        held = held + code
        actionOf[code]?.let { if (it !in actions) actions = actions + it }
    }
    fun release(code: Int) {
        held = held - code
        if (held.isEmpty() && actions.isNotEmpty()) onDone(actions.toCollection(LinkedHashSet()))
    }
    val text = if (actions.isEmpty()) stringResource(Res.string.combo_activation_recording) else actions.joinToString(" + ") { label(it) }
    Popup(alignment = Alignment.Center, onDismissRequest = { onDone(emptySet()) }, properties = PopupProperties(focusable = true)) {
        Box(
            Modifier.fillMaxSize().background(AppTheme.colors.backgroundDeep.copy(alpha = SCRIM_ALPHA))
                .focusRequester(focus).focusable()
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Escape) {
                        if (event.type == KeyEventType.KeyDown) onDone(emptySet())
                    } else when (event.type) {
                        KeyEventType.KeyDown -> KEY_CODES[event.key]?.let(::press)
                        KeyEventType.KeyUp -> KEY_CODES[event.key]?.let(::release)
                    }
                    true
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        var before = emptySet<Int>()
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type != PointerEventType.Press && event.type != PointerEventType.Release) continue
                            event.changes.forEach { it.consume() }
                            val now = MOUSE.filter { (_, pressed) -> pressed(event.buttons) }.map { it.first }.toSet()
                            (now - before).forEach(::press)
                            (before - now).forEach(::release)
                            before = now
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.background(AppTheme.colors.backgroundRaised, AppShapes.Small)
                    .border(AppTheme.dimens.borderWidth, AppTheme.colors.accentGold, AppShapes.Small)
                    .padding(horizontal = AppTheme.dimens.spaceL, vertical = AppTheme.dimens.spaceM),
            ) { Text(text, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textPrimary) }
        }
    }
}

/** Затемнение окна, пока идёт запись. */
private const val SCRIM_ALPHA = 0.7f

/** Кнопки мыши: код клавиши в раскладке игры (ЛКМ 1, ПКМ 2, СКМ 4) и зажата ли она. */
private val MOUSE: List<Pair<Int, (PointerButtons) -> Boolean>> = listOf(
    1 to { it.isPrimaryPressed },
    2 to { it.isSecondaryPressed },
    4 to { it.isTertiaryPressed },
)

/** Код клавиши в раскладке игры (как в `gamevariable.xml`): буквы — их код, Shift — 16, пробел — 32. */
private val KEY_CODES: Map<Key, Int> = mapOf(Key.ShiftLeft to 16, Key.ShiftRight to 16, Key.Spacebar to 32) +
    listOf(
        Key.A, Key.B, Key.C, Key.D, Key.E, Key.F, Key.G, Key.H, Key.I, Key.J, Key.K, Key.L, Key.M,
        Key.N, Key.O, Key.P, Key.Q, Key.R, Key.S, Key.T, Key.U, Key.V, Key.W, Key.X, Key.Y, Key.Z,
    ).mapIndexed { index, key -> key to 'A'.code + index }
