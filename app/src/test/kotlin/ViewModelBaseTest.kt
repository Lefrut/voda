import androidx.lifecycle.ViewModel
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.datastore.DataStorePrefs
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.ui.mvi.MviViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

abstract class ViewModelTestBase<T : ViewModel>: CoroutineTestBase() {


    protected lateinit var viewModel: T
    protected lateinit var cartManager: CartManager
    protected lateinit var likeManager: LikeManager
    protected lateinit var vodovozServiceRepository: VodovozServiceRepository
    protected lateinit var accountManager: AccountManager
    protected lateinit var dataStorePrefs: DataStorePrefs
    protected lateinit var userPreferencesRepository: UserPreferencesRepository
    protected lateinit var resourcesProvider: ResourcesProvider


    @Before
    override fun setUpBase() {
        super.setUpBase()

        vodovozServiceRepository = mockk(relaxed = true)
        accountManager = mockk(relaxed = true)
        dataStorePrefs = mockk(relaxed = true)
        cartManager = mockk(relaxed = true)
        likeManager = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)
        resourcesProvider = mockk(relaxed = true)

        viewModel = createViewModel()
    }

    protected abstract fun createViewModel(): T
}