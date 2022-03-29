package com.example.featuretrack.ui.map.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.core.delegate.viewBinding
import com.example.core.ext.doNotLeak
import com.example.core.ext.exhaustive
import com.example.feature_track.R
import com.example.feature_track.databinding.FragmentMapBinding
import com.example.featuretrack.common.Utils
import com.example.featuretrack.model.VehicleClusterItem
import com.example.featuretrack.model.VehicleUiInfo
import com.example.featuretrack.ui.map.viewmodel.TrackContract
import com.example.featuretrack.ui.map.viewmodel.TrackViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

    private lateinit var googleMap: GoogleMap
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val viewModel: TrackViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cancellationTokenSource: CancellationTokenSource

    private lateinit var clusterManager: ClusterManager<VehicleClusterItem>

    companion object {
        private const val MAP_INITIAL_ZOOM_LEVEL = 12f
        private const val ZOOM_INCREASED_LEVEL = 3f
        private const val PACKAGE = "package"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleMap.onCreate(savedInstanceState)
        binding.googleMap.getMapAsync(this)
        binding.googleMap.onResume()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.btnRetry.setOnClickListener {
            viewModel.onEvent(TrackContract.Event.OnRetry)
        }
        observeEffect()
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is TrackContract.Effect.InitLocationAccessEffect -> initiateLocationAccess()
                        is TrackContract.Effect.NetworkErrorEffect -> {
                            showSnackBar(getString(R.string.network_error))
                        }
                        is TrackContract.Effect.UnknownErrorEffect -> {
                            showSnackBar(getString(R.string.unknown_error))
                        }
                        is TrackContract.Effect.OpenApplicationSettingsEffect -> {
                            startApplicationSettings()
                        }
                        is TrackContract.Effect.OpenLocationSettingsEffect -> {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                        is TrackContract.Effect.PermissionDeniedEffect -> {
                            showUnableDialog()
                        }
                        is TrackContract.Effect.PermissionRequestEffect -> {
                            requestLocationPermission()
                        }
                    }.exhaustive
                }
            }
        }
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(binding.container, text, Snackbar.LENGTH_LONG).show()
    }

    private fun observeViewState() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { viewState ->
                    setLoading(viewState.isLoading)
                    viewState.nearestVehicle?.let { addNearestVehicleInfo(it) }
                    if (viewState.vehicles.isNotEmpty()) {
                        addItems(viewState.vehicles)
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progress.visibility = View.VISIBLE
        } else {
            binding.progress.visibility = View.GONE
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun addNearestVehicleInfo(nearestVehicle: VehicleUiInfo) {
        with(binding.sheet) {
            tvType.text = nearestVehicle.vehicleType
            tvMaxSpeed.text = getString(R.string.max_speed, nearestVehicle.maxSpeed)
            tvBatteryLevel.text = getString(R.string.battery_level, nearestVehicle.batteryLevel)
            tvDistance.text = getString(R.string.distance, nearestVehicle.distance)
            tvHasHelmetBox.text = if (nearestVehicle.hasHelmetBox) {
                getString(R.string.has_helmet_box)
            } else {
                getString(R.string.no_helmet_box)
            }
            ivImage.setImageResource(Utils.getVehicleDrawableId(nearestVehicle.vehicleType))
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpClusterer() {
        val markerManager = markerManager()
        clusterManager = ClusterManager(context, googleMap, markerManager)
        clusterManager.renderer = TrackClusterRenderer(requireContext(), googleMap, clusterManager)
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)
    }

    private fun addItems(vehicleList: List<VehicleUiInfo>) {
        val clusterItems = vehicleList.map { it.clusterItem }
        clusterManager.addItems(clusterItems)
        clusterManager.cluster()
    }

    override fun onStart() {
        super.onStart()
        cancellationTokenSource = CancellationTokenSource()
        viewModel.onEvent(TrackContract.Event.OnFragmentStart)
    }

    private fun initiateLocationAccess() {
        if (checkPermission()) {
            // access location
            accessUserLocation()
        } else {
            showPermissionRationaleDialog()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.setOnCameraIdleListener(this)
        setUpClusterer()
        observeViewState()
    }

    private fun checkPermission(): Boolean {
        return (
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    @SuppressLint("MissingPermission")
    private fun accessUserLocation() {
        if (isGPSEnabled()) {
            fusedLocationClient
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                location?.let {
                    addUserLocationMarker(LatLng(it.latitude, it.longitude))
                    viewModel.onEvent(TrackContract.Event.OnLocationAccessed(location))
                }
            }
        } else {
            buildDialogNoGps()
        }
    }

    private fun isGPSEnabled(): Boolean {
        var locationManager: LocationManager? = null

        if (locationManager == null) {
            locationManager =
                activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    private fun markerManager(): MarkerManager {
        return object : MarkerManager(this.googleMap) {
            override fun onMarkerClick(marker: Marker): Boolean {
                val currentZoom = googleMap.cameraPosition.zoom
                if (marker.title == null) {
                    moveToMap(marker.position, currentZoom + ZOOM_INCREASED_LEVEL)
                }
                viewModel.onEvent(TrackContract.Event.OnMarkerClicked(marker))
                return super.onMarkerClick(marker)
            }
        }
    }

    private fun addUserLocationMarker(latLng: LatLng) {
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.your_position))
        )
        moveToMap(latLng)
    }

    private fun moveToMap(latLng: LatLng, mapZoom: Float = MAP_INITIAL_ZOOM_LEVEL) {
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latLng.latitude, latLng.longitude),
                mapZoom
            )
        )
    }

    private fun showPermissionRationaleDialog() {
        createDialog(
            getString(R.string.title_location_logic),
            getString(R.string.message_location_logic),
            getString(R.string.yes),
            TrackContract.Event.OnPermissionRationaleDialogClicked
        )
    }

    private fun requestLocationPermission() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun buildDialogNoGps() {
        createDialog(
            getString(R.string.gps_not_enable_title),
            getString(R.string.gps_not_enable_message),
            getString(R.string.yes),
            TrackContract.Event.OnNoGpsDialogClicked
        )
    }

    private fun startApplicationSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                this.data = Uri.fromParts(PACKAGE, activity?.packageName, null)
            }
        )
    }

    private val requestMultiplePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.entries.first().value && permissions.entries.last().value) {
                accessUserLocation()
            } else {
                viewModel.onEvent(TrackContract.Event.OnPermissionDenied)
            }
        }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

    private fun showUnableDialog() {
        createDialog(
            getString(R.string.unable_title),
            getString(R.string.unable_message),
            getString(R.string.continue_text),
            TrackContract.Event.OnUnableDialogClicked
        )
    }

    private fun createDialog(
        title: String,
        message: String,
        buttonString: String,
        event: TrackContract.Event
    ) {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.run {
            setTitle(title)
            setMessage(message)
            setButton(
                DialogInterface.BUTTON_POSITIVE, buttonString
            ) { _, _ -> viewModel.onEvent(event) }
            setCancelable(false)
            show()
            doNotLeak(lifecycleOwner = this@TrackFragment)
        }
    }

    override fun onCameraIdle() {
        // do nothing
    }
}
