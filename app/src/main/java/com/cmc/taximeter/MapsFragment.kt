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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * MapsFragment
 *
 * Displays an interactive Google Map with real-time location tracking for taxi drivers.
 *
 * Key Features:
 * - Google Maps integration with custom UI controls
 * - Real-time GPS location tracking
 * - Dynamic driver position marker
 * - Location permission handling
 * - GPS enable/disable detection
 * - Google Play Services availability checking
 * - Automatic camera positioning
 *
 * Location Updates:
 * - Update interval: 2 seconds
 * - Fastest interval: 1 second
 * - Priority: High accuracy (GPS)
 *
 * Permissions Required:
 * - ACCESS_FINE_LOCATION: For precise GPS tracking
 * - ACCESS_COARSE_LOCATION: For network-based location
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    // ============================================
    // PROPERTIES
    // ============================================

    /** Google Map instance for displaying the map */
    private lateinit var map: GoogleMap

    /** Client for accessing device location services */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /** Marker representing the driver's current position on the map */
    private var driverMarker: Marker? = null

    /** Callback for receiving location updates */
    private lateinit var locationCallback: LocationCallback

    // ============================================
    // COMPANION OBJECT (CONSTANTS)
    // ============================================

    companion object {
        /** Request code for location permission requests */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Called when the fragment's view is created.
     *
     * Responsibilities:
     * 1. Sets status bar color to black
     * 2. Checks Google Play Services availability
     * 3. Initializes location provider client
     * 4. Sets up location update callback
     * 5. Loads the Google Map asynchronously
     *
     * Google Play Services Check:
     * - Verifies that Google Play Services is installed and up-to-date
     * - Shows error dialog if services are unavailable
     * - Required for Google Maps to function properly
     *
     * @param view The fragment's root view
     * @param savedInstanceState Bundle containing previous saved state (if any)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set status bar to black for consistent UI
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // ===== CHECK GOOGLE PLAY SERVICES =====
        // Google Play Services is required for Google Maps to work
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())

        if (status != ConnectionResult.SUCCESS) {
            // Services unavailable - show error dialog and stop initialization
            googleApiAvailability.getErrorDialog(requireActivity(), status, 2404)?.show()
            return
        }

        // ===== INITIALIZE LOCATION PROVIDER =====
        // FusedLocationProviderClient provides efficient location tracking
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // ===== SETUP LOCATION CALLBACK =====
        // This callback is invoked whenever location updates are received
        locationCallback = object : LocationCallback() {
            /**
             * Called when new location data is available.
             *
             * @param locationResult Contains one or more location updates
             */
            override fun onLocationResult(locationResult: LocationResult) {
                // Get the most recent location and update the map
                locationResult.lastLocation?.let { location ->
                    updateLocationOnMap(location)
                }
            }
        }

        // ===== LOAD GOOGLE MAP =====
        // Find the map fragment and request map initialization
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)  // onMapReady() will be called when ready
    }

    /**
     * Called when the Google Map is ready to be used.
     *
     * This is a callback method from OnMapReadyCallback interface.
     * It's invoked asynchronously once the map is initialized and ready for interaction.
     *
     * Map Configuration:
     * - Enables zoom controls (+/- buttons)
     * - Enables "My Location" button (centers on user)
     * - Enables compass (shows map orientation)
     *
     * After configuration, checks and requests location permissions.
     *
     * @param googleMap The initialized GoogleMap instance
     */
    override fun onMapReady(googleMap: GoogleMap) {
        // Store map reference for later use
        map = googleMap

        // Configure map UI settings
        map.uiSettings.apply {
            isZoomControlsEnabled = true           // Show +/- zoom buttons
            isMyLocationButtonEnabled = true       // Show "center on location" button
            isCompassEnabled = true                // Show compass when map is rotated
        }

        // Check permissions and enable location features
        checkLocationPermission()
    }

    // ============================================
    // PERMISSION HANDLING
    // ============================================

    /**
     * Checks location permission status and takes appropriate action.
     *
     * Permission Flow:
     * 1. If permission is GRANTED:
     *    - Check if GPS is enabled
     *    - If GPS enabled: Enable location tracking
     *    - If GPS disabled: Show dialog to enable GPS
     *
     * 2. If permission should show RATIONALE:
     *    - Show explanation toast to user
     *    - Request permission
     *
     * 3. If permission not requested yet:
     *    - Request permission directly
     *
     * This method implements a user-friendly permission request flow that
     * respects Android's permission best practices.
     */
    private fun checkLocationPermission() {
        when {
            // ===== CASE 1: PERMISSION ALREADY GRANTED =====
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, but need to check if GPS is enabled
                if (isLocationEnabled()) {
                    // GPS is on - proceed with location tracking
                    enableMyLocation()
                } else {
                    // GPS is off - prompt user to enable it
                    showGPSDisabledDialog()
                }
            }

            // ===== CASE 2: SHOULD SHOW RATIONALE =====
            // User has denied permission before, show explanation
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Explain why the app needs location permission
                Toast.makeText(
                    requireContext(),
                    "L'application a besoin d'accéder à votre position pour fonctionner",
                    Toast.LENGTH_LONG
                ).show()
                // Request permission after explanation
                requestLocationPermission()
            }

            // ===== CASE 3: FIRST TIME PERMISSION REQUEST =====
            else -> {
                // User hasn't been asked yet - request directly
                requestLocationPermission()
            }
        }
    }

    /**
     * Checks if location services (GPS or Network) are enabled on the device.
     *
     * Location Providers:
     * - GPS_PROVIDER: Satellite-based positioning (most accurate)
     * - NETWORK_PROVIDER: Cell tower/WiFi-based positioning (less accurate)
     *
     * Returns true if at least one provider is enabled.
     *
     * @return true if location services are enabled, false otherwise
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Displays a dialog prompting the user to enable GPS/location services.
     *
     * Dialog Actions:
     * - "Activer" (Enable): Opens device location settings
     * - "Annuler" (Cancel): Dismisses dialog and shows warning toast
     *
     * Dialog Behavior:
     * - Cannot be cancelled by tapping outside (setCancelable(false))
     * - Forces user to make a decision about enabling location
     *
     * Use Case:
     * Called when location permission is granted but GPS is disabled.
     */
    private fun showGPSDisabledDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Localisation désactivée")  // Location disabled
            .setMessage("Veuillez activer la localisation pour utiliser cette fonctionnalité")
            // Please enable location to use this feature
            .setPositiveButton("Activer") { _, _ ->
                // Open device location settings
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                // User cancelled - dismiss and show warning
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "L'application ne peut pas fonctionner sans localisation",
                    // The app cannot function without location
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)  // Must choose an option
            .show()
    }

    /**
     * Requests location permissions from the user.
     *
     * Permissions Requested:
     * - ACCESS_FINE_LOCATION: Precise GPS location
     * - ACCESS_COARSE_LOCATION: Approximate network location
     *
     * Both permissions are requested together for maximum compatibility.
     * The system will show a permission dialog to the user.
     *
     * Result handled in: onRequestPermissionsResult()
     */
    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Handles the result of permission requests.
     *
     * Callback Flow:
     * 1. Verify correct request code
     * 2. Check if permission was granted
     * 3. If granted:
     *    - Verify GPS is enabled
     *    - Enable location tracking if map is initialized
     *    - Show success toast
     * 4. If GPS disabled:
     *    - Show dialog to enable GPS
     * 5. If denied:
     *    - Show error toast
     *
     * @param requestCode The request code passed in requestPermissions()
     * @param permissions The requested permissions (array)
     * @param grantResults The grant results for each permission (GRANTED/DENIED)
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ===== PERMISSION GRANTED =====
                // Now check if GPS is enabled
                if (isLocationEnabled()) {
                    // GPS is on - enable location tracking
                    if (::map.isInitialized) {
                        enableMyLocation()
                        Toast.makeText(requireContext(), "Permission accordée", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // GPS is off - show dialog to enable it
                    showGPSDisabledDialog()
                }
            } else {
                // ===== PERMISSION DENIED =====
                Toast.makeText(
                    requireContext(),
                    "Permission refusée. L'application ne peut pas accéder à votre position",
                    // Permission denied. The app cannot access your location
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ============================================
    // LOCATION TRACKING
    // ============================================

    /**
     * Enables location tracking on the map.
     *
     * Prerequisites:
     * - Location permission must be granted
     * - GPS must be enabled
     *
     * Actions Performed:
     * 1. Verifies permission (safety check)
     * 2. Verifies GPS is enabled
     * 3. Enables "My Location" blue dot on map
     * 4. Starts continuous location updates
     * 5. Gets last known location
     * 6. Animates camera to user's position
     *
     * Error Handling:
     * - Catches SecurityException if permission is revoked
     * - Shows toast with error message
     *
     * Last Known Location:
     * - May be null if location was never obtained
     * - Provides immediate position without waiting for GPS fix
     * - Camera animates to position with zoom level 15
     */
    private fun enableMyLocation() {
        // ===== PERMISSION CHECK =====
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted - return without enabling location
            return
        }

        // ===== GPS CHECK =====
        // Double-check GPS is enabled before proceeding
        if (!isLocationEnabled()) {
            showGPSDisabledDialog()
            return
        }

        try {
            // ===== ENABLE MAP LOCATION LAYER =====
            // Shows blue dot for user's location on the map
            map.isMyLocationEnabled = true

            // ===== START LOCATION UPDATES =====
            startLocationUpdates()

            // ===== GET LAST KNOWN LOCATION =====
            // Provides immediate positioning without waiting for GPS
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    // Create LatLng from location coordinates
                    val userLocation = LatLng(it.latitude, it.longitude)

                    // Animate camera to user's position with zoom level 15
                    // Zoom level 15 is good for street-level navigation
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }.addOnFailureListener {
                // Failed to get last location
                Toast.makeText(
                    requireContext(),
                    "Impossible d'obtenir votre position",  // Unable to get your position
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: SecurityException) {
            // Permission was revoked after check (rare edge case)
            Toast.makeText(requireContext(), "Erreur de permission: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Starts requesting continuous location updates.
     *
     * Location Request Configuration:
     * - Interval: 2000ms (2 seconds) - Normal update frequency
     * - Fastest Interval: 1000ms (1 second) - Minimum time between updates
     * - Priority: HIGH_ACCURACY - Uses GPS for best accuracy
     *
     * Update Flow:
     * 1. Check if GPS is enabled
     * 2. Create location request with desired parameters
     * 3. Verify permission (safety check)
     * 4. Start location updates on main thread looper
     * 5. Updates delivered to locationCallback
     *
     * Priority Explanation:
     * - PRIORITY_HIGH_ACCURACY: Uses GPS, most accurate but higher battery usage
     * - Suitable for real-time tracking during rides
     *
     * Error Handling:
     * - Shows GPS dialog if disabled
     * - Catches SecurityException if permission revoked
     */
    private fun startLocationUpdates() {
        // ===== GPS CHECK =====
        // Ensure GPS is enabled before starting updates
        if (!isLocationEnabled()) {
            showGPSDisabledDialog()
            return
        }

        // ===== CREATE LOCATION REQUEST =====
        val locationRequest = LocationRequest.create().apply {
            interval = 2000                                    // Update every 2 seconds
            fastestInterval = 1000                             // No faster than 1 second
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // Use GPS
        }

        // ===== PERMISSION CHECK =====
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            // ===== START LOCATION UPDATES =====
            // Updates will be delivered to locationCallback on the main thread
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()  // Receive updates on main thread
            )
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Updates the driver's position marker on the map.
     *
     * Marker Behavior:
     * - First call: Creates new marker with title "Ma Position" (My Position)
     * - First call: Animates camera to position
     * - Subsequent calls: Updates existing marker position
     * - Subsequent calls: No camera animation (to avoid jerky movement)
     *
     * This method is called by locationCallback whenever new location data arrives.
     *
     * Camera Animation:
     * - Only animates on first location to center the map
     * - After that, marker moves but camera stays where user positioned it
     * - Prevents annoying automatic camera movements during manual map exploration
     *
     * @param location The new location data from GPS
     */
    private fun updateLocationOnMap(location: Location) {
        // Convert Location to LatLng for Google Maps API
        val userLocation = LatLng(location.latitude, location.longitude)

        if (driverMarker == null) {
            // ===== FIRST LOCATION - CREATE MARKER =====
            driverMarker = map.addMarker(
                MarkerOptions()
                    .position(userLocation)
                    .title("Ma Position")  // My Position
            )

            // Animate camera to marker position on first location
            // Zoom level 15 provides good street-level detail
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        } else {
            // ===== UPDATE EXISTING MARKER =====
            // Simply update marker position without camera animation
            driverMarker?.position = userLocation
        }
    }

    // ============================================
    // LIFECYCLE - PAUSE/RESUME/DESTROY
    // ============================================

    /**
     * Called when the fragment is no longer visible to the user.
     *
     * Stops location updates to:
     * - Save battery when map is not visible
     * - Prevent unnecessary GPS usage
     * - Reduce background processing
     *
     * Location updates will restart in onResume() when fragment becomes visible again.
     */
    override fun onPause() {
        super.onPause()
        // Stop location updates when fragment is not visible
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Called when the fragment becomes visible to the user again.
     *
     * Resume Flow:
     * 1. Check if location permission is still granted
     * 2. Check if GPS is still enabled
     * 3. If both conditions met: Resume location updates
     * 4. If GPS disabled: Show dialog to enable it
     *
     * This ensures that location tracking automatically resumes when:
     * - User switches back to this fragment from another tab
     * - User returns to app from background
     *
     * GPS Status Check:
     * - User might have disabled GPS while app was paused
     * - Prompt to re-enable if disabled
     */
    override fun onResume() {
        super.onResume()

        // ===== CHECK PERMISSION =====
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // ===== CHECK GPS STATUS =====
            if (isLocationEnabled()) {
                // Permission granted and GPS enabled - resume updates
                if (::map.isInitialized) {
                    startLocationUpdates()
                }
            } else {
                // GPS was turned off while paused - prompt to enable
                showGPSDisabledDialog()
            }
        }
    }

    /**
     * Called when the fragment's view is being destroyed.
     *
     * Cleanup Actions:
     * - Removes location update callback
     * - Releases location resources
     * - Prevents memory leaks
     *
     * This is the final cleanup when fragment is permanently destroyed,
     * not just hidden (unlike onPause which is temporary).
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up location updates to prevent memory leaks
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}