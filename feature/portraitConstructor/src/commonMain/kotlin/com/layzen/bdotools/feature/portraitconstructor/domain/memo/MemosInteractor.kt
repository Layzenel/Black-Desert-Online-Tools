package com.layzen.bdotools.feature.portraitconstructor.domain.memo

/** Сценарии заметок персонажей: чтение заметок игры и запись в неё заметок с портретов. */
class MemosInteractor(private val repository: CharacterMemosRepository) {

    /** См. [CharacterMemosRepository.memos]. */
    suspend fun memos(account: String): Map<String, String> = repository.memos(account)

    /** См. [CharacterMemosRepository.isGameRunning]. */
    suspend fun isGameRunning(): Boolean? = repository.isGameRunning()

    /** См. [CharacterMemosRepository.write]. */
    suspend fun write(account: String, changes: List<MemoChange>): String = repository.write(account, changes)
}
