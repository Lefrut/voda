package com.vodovoz.app.common.like

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.datastore.DataStorePrefs
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.singleResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeManager @Inject constructor(
    private val dataStorePrefs: DataStorePrefs,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) {

    companion object {
        private const val FAV_IDS = "Favorites"
    }

    private val mutex = Mutex()

    private fun getLikeVersion(productId: Long) = likesVersions.getOrDefault(productId, 0)

    private val likesStateListener = MutableSharedFlow<Map<Long, Boolean>>(1)
    private val likes = ConcurrentHashMap<Long, Boolean>()
    private val likesVersions = ConcurrentHashMap<Long, Int>()
    private val likesCategories = ConcurrentHashMap<Long, Int?>()
    var selectedCategoryId: Int? = null
        private set

    fun getLikes(): Map<Long, Boolean> {
        return likes.toMap()
    }

    fun getLikesCategories(): Map<Long, Int?> {
        return likesCategories
    }

    fun observeLikes() = likesStateListener.asSharedFlow()

    suspend fun changeCategory(categoryId: Int? = null) = mutex.withLock {
        selectedCategoryId = categoryId
    }

    suspend fun changeFavorite(productId: Long, newValue: Boolean) {

        val (likeVersion, userId) = mutex.withLock {
            if (selectedCategoryId != null) {
                updateFavoritesLocal(productId, newValue)
            }
            val updatedVersion = updateFavoritesOptimistically(productId, newValue)
            if (updatedVersion < getLikeVersion(productId)) return

            val userId = accountManager.fetchAccountId()
            updatedVersion to userId
        }

        kotlin.runCatching {
            if (userId != null) {
                updateFavoritesOnline(productId, newValue)
            } else {
                updateFavoritesLocal(productId, newValue)
            }
        }.onFailure {
            mutex.withLock {
                if (likeVersion == getLikeVersion(productId)) {
                    updateFavoritesOptimistically(productId, newValue)
                }
            }
        }
    }


    private suspend fun updateFavoritesOnline(
        productId: Long,
        newIsFavorite: Boolean,
    ) {
        if (newIsFavorite) {
            vodovozServiceRepository.addProductToFavorites(productId).singleResult().getOrThrow()
        } else {
            vodovozServiceRepository.removeProductFromFavorites(productId).singleResult()
                .getOrThrow()
        }
    }

    private suspend fun updateFavoritesOptimistically(productId: Long, newValue: Boolean): Int {
        if (likes[productId] == newValue) return -1

        val currentVersion = getLikeVersion(productId) + 1
        likesVersions[productId] = currentVersion
        likes[productId] = newValue
        likesStateListener.emit(likes.toMap())
        return currentVersion
    }

    private fun updateFavoritesLocal(productId: Long, newIsFavorite: Boolean) {
        val localLikesListString = dataStorePrefs.getString(FAV_IDS)
        val localLikesList = if (localLikesListString.isNullOrEmpty()) {
            listOf(productId)
        } else if (!newIsFavorite) {
            removeInFavoriteStr(localLikesListString, productId)
        } else {
            val ids = parseFavorites(localLikesListString)
            (listOf(productId) + ids)
        }
        dataStorePrefs.putString(FAV_IDS, formatFavorites(localLikesList))
    }

    fun fetchLocalFavorites(): String {
        return formatFavorites(
            parseFavorites(
                dataStorePrefs.getString(FAV_IDS)?.dropLastWhile { char ->
                    char == ','
                } ?: ""
            )
        )
    }

    private fun formatFavorites(favoriteList: List<Long>): String {
        val favoriteStr = StringBuilder()
        favoriteList.forEach { productId ->
            favoriteStr.append(productId).append(",")
        }
        return favoriteStr.toString()
    }

    private fun parseFavorites(favoriteStr: String): List<Long> {
        if (favoriteStr.isBlank()) return emptyList()

        val favorites = try {
            val favoriteList = mutableListOf<Long>()
            favoriteStr.split(",").forEach { id ->
                if (id.isNotEmpty()) {
                    favoriteList.add(id.toLong())
                }
            }
            favoriteList.toSet().toList()
        } catch (_: Exception) {
            val ids = extractIdsFromJson(favoriteStr)
            rewriteFavoritesLocal(ids.associateWith { true })
            ids
        }

        return favorites
    }

    private fun removeInFavoriteStr(favoriteStr: String, productId: Long): List<Long> {
        val favoriteList = mutableListOf<Long>()
        favoriteStr.split(",").forEach { id ->
            if (id.isNotEmpty() && id.toLong() != productId) {
                favoriteList.add(id.toLong())
            }
        }
        return favoriteList.toSet().toList()
    }

    private fun extractIdsFromJson(json: String): List<Long> {
        val moshi = Moshi.Builder().build()
        val innerType =
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        val type = Types.newParameterizedType(Map::class.java, String::class.java, innerType)
        val adapter = moshi.adapter<Map<String, Map<String, String>>>(type)
        return runCatching {
            adapter.fromJson(json)?.values?.mapNotNull { it["ID"]?.toLongOrNull() } ?: emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun syncFavoritesFromLocal() {
        val localLikesListString = dataStorePrefs.getString(FAV_IDS) ?: ""

        val localLikesList = parseFavorites(localLikesListString)

        localLikesList.forEach { productId ->
            updateFavoritesOptimistically(productId, true)
        }
    }

    private fun rewriteFavoritesLocal(favorites: Map<Long, Boolean>) {
        dataStorePrefs.putString(
            key = FAV_IDS,
            value = formatFavorites(
                favorites.filter { favorite ->
                    favorite.value
                }.keys.toList()
            )
        )
    }

    suspend fun updateLikesAfterLogin() {
        val localLikesListString = dataStorePrefs.getString(FAV_IDS)?.dropLast(1) ?: ""

        runCatching {
            vodovozServiceRepository.addFavoriteProducts(localLikesListString).singleResult()
            dataStorePrefs.remove(FAV_IDS)
        }
    }

}