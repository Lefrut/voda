package com.vodovoz.app.common.tracking

import com.vodovoz.app.common.datastore.DataStorePrefs
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingManager @Inject constructor(
    private val dataStorePrefs: DataStorePrefs,
) {

    private var sessionId: String = ""
    private var userGUID: String = ""

    private var viewGUID: String = ""

    private var sessionIdTime = 0

    private var enableTracking = false
    private fun generateUIDs(updateViewGUID: Boolean) {

        /**
         * sessionId
         * генерируется на 30 минут
         * **/
        val timeInMillis: Long = sessionIdTime.toLong() * 60 * 1000
        val currentTime = System.currentTimeMillis()
        val lastSessionIDTime: Long = dataStorePrefs.getLong("LastSessionIDTime") ?: 0L
        if (currentTime - lastSessionIDTime > timeInMillis) {
            sessionId = UUID.randomUUID().toString()
            dataStorePrefs.putString("sessionId", sessionId)
            dataStorePrefs.putLong("LastSessionIDTime", currentTime)
        } else {
            sessionId = dataStorePrefs.getString("sessionId") ?: UUID.randomUUID().toString()
        }

        /**
         * userGUID
         * генерируется на год,
         * формат: 0:&lt;TIMESTAMP>:&lt;UUID>, где TIMESTAMP - время создания в формате base36
         * **/
        val yearInMillis = 365L * 24 * 60 * 60 * 1000
        val lastUserGUIDTime = dataStorePrefs.getLong("LastUserGUIDTime") ?: 0L
        if (currentTime - lastUserGUIDTime > yearInMillis) {
            userGUID = createGuidUUID(currentTime)
            dataStorePrefs.putString("userGUID", userGUID)
            dataStorePrefs.putLong("LastUserGUIDTime", currentTime)
        } else {
            userGUID = dataStorePrefs.getString(
                "userGUID"
            ) ?: createGuidUUID(currentTime)

        }
        /**
         * viewGUID
         * Генерируется на каждом экране и сквозной для всех трекинг запросов с экрана при его посещении в данный момент времени.
         * **/
        if(updateViewGUID) {
            viewGUID = UUID.randomUUID().toString()
        }
    }

    private fun createGuidUUID(currentTime: Long): String {
        val timestampInBase36 = currentTime.toString(36)
        return "0:$timestampInBase36:${UUID.randomUUID()}"
    }

    fun getTracking(updateViewGUID: Boolean = true): Map<String, String> {
        if (!enableTracking) return emptyMap()
        generateUIDs(updateViewGUID)
        val trackingMap = HashMap<String, String>()
        trackingMap["sessionId"] = sessionId
        trackingMap["userGUID"] = userGUID
        trackingMap["viewGUID"] = viewGUID
        return trackingMap
    }

    fun setEnableTracking(enableTracking: Boolean) {
        this.enableTracking = enableTracking
    }

    fun setSessionIdTime(sessionIdTime: Int) {
        this.sessionIdTime = sessionIdTime
    }
}