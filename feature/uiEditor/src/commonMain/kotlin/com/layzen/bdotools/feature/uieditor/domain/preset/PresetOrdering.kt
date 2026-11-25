package com.layzen.bdotools.feature.uieditor.domain.preset

/**
 * Пресеты в порядке [order] (названия); те, которых в нём нет (новые), идут в конце по алфавиту.
 */
fun List<NamedLayout>.orderedBy(order: List<String>): List<NamedLayout> {
    val position = order.withIndex().associate { (index, name) -> name to index }
    return sortedWith(compareBy<NamedLayout> { position[it.name] ?: Int.MAX_VALUE }.thenBy { it.name.lowercase() })
}

/** Список с элементом с места [from], перенесённым на место [to]; неверные индексы список не меняют. */
fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
