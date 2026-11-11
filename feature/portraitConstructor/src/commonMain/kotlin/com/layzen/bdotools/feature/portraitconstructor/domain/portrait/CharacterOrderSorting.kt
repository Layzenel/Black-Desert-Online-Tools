package com.layzen.bdotools.feature.portraitconstructor.domain.portrait

/**
 * Раскладывает портреты в порядке окна выбора персонажа.
 * Персонажи без записи в [order] идут в конце, по возрастанию ID.
 */
fun sortByCharacterOrder(characterIds: List<String>, order: Map<String, Int>): List<String> =
    characterIds.sortedWith(
        compareBy<String>({ order[it] ?: Int.MAX_VALUE }, { it.length }, { it }),
    )
