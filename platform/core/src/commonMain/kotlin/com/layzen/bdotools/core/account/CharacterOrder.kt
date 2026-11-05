package com.layzen.bdotools.core.account

private val orderEntry = Regex("""<CharacterOrderList\s+CharacterNo="(\d+)"\s+Order="(-?\d+)"\s*/>""")

/**
 * Достаёт порядок персонажей из `gamevariable.xml`.
 *
 * Файл игры не является корректным XML (у него нет корневого тега), поэтому записи
 * `<CharacterOrderList CharacterNo="…" Order="…"/>` ищутся регулярным выражением.
 * Возвращает `CharacterNo` → `Order`.
 */
fun parseCharacterOrder(gameVariableXml: String): Map<String, Int> =
    orderEntry.findAll(gameVariableXml).associate { it.groupValues[1] to it.groupValues[2].toInt() }
