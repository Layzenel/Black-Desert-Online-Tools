package com.layzen.bdotools.feature.screenshoteditor.domain.watch

/** Сколько ждать, пока игра допишет файл: скриншот 8K пишется секунды, дольше — что-то пошло не так. */
private const val GIVE_UP_MS = 30_000L

/**
 * Новые скриншоты, которые игра ещё пишет. Файл готов, когда его размер не изменился между двумя проверками и
 * конец файла на месте ([looksComplete]) — иначе редактор открыл бы обрезанную картинку. Каждое имя выдаётся
 * один раз: игра после записи ещё трогает файл, и папка сообщает о нём снова.
 */
class PendingScreenshots {
    private val sizes = mutableMapOf<String, Long>()
    private val since = mutableMapOf<String, Long>()
    private val done = mutableSetOf<String>()

    /** Имена, которые ещё ждут готовности. */
    val waiting: Set<String> get() = since.keys

    /** Папка сообщила о файле [name] в момент [now]; не-скриншоты и уже выданные файлы не берутся. */
    fun seen(name: String, now: Long) {
        if (!isGameScreenshotName(name) || name in done || name in since) return
        since[name] = now
    }

    /**
     * Проверка файла [name]: его размер [size] и признак [complete] конца файла.
     * Возвращает `true`, когда файл готов — один раз; файл, который не дописался за 30 с, забывается.
     */
    fun check(name: String, size: Long, complete: Boolean, now: Long): Boolean {
        val start = since[name] ?: return false
        val previous = sizes.put(name, size)
        val ready = size > 0 && size == previous && complete
        if (ready) done += name
        if (ready || now - start > GIVE_UP_MS) {
            since.remove(name)
            sizes.remove(name)
        }
        return ready
    }
}
