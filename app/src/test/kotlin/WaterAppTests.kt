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
import io.mockk.mockkObject
import io.mockk.spyk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class WaterAppTests : CoroutineTestBase() {


    /*
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
                .add(WaterApp.Stage::class.java, StageAdapter())
                .add(Duration::class.java, DurationAdapter())
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
    fun `save stage`() = runTest {
        val stage = WaterApp.Stage("1234512321")
        coEvery { waterAppStorage.saveStage(any()) } returns Unit
        assertEquals(
            Result.success(stage.name),
            waterAppRepository.saveStage(stage)
        )
    }

    @Test
    fun `clear stage`() = runTest {
        coEvery { waterAppStorage.clearStage() } returns Unit

        assertEquals(Result.success(Unit), waterAppRepository.clearStage())
    }

    @Test
    fun `get stage flow`() = runTest {
        val stage = WaterApp.Stage("zzzzzzzzz")

        coEvery { waterAppStorage.stageFlow } returns flow {
            emit(stage.name)
        }

        waterAppRepository.stageFlow.test {
            assertEquals(stage.name, awaitItem().getOrThrow().name)

            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun stageAdapter_roundtrip_and_null() {
        val adapter: JsonAdapter<WaterApp.Stage> = moshi.adapter(WaterApp.Stage::class.java)

        // serialize
        val json = adapter.toJson(WaterApp.Stage("MAIN"))
        assertEquals("\"MAIN\"", json)

        // deserialize
        val obj = adapter.fromJson("\"ONBOARDING\"")
        assertEquals(WaterApp.Stage("ONBOARDING"), obj)

        // null cases
        assertNull(adapter.fromJson("null"))
        assertEquals("null", adapter.toJson(null))
    }

    @Test
    fun durationAdapter_roundtrip_and_null() {
        val adapter: JsonAdapter<Duration> = moshi.adapter(Duration::class.java)

        val json = adapter.toJson(2500.milliseconds)
        assertEquals("2500", json)

        // deserialize
        val obj = adapter.fromJson("1500")
        assertEquals(1500.milliseconds, obj)

        assertNull(adapter.fromJson("null"))
        assertEquals("null", adapter.toJson(null))
    }

    @Test
    fun calculateDailyGoal_variousInputs() = runTest {
        with(WaterApp) {
            assertEquals(
                2750,
                calculateDailyGoal(
                    man.copy(
                        weight = 70f,
                        activityLevel = WaterApp.UserInfo.ActivityLevel.Low
                    )
                ).totalMl
            )
            assertEquals(
                2775,
                calculateDailyGoal(
                    man.copy(
                        weight = 65f,
                        activityLevel = WaterApp.UserInfo.ActivityLevel.Medium
                    )
                ).totalMl
            )

            assertEquals(
                4400,
                calculateDailyGoal(
                    man.copy(
                        weight = 140f,
                        activityLevel = WaterApp.UserInfo.ActivityLevel.High
                    )
                ).totalMl
            )
        }
    }

    @Test
    fun `save daily goal`() = runTest {
        val dailyGoal = WaterApp.DailyGoal.create(1850)
        coEvery { waterAppStorage.saveDailyGoal(any()) } returns Unit

        assertEquals(Result.success(dailyGoal), waterAppRepository.saveDailyGoal(dailyGoal))
    }

    @Test
    fun `get daily goal flow`() = runTest {
        val dailyGoal = spyk(WaterApp.DailyGoal.create(2550))

        every { dailyGoal.plusMl(any()) } returnsMany listOf(
            dailyGoal.copy(currentMl = 500),
            dailyGoal.copy(currentMl = 800)
        )

        val dailyGoalAfterDrink = dailyGoal.plusMl(500)

        coEvery {
            waterAppStorage.dailyGoalFlow
        } returns flow {
            emit(moshi.toJson(dailyGoal))
            emit(moshi.toJson(dailyGoalAfterDrink))
            emit("12321321")
        }
        coEvery { waterAppRepository.clearDailyGoal() } returns Result.success(Unit)
        coEvery { waterAppRepository.userInfoFlow } returns flow {
            emit(Result.success(man))
        }

        mockkObject(WaterApp)
        every { WaterApp.calculateDailyGoal(any()) } returns dailyGoal

        waterAppRepository.dailyGoalFlow.test {
            assertEquals(Result.success(dailyGoal), awaitItem())
            assertEquals(Result.success(dailyGoalAfterDrink), awaitItem())
            assertEquals(Result.success(dailyGoal), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear daily goal`() = runTest {
        coEvery { waterAppStorage.clearDailyGoal() } returns Unit

        assertEquals(Result.success(Unit), waterAppRepository.clearDailyGoal())
    }

    @Test
    fun `get daily goal`() = runTest {
        val dailyGoal = WaterApp.DailyGoal.create(3000)

        every {
            waterAppRepository.dailyGoalFlow
        } returns flow {
            emit(Result.success(dailyGoal))
        }

        assertEquals(
            Result.success(dailyGoal),
            waterAppRepository.getDailyGoal()
        )
    }

    @Test
    fun `daily goal plus ml`() {
        val dailyGoal = WaterApp.DailyGoal.create(3000)

        assertEquals(dailyGoal.copy(currentMl = 250), dailyGoal.plusMl(250))
        assertEquals(dailyGoal, dailyGoal.plusMl(0))
        assertEquals(
            dailyGoal.copy(currentMl = 3000, wasCompleted = true),
            dailyGoal.plusMl(35000)
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
            WaterAppRepository.UnknownException::class.java,
            waterAppRepository.saveNotificationSettings(notificationSettings)
                .exceptionOrNull()?.javaClass
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
            throw storageIoException
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
                WaterAppRepository.UnknownException::class.java,
                awaitItem().exceptionOrNull()?.javaClass
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
    fun `get user info`() = runTest {
        every { waterAppRepository.userInfoFlow } returnsMany listOf(
            flow { emit(Result.success(man)) },
            flow {}
        )
        assertEquals(Result.success(man), waterAppRepository.getUserInfo())
        assertEquals(true, waterAppRepository.getUserInfo().isFailure)
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
    val stageFlow: Flow<String>
    val userInfoFlow: Flow<String>
    val notificationSettingsFlow: Flow<String>
    val dailyGoalFlow: Flow<String>

    suspend fun saveNotificationSettings(notificationSettings: String)
    suspend fun saveUserInfo(userInfo: String)
    suspend fun saveDailyGoal(dailyGoal: String)
    suspend fun saveStage(stage: String)

    suspend fun clearUserInfo()
    suspend fun clearDailyGoal()
    suspend fun clearNotificationSettings()
    suspend fun clearStage()

}

private val Context.waterAppDataStore by preferencesDataStore(
    "water_app.pb"
)

class WaterAppStorageImpl(
    private val context: Context,
) : WaterAppStorage {

    private val dataStore get() = context.waterAppDataStore

    private fun getDataFlow(key: String) = dataStore.data.map { it[key] ?: "" }
    private suspend fun editData(key: String, value: String) {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[key] = value
        }
    }

    override val stageFlow: Flow<String>
        get() = getDataFlow(STAGE)

    override val userInfoFlow: Flow<String>
        get() = getDataFlow(USER_INFO_KEY)

    override val notificationSettingsFlow: Flow<String>
        get() = getDataFlow(NOTIFICATION_SETTINGS_KEY)
    override val dailyGoalFlow: Flow<String>
        get() = getDataFlow(DAILY_GOAL)


    override suspend fun clearNotificationSettings() {
        editData(NOTIFICATION_SETTINGS_KEY, "")
    }

    override suspend fun clearStage() {
        editData(STAGE, "")
    }

    override suspend fun saveNotificationSettings(notificationSettings: String) {
        editData(NOTIFICATION_SETTINGS_KEY, notificationSettings)
    }

    override suspend fun saveUserInfo(userInfo: String) {
        editData(USER_INFO_KEY, userInfo)
    }

    override suspend fun saveDailyGoal(dailyGoal: String) {
        editData(DAILY_GOAL, dailyGoal)
    }

    override suspend fun saveStage(stage: String) {
        editData(STAGE, stage)
    }

    override suspend fun clearUserInfo() {
        editData(USER_INFO_KEY, "")
    }

    override suspend fun clearDailyGoal() {
        editData(DAILY_GOAL, "")
    }

    companion object {
        private const val NOTIFICATION_SETTINGS_KEY = "NOTIFICATION_SETTINGS_KEY"
        private const val USER_INFO_KEY = "USER_INFO_KEY"
        private const val DAILY_GOAL = "DAILY_GOAL"
        private const val STAGE = "STAGE"
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


    val stageFlow: Flow<Result<WaterApp.Stage>>
    val dailyGoalFlow: Flow<Result<WaterApp.DailyGoal>>
    val userInfoFlow: Flow<Result<WaterApp.UserInfo>>
    val settingsFlow: Flow<Result<WaterApp.NotificationSettings>>

    suspend fun saveNotificationSettings(
        notificationSettings: WaterApp.NotificationSettings,
    ): Result<WaterApp.NotificationSettings>

    suspend fun saveUserInfo(userInfo: WaterApp.UserInfo): Result<WaterApp.UserInfo>
    suspend fun saveDailyGoal(dailyGoal: WaterApp.DailyGoal): Result<WaterApp.DailyGoal>

    suspend fun clearNotificationSettings(): Result<Unit>
    suspend fun clearUserInfo(): Result<Unit>
    suspend fun clearDailyGoal(): Result<Unit>

    suspend fun getUserInfo(): Result<WaterApp.UserInfo>
    suspend fun getNotificationSettings(): Result<WaterApp.NotificationSettings>
    suspend fun getDailyGoal(): Result<WaterApp.DailyGoal>
    suspend fun saveStage(stage: WaterApp.Stage): Result<WaterApp.Stage>
    suspend fun clearStage(): Result<Unit>


    sealed class Exception : RuntimeException()

    class UnknownException(
        override val cause: Throwable? = null,
        override val message: String? = "FormatException",
    ) : Exception()

    class FormatException(
        override val cause: Throwable? = null,
        override val message: String? = "FormatException",
    ) : Exception()


}

fun Throwable.toUnknownException(): WaterAppRepository.UnknownException {
    return WaterAppRepository.UnknownException(cause, message)
}

open class WaterAppRepositoryImpl(
    private val storage: WaterAppStorage,
    protected val moshi: Moshi,
) : WaterAppRepository {

    override suspend fun getNotificationSettings(): Result<WaterApp.NotificationSettings> =
        settingsFlow.firstResult()

    override suspend fun getDailyGoal(): Result<WaterApp.DailyGoal> = dailyGoalFlow.firstResult()
    override suspend fun getUserInfo(): Result<WaterApp.UserInfo> = userInfoFlow.firstResult()


    override val userInfoFlow: Flow<Result<WaterApp.UserInfo>>
        get() = getFlowOperation(
            defaultValue = { WaterApp.UserInfo.ManDefault },
            flow = storage.userInfoFlow,
            onFormatError = ::clearUserInfo
        )


    override val settingsFlow: Flow<Result<WaterApp.NotificationSettings>>
        get() = getFlowOperation(
            defaultValue = { WaterApp.NotificationSettings.Default },
            flow = storage.notificationSettingsFlow,
            onFormatError = ::clearNotificationSettings
        )
    override val stageFlow: Flow<Result<WaterApp.Stage>>
        get() = getFlowOperation(
            defaultValue = { WaterApp.Stage("") },
            flow = storage.stageFlow,
            onFormatError = ::clearStage
        )

    override val dailyGoalFlow: Flow<Result<WaterApp.DailyGoal>>
        get() = getFlowOperation(
            defaultValue = {
                val currentInfo = userInfoFlow.firstResult().getOrThrow()
                WaterApp.calculateDailyGoal(currentInfo)
            },
            flow = storage.dailyGoalFlow,
            onFormatError = ::clearDailyGoal
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

    override suspend fun saveDailyGoal(
        dailyGoal: WaterApp.DailyGoal,
    ): Result<WaterApp.DailyGoal> = saveOperation(
        data = dailyGoal,
        block = storage::saveDailyGoal
    )

    override suspend fun saveStage(
        stage: WaterApp.Stage,
    ): Result<WaterApp.Stage> = saveOperation(
        data = stage,
        block = storage::saveStage
    )


    override suspend fun clearStage(): Result<Unit> = clearOperation {
        storage.clearStage()
    }

    override suspend fun clearUserInfo(): Result<Unit> = clearOperation {
        storage.clearUserInfo()
    }

    override suspend fun clearDailyGoal(): Result<Unit> = clearOperation {
        storage.clearDailyGoal()
    }

    override suspend fun clearNotificationSettings(): Result<Unit> = clearOperation {
        storage.clearNotificationSettings()
    }

    private suspend fun clearOperation(block: suspend () -> Unit) = kotlin.runCatching {
        block()
    }

    private suspend inline fun <reified T : Any> saveOperation(
        data: T,
        noinline toJson: T.() -> String = { moshi.toJson(this) },
        noinline block: suspend (json: String) -> Unit,
    ): Result<T> = runCatching {
        block(data.toJson())
        data
    }.recoverCatching {
        throw it.toUnknownException()
    }


    private inline fun <reified R> getFlowOperation(
        noinline defaultValue: suspend () -> R,
        flow: Flow<String>,
        noinline onFormatError: suspend () -> Unit,
    ): Flow<Result<R>> {
        return flow.map { json ->
            if (json.isBlank()) return@map runCatching {
                defaultValue()
            }
            val data = fromJson<R>(json)
            data.recoverCatching { t ->
                if (t is WaterAppRepository.FormatException) {
                    onFormatError()
                    defaultValue()
                } else throw t.toUnknownException()
            }
        }.catch { t ->
            emit(
                Result.failure(t.toUnknownException())
            )
        }

    }

    protected inline fun <reified R> fromJson(
        data: String,
        noinline fromJson: (String) -> R = {
            moshi.fromJson(it)
        },
    ): Result<R> = runCatching {
        fromJson(data)
    }.recoverCatching {
        Result.failure<R>(
            WaterAppRepository.FormatException(
                it.cause,
                it.message
            )
        ).getOrThrow()
    }


}


suspend inline fun <R> Flow<Result<R>>.firstResult(
) = runCatching {
    first().getOrThrow()
}

data object WaterApp {

    fun calculateDailyGoal(userInfo: UserInfo): DailyGoal {
        val weight = BigDecimal(userInfo.weight.toDouble())
        val sport = BigDecimal(userInfo.activityLevel.sport.toDouble())

        val base = BigDecimal("1.5")
        val coef = BigDecimal("0.02")
        val thousand = BigDecimal("1000")

        val result = base
            .add(weight.subtract(BigDecimal("20")).multiply(coef))
            .add(sport)
            .multiply(thousand)
            .setScale(0, RoundingMode.HALF_UP)

        return DailyGoal.create(result.intValueExact())
    }


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
        enum class ActivityLevel(val sport: Float) {
            Low(0.25f), Medium(0.375f), High(0.5f)
        }

        companion object {
            val ManDefault = UserInfo(Gender.Man, 175f, 80f, ActivityLevel.Low)
            val GirlDefault = UserInfo(Gender.Girl, 165f, 65f, ActivityLevel.Low)
        }

    }

    @Keep
    data class DailyGoal(
        val totalMl: Int,
        val currentMl: Int,
        val wasCompleted: Boolean,
    ) {

        init {
            require(totalMl > 0 && currentMl >= 0)
        }

        fun plusMl(ml: Int): DailyGoal {
            val newCurrentMl = (currentMl + ml).coerceAtMost(totalMl)
            return DailyGoal(totalMl, newCurrentMl, wasCompleted || newCurrentMl >= totalMl)
        }

        companion object {
            fun create(totalMl: Int) = DailyGoal(
                totalMl,
                0,
                false
            )
        }


    }

    @JvmInline
    @Keep
    value class Stage(val name: String)

}

abstract class NullableAdapter<T> : JsonAdapter<T>() {

    abstract fun parse(reader: JsonReader): T
    abstract fun serialize(writer: JsonWriter, value: T)

    @FromJson
    final override fun fromJson(reader: JsonReader): T? {
        return if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Unit>()
            null
        } else parse(reader)
    }

    @ToJson
    final override fun toJson(writer: JsonWriter, value: T?) {
        if (value == null) {
            writer.nullValue()
        } else serialize(writer, value)
    }
}

class StageAdapter : NullableAdapter<WaterApp.Stage>() {
    override fun parse(reader: JsonReader): WaterApp.Stage =
        WaterApp.Stage(reader.nextString())

    override fun serialize(writer: JsonWriter, value: WaterApp.Stage) {
        writer.value(value.name)
    }
}

class DurationAdapter : NullableAdapter<Duration>() {
    override fun parse(reader: JsonReader): Duration =
        reader.nextLong().milliseconds

    override fun serialize(writer: JsonWriter, value: Duration) {
        writer.value(value.inWholeMilliseconds)
    }
}

class LocalTimeAdapter {
    @ToJson
    fun toJson(value: LocalTime): String = value.toString()

    @FromJson
    fun fromJson(value: String): LocalTime = LocalTime.parse(value)
}