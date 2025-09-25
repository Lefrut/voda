import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.datastore.DataStorePrefs
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.domain.general.respository.MapServiceRepository
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import io.mockk.mockk
import org.junit.Before

abstract class ViewModelTestBase<T : ViewModel> : CoroutineTestBase() {


    protected lateinit var viewModel: T
    protected lateinit var cartManager: CartManager
    protected lateinit var likeManager: LikeManager
    protected lateinit var vodovozServiceRepository: VodovozServiceRepository
    protected lateinit var accountManager: AccountManager
    protected lateinit var dataStorePrefs: DataStorePrefs
    protected lateinit var userPreferencesRepository: UserPreferencesRepository
    protected lateinit var resourcesProvider: ResourcesProvider
    protected lateinit var savedStateHandle: SavedStateHandle
    protected lateinit var mapServiceRepository: MapServiceRepository


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
        savedStateHandle = mockk(relaxed = true)
        mapServiceRepository = mockk(relaxed = true)

        viewModel = createViewModel()
    }

    protected abstract fun createViewModel(): T
}