package com.cmc.taximeter

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import pub.devrel.easypermissions.EasyPermissions

class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var driverMarker: Marker? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set status bar to black
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Vérifier les services Google Play
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (status != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(requireActivity(), status, 2404)?.show()
        }

        // Initialisation du FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Charger la carte
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Vérifier et demander les permissions de localisation
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            startLocationUpdates()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Cette application nécessite un accès à votre position pour fonctionner.",
                LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        // Activer la couche de localisation sur la carte (le bouton de localisation)
        map.isMyLocationEnabled = true
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // Mise à jour toutes les secondes
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocationOnMap(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun updateLocationOnMap(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)

        if (driverMarker == null) {
            driverMarker = map.addMarker(MarkerOptions().position(userLocation).title("Position du Chauffeur"))
        } else {
            driverMarker?.position = userLocation
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                // Afficher un message d'erreur si la permission est refusée
                Toast.makeText(requireContext(), "Permission de localisation refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


