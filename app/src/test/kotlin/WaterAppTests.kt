import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.cash.turbine.test
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.core.network.serialization.fromJson
import com.vodovoz.app.core.network.serialization.toJson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class WaterAppTests : CoroutineTestBase() {


    /*
    * Локальное сохранение, удаление, получение:
    *   параметры пользователя,
    *   о уведомлениях, данные,
    *   о выпитой воде сегодня,
    *   сохранение стадии.
    * Расчет дозировки воды в зависимости от параметров.
    *
    * */

    @Mock
    private lateinit var ctx: Context
    private lateinit var waterAppRepository: WaterAppRepository
    private lateinit var waterAppStorage: WaterAppStorage
    private lateinit var moshi: Moshi

    private val defaultNotificationSettings = WaterApp.NotificationSettings.Default

    private val notificationSettings = defaultNotificationSettings.copy(
        hasNotifications = true,
        wakeUpTime = LocalTime.of(11, 0)
    )
    private val storageIoException = IOException()
    private val man = WaterApp.UserInfo.ManDefault
    private val girl = WaterApp.UserInfo.GirlDefault

    @Before
    override fun setUpBase() {
        super.setUpBase()
        ctx = mockk()
        moshi = spyk(
            Moshi.Builder()
                .add(LocalTimeAdapter())
                .add(DurationAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
        )
        waterAppStorage = spyk(
            WaterAppStorageImpl(ctx)
        )
        waterAppRepository = spyk(
            WaterAppRepositoryImpl(
                waterAppStorage,
                moshi
            )
        )
    }


    @Test
    fun `get initial notification settings`() = runTest {
        coEvery { waterAppRepository.settingsFlow } returns flow {
            emit(Result.success(defaultNotificationSettings))
        }

        assertEquals(
            Result.success(defaultNotificationSettings),
            waterAppRepository.getNotificationSettings()
        )

    }

    @Test
    fun `save notification settings when storage success`() = runTest {
        coEvery { waterAppStorage.saveNotificationSettings(any()) } returns Unit

        assertEquals(
            Result.success(notificationSettings),
            waterAppRepository.saveNotificationSettings(notificationSettings)
        )
    }

    @Test
    fun `fail save notification settings when storage error`() = runTest {
        coEvery { waterAppStorage.saveNotificationSettings(any()) } answers {
            throw storageIoException
        }

        assertEquals(
            Result.failure<WaterApp.NotificationSettings>(storageIoException),
            waterAppRepository.saveNotificationSettings(notificationSettings)
        )
    }

    @Test
    fun `clear notification settings`() = runTest {
        coEvery { waterAppStorage.clearNotificationSettings() } coAnswers { }
        assertEquals(Result.success(Unit), waterAppRepository.clearNotificationSettings())
        coEvery { waterAppStorage.clearNotificationSettings() } coAnswers { throw storageIoException }
        assertEquals(
            Result.failure<Unit>(storageIoException),
            waterAppRepository.clearNotificationSettings()
        )
    }

    @Test
    fun `notification settings flow`() = runTest {
        val settingsJson = moshi.toJson(notificationSettings)
        every { waterAppStorage.notificationSettingsFlow } returns flow {
            emit("")
            emit("123456")
            emit(settingsJson)
            emit("invalid format")
        }

        coEvery {
            waterAppRepository.clearNotificationSettings()
        } returnsMany listOf(
            Result.success(Unit),
            Result.failure(storageIoException)
        )

        waterAppRepository.settingsFlow.test {
            assertEquals(
                Result.success(defaultNotificationSettings),
                awaitItem()
            )
            assertEquals(
                Result.success(defaultNotificationSettings),
                awaitItem()
            )
            assertEquals(
                Result.success(notificationSettings),
                awaitItem()
            )
            assertEquals(
                Result.failure<WaterApp.NotificationSettings>(storageIoException),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save user info`() = runTest {
        coEvery { waterAppStorage.saveUserInfo(any()) } returns Unit

        assertEquals(Result.success(girl), waterAppRepository.saveUserInfo(girl))
    }

    @Test
    fun `user info flow from storage`() = runTest {

        val lastUserInfo = man.copy(
            activityLevel = WaterApp.UserInfo.ActivityLevel.High
        )

        coEvery { waterAppRepository.clearUserInfo() } returns Result.success(Unit)

        every { waterAppStorage.userInfoFlow } returns flow {
            emit("")
            emit(moshi.toJson(girl))
            emit("12312lpofwop")
            emit(moshi.toJson(lastUserInfo))
        }

        waterAppRepository.userInfoFlow.test {
            assertEquals(
                Result.success(man),
                awaitItem()
            )
            assertEquals(
                Result.success(girl),
                awaitItem()
            )
            assertEquals(
                Result.success(man),
                awaitItem()
            )
            assertEquals(
                Result.success(lastUserInfo),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            waterAppRepository.clearUserInfo()
        }
    }

    @Test
    fun `erasure types class check`() = runTest {
        fun <T : Any> method(value: List<T>): KClass<out T> {
            return value.first()::class
        }

        assertEquals(1::class, method(listOf(2)))

    }

}


interface WaterAppStorage {
    val userInfoFlow: Flow<String>
    val notificationSettingsFlow: Flow<String>
    suspend fun getNotificationSetting(): String
    suspend fun getUserInfo(): String

    suspend fun clearNotificationSettings()
    suspend fun saveNotificationSettings(notificationSettings: String)
    suspend fun saveUserInfo(userInfo: String)
    suspend fun clearUserInfo()
}

private val Context.waterAppDataStore by preferencesDataStore(
    "water_app.pb"
)

class WaterAppStorageImpl(
    private val context: Context,
) : WaterAppStorage {

    private val dataStore get() = context.waterAppDataStore

    private fun getDataFlow(key: String) = dataStore.data.map { it[key] ?: "" }
    private suspend fun getData(key: String) = getDataFlow(key).first()
    private suspend fun editData(key: String, value: String) {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[key] = value
        }
    }

    override val userInfoFlow: Flow<String>
        get() = getDataFlow(USER_INFO_KEY)

    override val notificationSettingsFlow: Flow<String>
        get() = getDataFlow(NOTIFICATION_SETTINGS_KEY)


    override suspend fun getNotificationSetting(): String {
        return getData(NOTIFICATION_SETTINGS_KEY)
    }

    override suspend fun getUserInfo(): String {
        return getData(USER_INFO_KEY)
    }

    override suspend fun clearNotificationSettings() {
        editData(NOTIFICATION_SETTINGS_KEY, "")
    }

    override suspend fun saveNotificationSettings(notificationSettings: String) {
        editData(NOTIFICATION_SETTINGS_KEY, notificationSettings)
    }

    override suspend fun saveUserInfo(userInfo: String) {
        editData(USER_INFO_KEY, userInfo)
    }

    override suspend fun clearUserInfo() {
        editData(USER_INFO_KEY, "")
    }

    companion object {
        private const val NOTIFICATION_SETTINGS_KEY = "NOTIFICATION_SETTINGS_KEY"
        private const val USER_INFO_KEY = "USER_INFO_KEY"
    }
}


inline operator fun <reified T> Preferences.get(keyName: String): T? {
    asMap().forEach { (t, u) ->
        if (t.name == keyName) return u as? T
    }
    return null
}

@Suppress("UNCHECKED_CAST")
inline operator fun <reified T> MutablePreferences.set(keyName: String, value: T) {
    val key: Preferences.Key<T> = when (value) {
        is Int -> intPreferencesKey(keyName)
        is Double -> doublePreferencesKey(keyName)
        is String -> stringPreferencesKey(keyName)
        is Boolean -> booleanPreferencesKey(keyName)
        is Float -> floatPreferencesKey(keyName)
        is Long -> longPreferencesKey(keyName)
        is Set<*> -> stringSetPreferencesKey(keyName)
        is ByteArray -> byteArrayPreferencesKey(keyName)
        else -> throw IllegalArgumentException(
            "Unsupported type for Preferences key: ${value!!::class.java.name}"
        )
    } as Preferences.Key<T>
    set(key, value)
}

interface WaterAppRepository {


    val userInfoFlow: Flow<Result<WaterApp.UserInfo>>
    val settingsFlow: Flow<Result<WaterApp.NotificationSettings>>
    suspend fun clearNotificationSettings(): Result<Unit>
    suspend fun saveNotificationSettings(
        notificationSettings: WaterApp.NotificationSettings,
    ): Result<WaterApp.NotificationSettings>

    suspend fun getNotificationSettings(): Result<WaterApp.NotificationSettings>
    suspend fun saveUserInfo(userInfo: WaterApp.UserInfo): Result<WaterApp.UserInfo>
    suspend fun clearUserInfo(): Result<Unit>

}

class WaterAppRepositoryImpl(
    private val storage: WaterAppStorage,
    private val moshi: Moshi,
) : WaterAppRepository {

    override suspend fun getNotificationSettings(): Result<WaterApp.NotificationSettings> =
        runCatching {  settingsFlow.first().getOrThrow() }

    override val userInfoFlow: Flow<Result<WaterApp.UserInfo>>
        get() = getOperation(
            defaultValue = WaterApp.UserInfo.ManDefault,
            block = storage::userInfoFlow,
            onClear = ::clearUserInfo
        )


    override val settingsFlow: Flow<Result<WaterApp.NotificationSettings>>
        get() = getOperation(
            defaultValue = WaterApp.NotificationSettings.Default,
            block = storage::notificationSettingsFlow,
            onClear = ::clearNotificationSettings
        )


    override suspend fun saveNotificationSettings(
        notificationSettings: WaterApp.NotificationSettings,
    ): Result<WaterApp.NotificationSettings> = saveOperation(
        data = notificationSettings,
        block = storage::saveNotificationSettings
    )

    override suspend fun saveUserInfo(
        userInfo: WaterApp.UserInfo,
    ): Result<WaterApp.UserInfo> = saveOperation(
        data = userInfo,
        block = storage::saveUserInfo
    )

    override suspend fun clearUserInfo(): Result<Unit> = runCatching {
        storage.clearUserInfo()
    }

    override suspend fun clearNotificationSettings(): Result<Unit> = runCatching {
        storage.clearNotificationSettings()
    }

    private suspend inline fun <reified T : Any> saveOperation(
        data: T,
        noinline toJson: T.() -> String = { moshi.toJson(this) },
        noinline block: suspend (json: String) -> Unit,
    ): Result<T> = runCatching {
        block(data.toJson())
        data
    }

    private inline fun <reified R> getOperation(
        defaultValue: R,
        noinline block: () -> Flow<String>,
        noinline fromJson: (String) -> R = { moshi.fromJson(it) },
        noinline onClear: suspend () -> Result<*>,
    ): Flow<Result<R>> = block().map { json ->
        runCatching {
            if (json.isBlank()) {
                defaultValue
            } else fromJson(json)
        }.recoverCatching {
            onClear().getOrThrow()
            defaultValue
        }
    }
}

data object WaterApp {


    @Keep
    data class NotificationSettings(
        val hasNotifications: Boolean,
        val notificationsDelay: Duration,
        val wakeUpTime: LocalTime,
        val sleepTime: LocalTime,
    ) {
        companion object {
            val Default = NotificationSettings(
                hasNotifications = false,
                notificationsDelay = 2.hours,
                wakeUpTime = LocalTime.of(9, 0, 0),
                sleepTime = LocalTime.of(23, 0, 0)
            )
        }
    }

    @Keep
    data class UserInfo(
        val gender: Gender,
        val height: Float,
        val weight: Float,
        val activityLevel: ActivityLevel,
    ) {

        @Keep
        enum class Gender {
            Man, Girl
        }

        @Keep
        enum class ActivityLevel {
            Low, Medium, High
        }

        companion object {
            val ManDefault = UserInfo(Gender.Man, 175f, 80f, ActivityLevel.Low)
            val GirlDefault = UserInfo(Gender.Girl, 165f, 65f, ActivityLevel.Low)
        }

    }

}

class DurationAdapter : JsonAdapter<Duration>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Duration? {
        return if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Unit>()
            null
        } else {
            reader.nextLong().milliseconds
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Duration?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value.inWholeMilliseconds)
        }
    }
}

class LocalTimeAdapter {
    @ToJson
    fun toJson(value: LocalTime): String = value.toString()

    @FromJson
    fun fromJson(value: String): LocalTime = LocalTime.parse(value)
}