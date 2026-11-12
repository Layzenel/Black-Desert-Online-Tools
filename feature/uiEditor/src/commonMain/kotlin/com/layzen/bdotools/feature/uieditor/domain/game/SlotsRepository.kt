package com.layzen.bdotools.feature.uieditor.domain.game

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills

/**
 * Настройки интерфейса игры в папке `Документы\Black Desert` и файлы редактора рядом с ними: пресеты
 * пользователя и подложка. Ошибки работы с файлами — [SlotsException].
 */
interface SlotsRepository {

    /** Есть ли доступ к папке игры; ничего не спрашивает у пользователя. */
    suspend fun access(): FolderAccess

    /** Просит пользователя выбрать папку `Black Desert` и запоминает её. */
    suspend fun selectFolder(): FolderAccess.Granted

    /** Просит разрешение на уже выбранную папку (браузер спрашивает его заново после перезагрузки страницы). */
    suspend fun requestPermission(): FolderAccess.Granted

    /** Аккаунты, у которых есть `gamevariable.xml`; если нет ни одного — [SlotsError.NoGameVariable]. */
    suspend fun accounts(): List<String>

    /** Читает файл аккаунта; в редакторе будет набор [edited], если он есть в файле, иначе общий. */
    suspend fun load(account: String, edited: GamePreset = GamePreset.General): LoadedGame

    /**
     * Записывает [layout] в набор [target] файла аккаунта. Видимость общего набора игра хранит ещё и у каждого
     * персонажа, поэтому для него поменявшийся `IsShow` записывается и в файлы персонажей; у пресетов 1–3
     * видимость лежит в самих записях. «Настройка быстрых слотов» одна на аккаунт и пишется при любом [target],
     * если поменялась. Окна чата пишутся в тот же набор, их видимость в общем наборе — ещё и персонажам.
     * Возвращает путь резервной копии.
     */
    suspend fun apply(loaded: LoadedGame, layout: SlotLayout, target: GamePreset): String

    /**
     * Возвращает файлы аккаунта [account] — `gamevariable.xml` и файлы персонажей — к текстам до последнего [apply]
     * этого сеанса, с учётом записей навыков после него. Перед записью тоже делаются резервные копии.
     * `false` — откатывать нечего.
     */
    suspend fun undoLastApply(account: String): Boolean

    /** Навыки слотов перезарядки и надписи всех персонажей аккаунта [account], у которых есть файл. */
    suspend fun characterSkills(account: String): List<CharacterSkills>

    /**
     * Записывает персонажу [characterNo] аккаунта [account] навыки слотов перезарядки [slots] (`SlotNo` → `SkillNo`)
     * и надписи [memos] (`SkillNo` → текст); перед записью делает резервную копию. Нет файла — ничего не пишет.
     */
    suspend fun writeCharacterSkills(account: String, characterNo: String, slots: Map<Int, Int>, memos: Map<Int, String>)

    /** Переносит пресеты и подложку из версий до аккаунтов в папку аккаунта [account]. */
    suspend fun adoptLooseFiles(account: String)

    /** Картинка-подложка редактора для аккаунта [account] или `null`, если её не выбирали. */
    suspend fun readBackground(account: String): ByteArray?

    /** Сохраняет подложку аккаунта [account]; `null` удаляет её. */
    suspend fun writeBackground(account: String, bytes: ByteArray?)

    /** Пресеты аккаунта [account]; файлы, которые не удалось прочитать, пропускаются. */
    suspend fun presets(account: String): List<NamedLayout>

    /** Сохраняет пресет аккаунта [account]; пресет с таким же названием перезаписывается. */
    suspend fun savePreset(account: String, preset: NamedLayout)

    /** Удаляет пресет аккаунта [account] с названием [name]. */
    suspend fun deletePreset(account: String, name: String)

    /** Имя файла для экспорта и папки пресетов: название без символов, запрещённых в Windows. */
    fun fileName(name: String): String
}
