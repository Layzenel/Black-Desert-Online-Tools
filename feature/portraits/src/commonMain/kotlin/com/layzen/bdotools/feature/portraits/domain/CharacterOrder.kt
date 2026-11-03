package com.layzen.bdotools.feature.portraits.domain

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

/**
 * Раскладывает портреты в порядке окна выбора персонажа.
 * Персонажи без записи в [order] идут в конце, по возрастанию ID.
 */
fun sortByCharacterOrder(characterIds: List<String>, order: Map<String, Int>): List<String> =
    characterIds.sortedWith(
        compareBy<String>({ order[it] ?: Int.MAX_VALUE }, { it.length }, { it }),
    )

/**
 * Выбирает из нескольких `gamevariable.xml` (по одному на папку в `UserCache`) тот,
 * чей список персонажей лучше всего совпадает с файлами в `FaceTexture`.
 */
fun pickCharacterOrder(candidates: List<String>, characterIds: Collection<String>): Map<String, Int> =
    candidates
        .map(::parseCharacterOrder)
        .maxByOrNull { order -> characterIds.count { it in order } }
        .orEmpty()
