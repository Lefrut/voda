package com.vodovoz.app.common.search

import com.vodovoz.app.common.datastore.DataStorePrefs
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchManager @Inject constructor(
    private val dataStorePrefs: DataStorePrefs,
) {

    companion object {
        private const val SEARCH_HISTORY = "SEARCH_HISTORY"
    }

    fun searchHistoryFlow() =
        dataStorePrefs.getStringFlow(SEARCH_HISTORY).map { queries ->
            Formatters.parse(queries ?: "")
        }

    fun clearSearchHistory() {
        dataStorePrefs.remove(SEARCH_HISTORY)
    }

    fun addQueryToHistory(query: String) {
        if (query.isEmpty()) return

        val queryList = fetchSearchHistory()
        val cont = queryList.find { it == query }
        if (cont == null) {
            dataStorePrefs.putString(
                SEARCH_HISTORY,
                Formatters.format(listOf(query) + queryList)
            )
        }
    }

    fun removeQueryFromHistory(query: String) {
        val queryList = fetchSearchHistory().toMutableList().apply { remove(query) }
        dataStorePrefs.putString(SEARCH_HISTORY, Formatters.format(queryList))
    }

    private fun fetchSearchHistory() =
        Formatters.parse(dataStorePrefs.getString(SEARCH_HISTORY) ?: "")


    private data object Formatters {

        fun format(queries: List<String>): String {
            return StringBuilder().apply {
                queries.forEach { query -> append(query).append(",") }
            }.toString()
        }

        fun parse(queries: String): List<String> {
            val queryList = queries.split(",").toMutableList()
            return queryList.filter { it.isNotEmpty() }
        }

    }
}