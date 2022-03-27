package com.example.featuretrack.ui.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.core.delegate.viewBinding
import com.example.core.ext.doNotLeak
import com.example.core.ext.exhaustive
import com.example.core.ext.safeNavigate
import com.example.feature_track.R
import com.example.feature_track.databinding.FragmentPermissionBinding
import com.example.featuretrack.common.Utils
import com.example.featuretrack.model.GeoLocation
import com.example.featuretrack.ui.permission.viewmodel.PermissionContract
import com.example.featuretrack.ui.permission.viewmodel.PermissionViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PermissionFragment : Fragment(R.layout.fragment_permission) {
    private val binding: FragmentPermissionBinding by viewBinding(FragmentPermissionBinding::bind)
    private val viewModel: PermissionViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cancellationTokenSource: CancellationTokenSource

    companion object {
        private const val PACKAGE = "package"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.btnContinue.setOnClickListener {
            viewModel.onEvent(PermissionContract.Event.OnContinueClicked)
        }
        observeEffect()
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is PermissionContract.Effect.RequestPermissionEffect -> requestLocationPermission()
                        is PermissionContract.Effect.NavigationEffect -> {
                            navigateToMap(effect.location)
                        }
                        is PermissionContract.Effect.PermissionDeniedEffect -> {
                            showUnableDialog()
                        }
                        is PermissionContract.Effect.FragmentStartEffect -> {
                            initiateLocationAccess()
                        }
                        is PermissionContract.Effect.OpenLocationSettingsEffect -> {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                        is PermissionContract.Effect.OpenApplicationSettingsEffect -> {
                            startApplicationSettings()
                        }
                    }.exhaustive
                }
            }
        }
    }

    private fun navigateToMap(location: GeoLocation) {
        findNavController().safeNavigate(
            PermissionFragmentDirections.actionPermissionFragmentToTrackFragment(location)
        )
    }

    private val requestMultiplePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.entries.first().value && permissions.entries.last().value) {
                accessUserLocation()
                Timber.e("DEBUG permission accepted")
            } else {
                viewModel.onEvent(PermissionContract.Event.OnPermissionDenied)
                Timber.e("DEBUG permission not accepted")
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

    override fun onStart() {
        super.onStart()
        cancellationTokenSource = CancellationTokenSource()
        viewModel.onEvent(PermissionContract.Event.OnFragmentStart)
    }

    private fun initiateLocationAccess() {
        val isPermissionNeeded = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        if (isPermissionNeeded != null && isPermissionNeeded) {
            // access location
            accessUserLocation()
        }
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
                    viewModel.onEvent(
                        PermissionContract.Event.OnLocationAccessed(
                            GeoLocation(location.latitude, location.longitude)
                        )
                    )
                }
            }
        } else {
            buildDialogNoGps()
        }
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
                }
            }

        val alert = builder.create()
        alert.show()
        alert.doNotLeak(this)
    }

    private fun startApplicationSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                this.data = Uri.fromParts(PACKAGE, activity?.packageName, null)
            }
        )
    }

    private fun showUnableDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.run {
            setTitle(getString(R.string.unable_title))
            setMessage(getString(R.string.unable_message))
            setButton(
                DialogInterface.BUTTON_POSITIVE, getString(R.string.continue_text)
            ) { _, _ -> viewModel.onEvent(PermissionContract.Event.OnUnableDialogClicked) }
            setCancelable(false)
            show()
            doNotLeak(this@PermissionFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }
}