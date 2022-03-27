package com.example.featuretrack.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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
import timber.log.Timber

@AndroidEntryPoint
class TrackFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

    private lateinit var googleMap: GoogleMap
    private val binding: FragmentMapBinding by viewBinding(FragmentMapBinding::bind)
    private val viewModel: TrackViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cancellationTokenSource: CancellationTokenSource
    private lateinit var locationCallback: LocationCallback
    private lateinit var clusterManager: ClusterManager<VehicleClusterItem>

    companion object {
        private const val MAP_INITIAL_ZOOM_LEVEL = 12f
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleMap.onCreate(savedInstanceState)
        binding.googleMap.getMapAsync(this)
        // todo where is the best place to call it
        binding.googleMap.onResume()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationCallback = initLocationCallback()
        binding.btnRetry.setOnClickListener {
            viewModel.onEvent(TrackContract.Event.OnRetry)
        }
        val safeArgs: TrackFragmentArgs by navArgs()
        val userLocation = Location("user location")
        userLocation.latitude = safeArgs.location.lat
        userLocation.latitude = safeArgs.location.lng
        viewModel.onEvent(TrackContract.Event.OnLocationAccessed(userLocation))
        observeEffect()
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is TrackContract.Effect.OnRetryLocationAccess -> initiateLocationAccess()
                        is TrackContract.Effect.OnNetworkError -> {
                            showSnackBar(getString(R.string.network_error))
                        }
                        is TrackContract.Effect.OnUnknownError -> {
                            showSnackBar(getString(R.string.unknown_error))
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
                        setUpClusterer(viewState.vehicles)
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

    private fun addNearestVehicleInfo(nearestVehicle: VehicleUiInfo) {
        Timber.e("nearest vehicle $nearestVehicle")
        with(binding.sheet) {
            tvType.text = nearestVehicle.vehicleType
            tvMaxSpeed.text = getString(R.string.max_speed, nearestVehicle.maxSpeed)
            tvBatteryLevel.text = getString(R.string.battery_level, nearestVehicle.batteryLevel)
            tvHasHelmetBox.text = if (nearestVehicle.hasHelmetBox) {
                getString(R.string.has_helmet_box)
            } else {
                getString(R.string.no_helmet_box)
            }
            ivImage.setImageResource(Utils.getVehicleDrawableId(nearestVehicle.vehicleType))
        }
    }

    private fun setUpClusterer(vehicleList: List<VehicleUiInfo>) {
        // Position the map.
        val markerManager = markerManager()
        clusterManager = ClusterManager(context, googleMap, markerManager)

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)

        // Add cluster items (markers) to the cluster manager.
        addItems(vehicleList)
        clusterManager.renderer = TrackClusterRenderer(requireContext(), googleMap, clusterManager)
    }

    private fun addItems(vehicleList: List<VehicleUiInfo>) {
        val clusterItems = vehicleList.map { it.clusterItem }
        clusterManager.addItems(clusterItems)
    }

    override fun onStart() {
        super.onStart()
        cancellationTokenSource = CancellationTokenSource()
        initiateLocationAccess()
    }

    private fun initiateLocationAccess() {
        val isNeeded = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        Timber.e("permission needed $isNeeded")
        if (checkPermission()) {
            // access location
            accessUserLocation()
            Timber.e("access location")
        } else {
            showPermissionRationaleDialog()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.setOnCameraIdleListener(this)
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
        if (Utils.isGPSEnabled(activity)) {
            fusedLocationClient
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                location?.let {
                    moveToMap(LatLng(location.latitude, location.longitude))
                    viewModel.onEvent(TrackContract.Event.OnLocationAccessed(location))
                    Timber.e("location: ${location.latitude}")
                }
            }
        } else {
            buildDialogNoGps()
        }
    }

    private fun initLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation.also { location ->
                    moveToMap(LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    private fun markerManager(): MarkerManager {
        return object : MarkerManager(this.googleMap) {
            override fun onMarkerClick(marker: Marker): Boolean {
                Timber.e("marker clicked: ${marker.title}")
                viewModel.onEvent(TrackContract.Event.OnMarkerClickedClicked(marker))
                return super.onMarkerClick(marker)
            }
        }
    }

    private fun moveToMap(latLng: LatLng, mapZoom: Float = MAP_INITIAL_ZOOM_LEVEL) {
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.your_position))
        )
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latLng.latitude, latLng.longitude),
                mapZoom
            )
        )
    }

    private fun showPermissionRationaleDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.run {
            setTitle(getString(R.string.title_location_logic))
            setMessage(getString(R.string.message_location_logic))
            setButton(
                DialogInterface.BUTTON_POSITIVE, getString(R.string.yes)
            ) { _, _ -> requestLocationPermission() }
            setCancelable(false)
            show()
            doNotLeak(this@TrackFragment)
        }
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
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.gps_not_enable_message))
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialog, _ ->
                run {
                    dialog.cancel()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }

        val alert = builder.create()
        alert.show()
        alert.doNotLeak(this)
    }

    private val requestMultiplePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.entries.first().value && permissions.entries.last().value) {
                accessUserLocation()
                Timber.e("DEBUG permission accepted")
            } else {
                Timber.e("DEBUG permission not accepted")
            }
        }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

    override fun onCameraIdle() {
        // do nothing
    }
}
