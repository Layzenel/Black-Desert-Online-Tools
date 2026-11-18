package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.core.xml.attributesOf

private val SLOT_SKILL = Regex("""<SkillCoolTimeSlot\s+SlotNo="\d+"[^>]*>""")
private val UI_DATA = Regex("""<UIData\s+Index="\d+"[^>]*>""")
private val UI_SCALE = Regex("""<UiScale\s+Value="([\d.]+)"""")
private val RESOLUTION_HEIGHT = Regex("""<Resolution\s+Width="\d+"\s+Height="(\d+)"""")

/** `UIData Index` слотов перезарядки 1–20 по порядку: 128–137 и 148–157. */
private val SLOT_INDICES = (128..137) + (148..157)

/** Умения в слотах перезарядки из файла персонажа: номер слота (с нуля) → умение; пустые слоты пропущены. */
internal fun parseSlotSkills(characterText: String): Map<Int, Int> = SLOT_SKILL.findAll(characterText)
    .map { attributesOf(it.value) }
    .mapNotNull { a -> a["SlotNo"]?.toIntOrNull()?.let { slot -> a["SkillNo"]?.toIntOrNull()?.takeIf { it > 0 }?.let { slot to it } } }
    .toMap()

/** Центры слотов перезарядки из файла аккаунта: номер слота → центр в долях экрана. */
internal fun parseSlotCenters(accountText: String): Map<Int, Pair<Double, Double>> = uiData(accountText)
    .mapNotNull { (slot, a) ->
        val x = a["RelativePosX"]?.toDoubleOrNull() ?: return@mapNotNull null
        val y = a["RelativePosY"]?.toDoubleOrNull() ?: return@mapNotNull null
        slot to (x to y)
    }.toMap()

/** Скрытые слоты: `IsShow="false"` в файле персонажа (видимость у каждого персонажа своя). */
internal fun parseHiddenSlots(characterText: String): Set<Int> =
    uiData(characterText).filter { (_, a) -> a["IsShow"] == "false" }.map { it.first }.toSet()

/** «Размер интерфейса» игры (100 % → 1) или `null`. */
internal fun parseUiScale(accountText: String): Double? = UI_SCALE.find(accountText)?.groupValues?.get(1)?.toDoubleOrNull()

/** Высота разрешения игры в пикселях или `null`. */
internal fun parseResolutionHeight(accountText: String): Int? = RESOLUTION_HEIGHT.find(accountText)?.groupValues?.get(1)?.toIntOrNull()

private fun uiData(text: String): List<Pair<Int, Map<String, String>>> = UI_DATA.findAll(text)
    .map { attributesOf(it.value) }
    .mapNotNull { a -> a["Index"]?.toIntOrNull()?.let { SLOT_INDICES.indexOf(it) }?.takeIf { it >= 0 }?.let { it to a } }
    .toList()
