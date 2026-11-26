package com.layzen.bdotools.feature.comboassist.data.slots

import com.layzen.bdotools.core.xml.attributesOf

private val SLOT_SKILL = Regex("""<SkillCoolTimeSlot\s+SlotNo="\d+"[^>]*>""")
private val HOTBAR_SKILL = Regex("""<QuickSlotSkillData\s+Key="\d+"[^>]*>""")
private val UI_DATA = Regex("""<UIData\s+Index="\d+"[^>]*>""")

/** Номер элемента «История испол. навыков» в `UIData`. */
private const val HISTORY_INDEX = "158"
private val UI_SCALE = Regex("""<UiScale\s+Value="([\d.]+)"""")
private val RESOLUTION_HEIGHT = Regex("""<Resolution\s+Width="\d+"\s+Height="(\d+)"""")

/** `UIData Index` слотов перезарядки 1–20 по порядку: 128–137 и 148–157. */
private val SLOT_INDICES = (128..137) + (148..157)

/**
 * `UIData Index` слотов хотбара 1–20 по порядку (`Key` 0–19 в `QuickSlotSkillData`): 100, 91–99, 110, 101–109 — тот же
 * порядок, что у хотбара в редакторе интерфейса.
 */
private val HOTBAR_INDICES = listOf(100) + (91..99) + 110 + (101..109)

/** Умения в слотах перезарядки из файла персонажа: номер слота (с нуля) → умение; пустые слоты пропущены. */
internal fun parseSlotSkills(characterText: String): Map<Int, Int> = skills(characterText, SLOT_SKILL, "SlotNo")

/** Умения на хотбаре из файла персонажа: номер слота (`Key`, с нуля) → умение; предметы и пустые слоты пропущены. */
internal fun parseHotbarSkills(characterText: String): Map<Int, Int> = skills(characterText, HOTBAR_SKILL, "Key")

/** Центры слотов перезарядки из файла аккаунта: номер слота → центр в долях экрана. */
internal fun parseSlotCenters(accountText: String): Map<Int, Pair<Double, Double>> = centers(accountText, SLOT_INDICES)

/** Центры слотов хотбара из файла аккаунта: номер слота → центр в долях экрана. */
internal fun parseHotbarCenters(accountText: String): Map<Int, Pair<Double, Double>> = centers(accountText, HOTBAR_INDICES)

/** Скрытые слоты: `IsShow="false"` в файле персонажа (видимость у каждого персонажа своя). */
internal fun parseHiddenSlots(characterText: String): Set<Int> = hidden(characterText, SLOT_INDICES)

/**
 * Скрытые слоты хотбара: `IsShow="false"` в файле персонажа. При выключенной «Настройке быстрых слотов» игра прячет
 * их все и показывает одну панель (`UIData Index="12"`).
 *
 * shortcut: умения на панели «Быстрый слот» не следятся — её слоты не описаны в файлах по отдельности; если понадобятся,
 * считать их места от позиции панели.
 */
internal fun parseHiddenHotbar(characterText: String): Set<Int> = hidden(characterText, HOTBAR_INDICES)

/**
 * Видна ли «История испол. навыков» (`UIData Index="158"`): `IsShow` в файле персонажа (видимость у каждого своя),
 * без записи там — в файле аккаунта; нигде нет — видна.
 */
internal fun parseHistoryShown(characterText: String, accountText: String): Boolean =
    sequenceOf(characterText, accountText).firstNotNullOfOrNull { text ->
        UI_DATA.findAll(text).map { attributesOf(it.value) }.firstOrNull { it["Index"] == HISTORY_INDEX }?.get("IsShow")
    } != "false"

/** «Размер интерфейса» игры (100 % → 1) или `null`. */
internal fun parseUiScale(accountText: String): Double? = UI_SCALE.find(accountText)?.groupValues?.get(1)?.toDoubleOrNull()

/** Высота разрешения игры в пикселях или `null`. */
internal fun parseResolutionHeight(accountText: String): Int? = RESOLUTION_HEIGHT.find(accountText)?.groupValues?.get(1)?.toIntOrNull()

/** Записи [tag] с номером слота в атрибуте [slotAttribute] → умение `SkillNo`; пустые (`0`) пропущены. */
private fun skills(text: String, tag: Regex, slotAttribute: String): Map<Int, Int> = tag.findAll(text)
    .map { attributesOf(it.value) }
    .mapNotNull { a -> a[slotAttribute]?.toIntOrNull()?.let { slot -> a["SkillNo"]?.toIntOrNull()?.takeIf { it > 0 }?.let { slot to it } } }
    .toMap()

private fun centers(accountText: String, indices: List<Int>): Map<Int, Pair<Double, Double>> = uiData(accountText, indices)
    .mapNotNull { (slot, a) ->
        val x = a["RelativePosX"]?.toDoubleOrNull() ?: return@mapNotNull null
        val y = a["RelativePosY"]?.toDoubleOrNull() ?: return@mapNotNull null
        slot to (x to y)
    }.toMap()

private fun hidden(characterText: String, indices: List<Int>): Set<Int> =
    uiData(characterText, indices).filter { (_, a) -> a["IsShow"] == "false" }.map { it.first }.toSet()

/** Записи `UIData` с `Index` из [indices]: номер слота (позиция в [indices]) → атрибуты. */
private fun uiData(text: String, indices: List<Int>): List<Pair<Int, Map<String, String>>> = UI_DATA.findAll(text)
    .map { attributesOf(it.value) }
    .mapNotNull { a -> a["Index"]?.toIntOrNull()?.let { indices.indexOf(it) }?.takeIf { it >= 0 }?.let { it to a } }
    .toList()
