package com.example.featuretrack.ui.map.viewmodel

import android.location.Location
import com.example.domain.domain.model.VehicleInfo
import com.example.domain.domain.usecase.GetVehiclesUseCase
import com.example.featuretrack.model.VehicleClusterItem
import com.example.featuretrack.model.VehicleUiInfo
import com.google.android.gms.maps.model.Marker
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test

class TrackViewModelTest {
    private lateinit var useCase: GetVehiclesUseCase
    private lateinit var viewModel: TrackViewModel
    private val marker: Marker = mockk()

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        useCase = mockk()
        Dispatchers.setMain(testDispatcher)
    }

    private fun initViewModel() = TrackViewModel(useCase)

    private fun domainList(): List<VehicleInfo> {
        val vehicleInfo = VehicleInfo(
            id = "test_1",
            type = "vehicle",
            batteryLevel = 20,
            lat = 53.02,
            lng = 13.32,
            maxSpeed = 20,
            vehicleType = "escooter",
            hasHelmetBox = false
        )
        return listOf(
            vehicleInfo.copy(distance = 1000.0f),
            vehicleInfo.copy(id = "test_2", lat = 100.5, lng = 32.6, distance = 2000.0f),
            vehicleInfo.copy(id = "test_3", lat = 10.5, lng = 54.6, distance = 3000.0f),
        )
    }

    private fun uiList(): List<VehicleUiInfo> {
        val vehicleUiInfo = VehicleUiInfo(
            id = "test_1",
            batteryLevel = 20,
            clusterItem = VehicleClusterItem(53.02, 13.32, "test_1", "escooter"),
            maxSpeed = 20,
            vehicleType = "escooter",
            hasHelmetBox = false,
            distance = 1.0
        )
        return listOf(
            vehicleUiInfo,
            vehicleUiInfo.copy(
                id = "test_2",
                clusterItem = VehicleClusterItem(100.5, 32.6, "test_2", "escooter"),
                distance = 2.0
            ),
            vehicleUiInfo.copy(
                id = "test_3",
                clusterItem = VehicleClusterItem(10.5, 54.6, "test_3", "escooter"),
                distance = 3.0
            )
        )
    }

    private fun userLocation(): Location {
        val userLocation = Location("user")
        userLocation.run {
            latitude = 54.02
            longitude = 14.02
        }
        return userLocation
    }

    @Test
    fun `test onLocationAccessed event return success state`() = runTest {
        val provided = GetVehiclesUseCase.Output.Success(domainList())
        val expected = uiList()
        coEvery {
            useCase.execute(any())
        } returns flow { emit(provided) }
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnLocationAccessed(userLocation()))

        viewModel.viewState
            .take(1)
            .collectLatest { state ->
                MatcherAssert.assertThat(
                    state.vehicles,
                    CoreMatchers.equalTo(expected)
                )
            }
    }

    @Test
    fun `test onLocationAccessed event return network error effect`() = runTest {
        coEvery {
            useCase.execute(any())
        } returns flow { emit(GetVehiclesUseCase.Output.NetworkError) }
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnLocationAccessed(userLocation()))

        viewModel.effect
            .take(1)
            .collectLatest { effect ->
                assert(effect is TrackContract.Effect.NetworkErrorEffect)
            }
    }

    @Test
    fun `test onLocationAccessed event return unknown error effect`() = runTest {
        val message = "test"
        coEvery {
            useCase.execute(any())
        } returns flow { emit(GetVehiclesUseCase.Output.UnknownError(message)) }
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnLocationAccessed(userLocation()))

        viewModel.effect
            .take(1)
            .collectLatest { effect ->
                assert(effect is TrackContract.Effect.UnknownErrorEffect)
                assert(message == (effect as TrackContract.Effect.UnknownErrorEffect).message)
            }
    }

    @Test
    fun `test onRetry event return InitLocationAccessEffect effect`() = runTest {
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnRetry)

        viewModel.effect
            .take(1)
            .collectLatest { effect ->
                assert(effect is TrackContract.Effect.InitLocationAccessEffect)
            }
    }

    @Test
    fun `test OnMarkerClicked event change nearBy Vehicle state`() = runTest {
        val provided = GetVehiclesUseCase.Output.Success(domainList())
        val expected = uiList()[1]
        coEvery {
            useCase.execute(any())
        } returns flow { emit(provided) }

        every {
            marker.title
        } returns "test_2"

        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnLocationAccessed(userLocation()))

        viewModel.onEvent(TrackContract.Event.OnMarkerClicked(marker))

        viewModel.viewState
            .take(1)
            .collectLatest { state ->
                MatcherAssert.assertThat(
                    state.nearestVehicle,
                    CoreMatchers.equalTo(expected)
                )
            }
    }

    @Test
    fun `test OnFragmentStart event return InitLocationAccessEffect effect`() = runTest {
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnRetry)

        viewModel.effect
            .take(1)
            .collectLatest { effect ->
                assert(effect is TrackContract.Effect.InitLocationAccessEffect)
            }
    }

    @Test
    fun `test OnNoGpsDialogClicked event return OpenLocationSettingsEffect effect`() = runTest {
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnNoGpsDialogClicked)

        viewModel.effect
            .take(1)
            .collectLatest { effect ->
                assert(effect is TrackContract.Effect.OpenLocationSettingsEffect)
            }
    }

    @Test
    fun `test OnUnableDialogClicked event return OpenApplicationSettingsEffect effect`() = runTest {
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnUnableDialogClicked)

        viewModel.effect
            .take(1)
            .collectLatest { effect ->
                assert(effect is TrackContract.Effect.OpenApplicationSettingsEffect)
            }
    }

    @Test
    fun `test OnPermissionDenied event return PermissionDeniedEffect effect`() = runTest {
        viewModel = initViewModel()
        viewModel.onEvent(TrackContract.Event.OnPermissionDenied)

        viewModel.effect
            .take(1)
            .collectLatest { effect ->
                assert(effect is TrackContract.Effect.PermissionDeniedEffect)
            }
    }

    @Test
    fun `test OnPermissionRationaleDialogClicked event return PermissionRequestEffect effect`() =
        runTest {
            viewModel = initViewModel()
            viewModel.onEvent(TrackContract.Event.OnPermissionRationaleDialogClicked)

            viewModel.effect
                .take(1)
                .collectLatest { effect ->
                    assert(effect is TrackContract.Effect.PermissionRequestEffect)
                }
        }

    @org.junit.After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
