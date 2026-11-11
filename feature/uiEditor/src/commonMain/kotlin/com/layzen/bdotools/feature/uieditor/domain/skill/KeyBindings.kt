package com.layzen.bdotools.feature.uieditor.domain.skill

import com.layzen.bdotools.feature.uieditor.domain.game.attributesOf

private val ACTION_KEY = Regex("""<ShortcutKey_Action_Key\s[^>]*/>""")

/**
 * Клавиши действий персонажа: имя действия (`InputType`, как в `{KeyBind:Dash}` команды навыка) → виртуальный код
 * клавиши Windows (`keyCode`: 1 — левая кнопка мыши, 16 — Shift, 81 — Q). Раскладка у каждого персонажа своя
 * и лежит в его `gamevariable.xml` (`ShortcutKey_Action_Key`).
 *
 * @property keyCodes код клавиши по действию; действия без клавиши (код 0) здесь нет
 */
data class KeyBindings(val keyCodes: Map<String, Int>) {

    /** Код клавиши действия [action] или `null`, если клавиши нет. */
    operator fun get(action: String): Int? = keyCodes[action]

    companion object {
        /** Клавиши по умолчанию в игре — для команд навыков, пока персонаж не выбран. */
        val Default = KeyBindings(
            mapOf(
                "MoveFront" to 87, "MoveBack" to 83, "MoveLeft" to 65, "MoveRight" to 68,
                "Attack1" to 1, "Attack2" to 2, "Dash" to 16, "Jump" to 32,
                "CrouchOrSkill" to 81, "GrabOrGuard" to 69, "Kick" to 70,
                "ServantOrder1" to 90, "ServantOrder2" to 88, "ServantOrder3" to 67,
            ),
        )
    }
}

/** Клавиши действий из файла персонажа ([text] — по символу на байт); действия без клавиши пропускаются. */
fun parseKeyBindings(text: String): KeyBindings = KeyBindings(
    ACTION_KEY.findAll(text).mapNotNull { match ->
        val attributes = attributesOf(match.value)
        val action = attributes["InputType"] ?: return@mapNotNull null
        val keyCode = attributes["keyCode"]?.toIntOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
        action to keyCode
    }.toMap(),
)
