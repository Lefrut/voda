import androidx.lifecycle.ViewModel
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.datastore.DataStorePrefs
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTestBase<T : ViewModel> {

    protected val dispatcher = StandardTestDispatcher()

    protected lateinit var viewModel: T
    protected lateinit var cartManager: CartManager
    protected lateinit var likeManager: LikeManager
    protected lateinit var vodovozServiceRepository: VodovozServiceRepository
    protected lateinit var accountManager: AccountManager
    protected lateinit var dataStorePrefs: DataStorePrefs
    protected lateinit var userPreferencesRepository: UserPreferencesRepository
    protected lateinit var resourcesProvider: ResourcesProvider

    @Before
    open fun setUpBase() {
        Dispatchers.setMain(dispatcher)

        vodovozServiceRepository = mockk(relaxed = true)
        accountManager = mockk(relaxed = true)
        dataStorePrefs = mockk(relaxed = true)
        cartManager = mockk(relaxed = true)
        likeManager = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)
        resourcesProvider = mockk(relaxed = true)

        viewModel = createViewModel()
    }

    @After
    open fun tearDownBase() {
        Dispatchers.resetMain()
    }

    protected abstract fun createViewModel(): T
}