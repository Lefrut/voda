package com.m.vodovoz.feature.map

import ViewModelTestBase
import app.cash.turbine.test
import com.m.vodovoz.design_system.model.MapPointUi
import com.m.vodovoz.domain.general.MkadDistanceUseCase
import com.m.vodovoz.domain.general.model.location.MapAddressModel
import com.m.vodovoz.feature.map.MapFlowViewModel.MapUiMode
import com.m.vodovoz.feature.map.model.MapAddressUi
import com.m.vodovoz.feature.map.model.toUi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class MapFlowViewModelTest : ViewModelTestBase<MapFlowViewModel>() {

    private val mkadDistanceUseCase = mockk<MkadDistanceUseCase>(relaxed = true)

    override fun createViewModel(): MapFlowViewModel {

        every { savedStateHandle.get<String>(any()) } returns null
        coEvery { vodovozServiceRepository.getMapAreas() } returns flow { throw Throwable() }

        return spyk(
            MapFlowViewModel(
                savedState = savedStateHandle,
                mapServiceRepository = mapServiceRepository,
                vodovozServiceRepository = vodovozServiceRepository,
                mkadDistanceUseCase = mkadDistanceUseCase
            )
        )
    }


    @Test
    fun navigateBack() = runTest {
        viewModel.events.test {

            viewModel.navigateBack().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.GoBack, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun plusZoom() = runTest {
        viewModel.events.test {

            viewModel.plusZoom().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.MoveCameraPlus, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun minusZoom() = runTest {
        viewModel.events.test {

            viewModel.minusZoom().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.MoveCameraMinus, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun checkGeo() = runTest {
        viewModel.events.test {

            viewModel.checkGeo().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.CheckGeo, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changeQuery() = runTest {
        val addresses = listOf("1", "2", "3")
        coEvery { mapServiceRepository.getAddressesInMoscowByQuery(any()) } returns flowOf(
            Result.success(addresses)
        )

        viewModel.state.test {
            awaitItem()
            viewModel.changeQuery("123").join()
            assertEquals("123", awaitItem().query)
            assertEquals(addresses, awaitItem().recommendedAddresses)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun searchAddressByQuery() {
        every { viewModel.searchAddress("1") } returns Job()

        viewModel.searchAddressByQuery()

        verify {
            viewModel.searchAddress("")
        }
    }

    @Test
    fun moveToAvailableGeo() = runTest {
        val addressPoint = MapPointUi(1.0, 1.0)

        every { viewModel.stateSnapshot.currentMapAddress?.point } returnsMany listOf(
            addressPoint,
            null
        )

        viewModel.events.test {
            viewModel.moveToAvailableGeo().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.MoveToAddress(addressPoint), awaitItem())
            viewModel.moveToAvailableGeo().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.MoveToGeoOrMoscow, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        every { savedStateHandle.get<String>(any()) } returns "addressName"
        val viewModel2 = spyk(
            MapFlowViewModel(
                savedState = savedStateHandle,
                mapServiceRepository = mapServiceRepository,
                vodovozServiceRepository = vodovozServiceRepository,
                mkadDistanceUseCase = mkadDistanceUseCase
            )
        )
        every { viewModel2.searchAddress("addressName") } returns Job()
        viewModel2.moveToAvailableGeo().join()
        verify { viewModel2.searchAddress("addressName") }

    }

    @Test
    fun moveToUserGeo() = runTest {
        viewModel.events.test {
            viewModel.moveToUserGeo().join()
            assertEquals(
                MapFlowViewModel.MapFlowEvents.MoveToGeoOrMoscow,
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun showSettingDialog() = runTest {
        viewModel.showSettingDialog().join()

        assertEquals(true, viewModel.stateSnapshot.showSettingsDialog)
    }

    @Test
    fun closeSettingsDialog() = runTest {
        viewModel.setState(MapFlowViewModel.MapFlowState(showSettingsDialog = true))
        viewModel.closeSettingsDialog().join()

        assertEquals(false, viewModel.stateSnapshot.showSettingsDialog)
    }

    @Test
    fun showAddressBottomSheet() = runTest {

        viewModel.events.test {
            viewModel.showAddressBottomSheet().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.ShowAddressBottomSheet, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun hideAddressBottomSheet() = runTest {
        viewModel.events.test {
            viewModel.hideAddressBottomSheet().join()
            assertEquals(MapFlowViewModel.MapFlowEvents.HideAddressBottomSheet, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchAddress() = runTest(timeout = 10.seconds) {
//        val moscow = "Moscow 1"
//        val mapAddress = MapAddressModel.Empty.copy(city = moscow)
//        val throwable = Throwable()
//        val updatedMapAddress = mapAddress.toUi()
//
//        coEvery { mapServiceRepository.searchAddressInMoscow(moscow) } returnsMany listOf(
//            flowOf(Result.success(mapAddress)),
//            flowOf(Result.failure(throwable))
//        )
//        every { viewModel.changeQuery(any()) } returns Job().apply { complete() }
//
//
//        viewModel.state.test {
//            viewModel.searchAddress(moscow).join()
//            val item1 = awaitItem()
//            assertEquals(true, item1.addressIsLoading)
//            assertEquals(
//                item1.copy(
//                    currentMapAddress = updatedMapAddress,
//                    mode = MapUiMode.OnlyMap,
//                    addressIsLoading = false,
//                    addressIsError = with(updatedMapAddress) { house.isBlank() }
//                ),
//                awaitItem()
//            )
//
//            cancelAndIgnoreRemainingEvents()
//        }
    }

    @Test
    fun searchAddressByMapPoint() {
    }

    @Test
    fun changeToSearchMode() {
    }

    @Test
    fun navigateToLocationSettings() {
    }

    @Test
    fun navigateToAddAddress() {
    }

    @Test
    fun showDeliveryBottomSheet() {
    }

    @Test
    fun closeDeliveryBottomSheet() {
    }

}
