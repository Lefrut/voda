import com.vodovoz.app.feature.home.HomeFlowViewModel

class HomeFlowViewModelTests : ViewModelTestBase<HomeFlowViewModel>() {

    override fun createViewModel(): HomeFlowViewModel {
        return HomeFlowViewModel(
            cartManager,
            likeManager,
            accountManager,
            vodovozServiceRepository,
            resourcesProvider,
            userPreferencesRepository
        )
    }


}