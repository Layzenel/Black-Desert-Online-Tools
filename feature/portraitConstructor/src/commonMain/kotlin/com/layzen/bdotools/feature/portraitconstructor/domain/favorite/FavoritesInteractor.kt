package com.layzen.bdotools.feature.portraitconstructor.domain.favorite

import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitsRepository
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.StoredPortrait

/** Сценарии избранного: наборы портретов, их сохранение, переименование, замена и удаление. */
class FavoritesInteractor(private val repository: PortraitsRepository) {

    /** См. [PortraitsRepository.favoriteSets]. */
    suspend fun sets(): List<FavoriteSet> = repository.favoriteSets()

    /** См. [PortraitsRepository.favoritePath]. */
    fun path(set: String): String = repository.favoritePath(set)

    /** См. [PortraitsRepository.readFavoritePortrait]. */
    suspend fun readPortrait(set: String, characterId: String): StoredPortrait =
        repository.readFavoritePortrait(set, characterId)

    /** См. [PortraitsRepository.saveFavoriteSet]. */
    suspend fun save(
        ids: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String = repository.saveFavoriteSet(ids, onProgress)

    /** См. [PortraitsRepository.renameFavoriteSet]. */
    suspend fun rename(set: String, title: String) = repository.renameFavoriteSet(set, title)

    /** См. [PortraitsRepository.replaceFavoriteSet]. */
    suspend fun replace(
        set: String,
        characterIds: List<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): String = repository.replaceFavoriteSet(set, characterIds, onProgress)

    /** См. [PortraitsRepository.deleteFavoriteSets]. */
    suspend fun delete(
        names: Collection<String>,
        onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> },
    ): List<FavoriteSet> = repository.deleteFavoriteSets(names, onProgress)
}
