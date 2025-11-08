package com.cmc.taximeter

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var driverMarker: Marker? = null
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set status bar to black
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Check Google Play Services availability
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (status != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(requireActivity(), status, 2404)?.show()
            return
        }

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocationOnMap(location)
                }
            }
        }

        // Load the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }

        // Check and request permissions
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, check if GPS is enabled
                if (isLocationEnabled()) {
                    enableMyLocation()
                } else {
                    showGPSDisabledDialog()
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show explanation and request permission
                Toast.makeText(
                    requireContext(),
                    "L'application a besoin d'accéder à votre position pour fonctionner",
                    Toast.LENGTH_LONG
                ).show()
                requestLocationPermission()
            }
            else -> {
                // Request permission directly
                requestLocationPermission()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showGPSDisabledDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Localisation désactivée")
            .setMessage("Veuillez activer la localisation pour utiliser cette fonctionnalité")
            .setPositiveButton("Activer") { _, _ ->
                // Open location settings
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "L'application ne peut pas fonctionner sans localisation",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Check again if location is enabled before proceeding
        if (!isLocationEnabled()) {
            showGPSDisabledDialog()
            return
        }

        try {
            map.isMyLocationEnabled = true
            startLocationUpdates()

            // Get last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Impossible d'obtenir votre position",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Erreur de permission: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationUpdates() {
        // Check if location is enabled before starting updates
        if (!isLocationEnabled()) {
            showGPSDisabledDialog()
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2 seconds update
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationOnMap(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)

        if (driverMarker == null) {
            driverMarker = map.addMarker(
                MarkerOptions()
                    .position(userLocation)
                    .title("Ma Position")
            )
            // Only animate on first location
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        } else {
            driverMarker?.position = userLocation
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now check if GPS is enabled
                if (isLocationEnabled()) {
                    if (::map.isInitialized) {
                        enableMyLocation()
                        Toast.makeText(requireContext(), "Permission accordée", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // GPS is off, show dialog
                    showGPSDisabledDialog()
                }
            } else {
                // Permission denied
                Toast.makeText(
                    requireContext(),
                    "Permission refusée. L'application ne peut pas accéder à votre position",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop location updates when fragment is not visible
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Check if GPS is enabled
            if (isLocationEnabled()) {
                // Resume location updates
                if (::map.isInitialized) {
                    startLocationUpdates()
                }
            } else {
                // GPS was turned off, show dialog
                showGPSDisabledDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}