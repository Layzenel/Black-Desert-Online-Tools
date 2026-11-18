package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Комбо в файле на устройстве: поделиться комбо или перенести его. В настольной версии — окна «Сохранить как»
 * и «Открыть», на сайте — скачивание и выбор файла.
 */
interface ComboFiles {
    /** Сохраняет [combo] в файл, который выберет игрок; ничего не делает, если окно закрыли. */
    suspend fun export(combo: Combo)

    /**
     * Комбо из файла, который выберет игрок; `null`, если окно закрыли.
     *
     * @throws IllegalArgumentException если это не файл комбо
     */
    suspend fun import(): List<Combo>?
}
