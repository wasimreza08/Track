package com.example.featuretrack.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.core.delegate.viewBinding
import com.example.feature_track.R
import com.example.feature_track.databinding.MapFragmentBinding
import com.example.featuretrack.viewmodel.TrackViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackFragment : Fragment(R.layout.map_fragment), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener {

    private lateinit var mMap: GoogleMap
    private val binding: MapFragmentBinding by viewBinding(MapFragmentBinding::bind)
    private val viewModel: TrackViewModel by viewModels()
    //private val mViewModel: SharedViewModel by viewModels()
    // private val mNormalMarkers: ArrayList<Marker> = ArrayList()
    //private  var mSelectedVehicle:UiVehicleData? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleMap.onCreate(savedInstanceState)
        binding.googleMap.getMapAsync(this)
        binding.googleMap.onResume()
        val standardBottomSheetBehavior =
            BottomSheetBehavior.from(binding.sheet.standardBottomSheet)
        standardBottomSheetBehavior.isHideable = false
        standardBottomSheetBehavior.peekHeight = 150
        standardBottomSheetBehavior.isDraggable = true
        standardBottomSheetBehavior.skipCollapsed = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraIdleListener(this)
        viewModel.loadData()
        //addSelectedVehicle()
        // mViewModel.fetchVehicles().observe(viewLifecycleOwner) { onObserved(it) }
    }

    override fun onCameraIdle() {
        val bounds = mMap.projection.visibleRegion.latLngBounds

        val bound = doubleArrayOf(
            bounds.southwest.latitude, bounds.southwest.longitude,
            bounds.northeast.latitude, bounds.northeast.longitude
        )
    }

/*    private fun onObserved(result: Event<DataState<List<UiVehicleData>>>){
        val data = result.getContentIfNotHandled()
        if(data?.status == Status.SUCCESS){
            removeNormalMarker()
            addNormalMarkers(data.data?: emptyList())
        } else if(data?.status == Status.ERROR){
            onError(activity, true)
        }
    }*/

/*    override fun onCameraIdle() {
        val bounds = mMap.projection.visibleRegion.latLngBounds

        val bound = doubleArrayOf(
            bounds.southwest.latitude, bounds.southwest.longitude,
            bounds.northeast.latitude, bounds.northeast.longitude
        )
        mViewModel.setBound(bound)
    }

    private fun addSelectedVehicle() {
        mSelectedVehicle?.let {  val selectedPosition = LatLng(
            it.lat,
            it.lon
        )
            val title = getString(R.string.id) + it.id
            val snippet: String =
                getString(R.string.type) + it.fleetType + ", " + getString(R.string.heading) + it.heading
            addMarker(
                selectedPosition,
                it.heading.toFloat(),
                selectedVehicleIcon(),
                title,
                snippet
            )
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    selectedPosition,
                    Constants.ZOOM_LEVEL.toFloat()
                )
            ) }
        if(mSelectedVehicle == null) {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    Constants.UNSELECTED_FOCUS,
                    Constants.ZOOM_LEVEL.toFloat()
                )
            )
        }
        //addNormalMarkers()
    }

    private fun removeNormalMarker() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mNormalMarkers.forEach { it.remove() }
        } else {
            Handler(Looper.getMainLooper()).post {
                mNormalMarkers.forEach { it.remove() }
            }
        }

    }

    private fun addNormalMarkers(result : List<UiVehicleData>) {
        val unSelectedVehicles = mViewModel.filterUnselectedVehicle(result)
        unSelectedVehicles?.forEach { unSelectedVehicle->
            val position = LatLng(
                unSelectedVehicle.lat,
                unSelectedVehicle.lon
            )
            mNormalMarkers.add(
                addMarker(
                    position,
                    unSelectedVehicle.heading.toFloat(),
                    unselectedVehicleIcon()
                )
            )
        }
    }

    private fun selectedVehicleIcon() = R.drawable.img_car_selected


    private fun unselectedVehicleIcon() = R.drawable.img_car_top_view


    private fun addMarker(
        position: LatLng,
        rotation: Float = 0f,
        icon: Int,
        title: String = Constants.EMPTY_STRING,
        snippet: String = Constants.EMPTY_STRING
    ): Marker {
        val markar: Marker = mMap.addMarker(
            MarkerOptions().position(position).title(title).snippet(snippet)
                .icon(
                    bitmapDescriptorFromVector(context, icon)
                )
        )
        // tag is set for testing
        markar.tag = icon
        markar.rotation = rotation
        if (rotation < 120 || rotation > 240) {
            //angle between 120 to 240 can hide the vehicle by info window
            markar.showInfoWindow()
        }
        return markar
    }*/
}