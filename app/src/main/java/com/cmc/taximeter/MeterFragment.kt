package com.cmc.taximeter

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import kotlin.math.floor

/**
 * MeterFragment
 *
 * Core taximeter functionality - calculates fare based on distance and time.
 *
 * Key Features:
 * - Real-time distance tracking using GPS
 * - Time-based fare calculation (charged per complete minute)
 * - Live fare display with breakdown (base + distance + time)
 * - Ride history storage and display
 * - Start/Stop/Reset ride controls
 * - Completion notifications with fare breakdown
 * - Smart location filtering (accuracy, speed thresholds)
 *
 * Fare Calculation (from Cahier de Charge):
 * - Base fare: 2.50 DH
 * - Distance: 1.50 DH per km
 * - Time: 0.50 DH per COMPLETE minute (NOT partial minutes)
 *
 * Technical Implementation:
 * - Uses FusedLocationProviderClient for GPS tracking
 * - Coroutines for timer updates (1 second intervals)
 * - SharedPreferences for ride history persistence (max 20 rides)
 * - Quality filters: min speed 0.5 m/s, max accuracy 20m, min distance 5m
 *
 * Important Note:
 * The fare only increases every 60 seconds (complete minutes), not continuously.
 * This matches real taxi meter behavior.
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class MeterFragment : Fragment(R.layout.fragment_meter) {

    // ============================================
    // VIEW PROPERTIES
    // ============================================

    /** Displays distance traveled in km */
    private lateinit var tvDistance: TextView

    /** Displays elapsed time in MM:SS format */
    private lateinit var tvTempsEcoule: TextView

    /** Displays current total fare to pay */
    private lateinit var tvTotalAPayer: TextView

    /** Button to start/stop the ride (toggles between START and STOP) */
    private lateinit var btnStartStop: Button

    /** Button to reset the meter to zero */
    private lateinit var btnReset: Button

    /** Button to view ride history */
    private lateinit var btnHistory: ImageButton

    // ============================================
    // LOCATION TRACKING
    // ============================================

    /** Client for accessing location services efficiently */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /** Callback that receives location updates from GPS */
    private lateinit var locationCallback: LocationCallback

    // ============================================
    // RIDE STATE VARIABLES
    // ============================================

    /** Timestamp when the current ride started (milliseconds) */
    private var rideStartTime: Long = 0

    /** Last recorded GPS location (used to calculate distance between updates) */
    private var lastLocation: Location? = null

    /** Total distance traveled in kilometers */
    private var accumulatedDistance: Double = 0.0

    /** Whether a ride is currently in progress */
    private var isRideActive: Boolean = false

    /** Coroutine job for the timer (updates display every second) */
    private var timerJob: Job? = null

    // ============================================
    // TARIFF CONSTANTS (FROM CAHIER DE CHARGE)
    // ============================================

    /** Base fare charged at start of every ride (DH) */
    private val BASE_FARE: Double = 2.50

    /** Price per kilometer traveled (DH/km) */
    private val PRICE_PER_KM: Double = 1.50

    /** Price per COMPLETE minute elapsed (DH/min) */
    private val PRICE_PER_MINUTE: Double = 0.50

    // ============================================
    // LOCATION TRACKING CONFIGURATION
    // ============================================

    /** Minimum distance change to record (meters) - filters GPS noise */
    private val MIN_DISTANCE_CHANGE: Float = 5f

    /** Minimum speed to count as moving (m/s) - filters stopped/slow movement */
    private val MIN_SPEED_THRESHOLD: Float = 0.5f

    /** Maximum acceptable GPS accuracy (meters) - filters inaccurate readings */
    private val MAX_ACCURACY: Float = 20f

    // ============================================
    // CONSTANTS
    // ============================================

    /** Request code for location permission */
    private val PERMISSION_LOCATION_CODE = 123

    /** Notification channel ID for ride completion notifications */
    private val CHANNEL_ID = "ride_channel"

    /** Notification ID for ride completion */
    private val NOTIFICATION_ID = 1

    // ============================================
    // RIDE HISTORY
    // ============================================

    /** List storing previous ride records (max 20, newest first) */
    private val rideHistory = mutableListOf<RideRecord>()

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Called when the fragment's view has been created.
     *
     * Initialization sequence:
     * 1. Sets status bar color
     * 2. Initializes all UI views
     * 3. Sets up location client and callback
     * 4. Sets up button click listeners
     * 5. Loads ride history from SharedPreferences
     *
     * @param view The root view of the fragment
     * @param savedInstanceState Previously saved state (if any)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set status bar to black for consistent UI
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Initialize all components in sequence
        initializeViews(view)
        setupLocationClient()
        setupClickListeners()
        loadHistory()
    }

    /**
     * Called when fragment becomes visible to the user.
     *
     * Resumes location updates if:
     * - A ride is currently active
     * - Permissions are granted
     * - GPS is enabled
     *
     * This ensures the meter continues working when user returns to the fragment.
     */
    override fun onResume() {
        super.onResume()

        // Only resume updates if a ride is in progress
        if (isRideActive && checkPermissionsAndLocation()) {
            startLocationUpdates()
        }
    }

    /**
     * Called when fragment is no longer visible to the user.
     *
     * Stops location updates ONLY if no ride is active.
     * This conserves battery while allowing ongoing rides to continue tracking.
     */
    override fun onPause() {
        super.onPause()

        // Only stop updates if no active ride
        // If ride is active, we keep tracking even when fragment is not visible
        if (!isRideActive) {
            stopLocationUpdates()
        }
    }

    /**
     * Called when the fragment's view is being destroyed.
     *
     * Performs cleanup:
     * - Stops location updates
     * - Cancels timer coroutine
     *
     * Prevents memory leaks and battery drain.
     */
    override fun onDestroyView() {
        super.onDestroyView()

        // Clean up resources
        stopLocationUpdates()
        stopTimer()
    }

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes all UI view references and sets default values.
     *
     * Initial display values:
     * - Distance: 0.00 km
     * - Time: 00:00
     * - Total: 2.50 DH (base fare)
     *
     * @param view The root view containing all UI elements
     */
    private fun initializeViews(view: View) {
        // Find all view references
        tvDistance = view.findViewById(R.id.tvDistance)
        tvTempsEcoule = view.findViewById(R.id.tvTempsEcoule)
        tvTotalAPayer = view.findViewById(R.id.tvTotalAPayer)
        btnStartStop = view.findViewById(R.id.btnDemarrerTaxi)
        btnReset = view.findViewById(R.id.btnReset)
        btnHistory = view.findViewById(R.id.btnHistory)

        // Initialize display with zeros
        tvDistance.text = "0.00"
        tvTempsEcoule.text = "00:00"
        tvTotalAPayer.text = String.format("%.2f", BASE_FARE)
    }

    /**
     * Sets up the location client and callback for GPS tracking.
     *
     * LocationCallback behavior:
     * - Receives location updates from FusedLocationProviderClient
     * - Only processes updates when ride is active
     * - Passes each location to updateLocation() for processing
     */
    private fun setupLocationClient() {
        // Initialize the fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Create callback to receive location updates
        locationCallback = object : LocationCallback() {
            /**
             * Called when new location data is available.
             *
             * @param locationResult Contains one or more location updates
             */
            override fun onLocationResult(locationResult: LocationResult) {
                // Process each location update
                for (location in locationResult.locations) {
                    // Only update distance if ride is active
                    if (isRideActive) {
                        updateLocation(location)
                    }
                }
            }
        }
    }

    /**
     * Sets up click listeners for all interactive buttons.
     *
     * Button behaviors:
     * - Start/Stop: Toggles ride state
     * - Reset: Clears meter (with confirmation if ride active)
     * - History: Shows dialog with previous rides
     */
    private fun setupClickListeners() {
        // ===== START/STOP BUTTON =====
        btnStartStop.setOnClickListener {
            if (isRideActive) {
                // Currently running - stop the ride
                stopRide()
            } else {
                // Currently stopped - check prerequisites then start
                if (checkPermissionsAndLocation()) {
                    startRide()
                }
            }
        }

        // ===== RESET BUTTON =====
        btnReset.setOnClickListener {
            resetRide()
        }

        // ===== HISTORY BUTTON =====
        btnHistory.setOnClickListener {
            showHistoryDialog()
        }
    }

    // ============================================
    // PERMISSION & GPS CHECKING
    // ============================================

    /**
     * Checks if all prerequisites for tracking are met.
     *
     * Checks:
     * 1. Location permission (FINE and COARSE)
     * 2. GPS/location services enabled
     *
     * @return true if all checks pass, false if any check fails
     *
     * Side effects:
     * - Requests permission if not granted
     * - Shows GPS enable dialog if GPS is off
     */
    private fun checkPermissionsAndLocation(): Boolean {
        // ===== PERMISSION CHECK =====
        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission not granted - request it
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, PERMISSION_LOCATION_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    .build()
            )
            return false
        }

        // ===== GPS CHECK =====
        if (!isLocationEnabled()) {
            // Permission granted but GPS is off
            showEnableLocationDialog()
            return false
        }

        // All checks passed
        return true
    }

    /**
     * Checks if location services (GPS) are enabled on the device.
     *
     * Checks both:
     * - GPS_PROVIDER: Satellite-based (high accuracy)
     * - NETWORK_PROVIDER: Cell/WiFi-based (lower accuracy)
     *
     * @return true if at least one provider is enabled
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Shows dialog prompting user to enable GPS/location services.
     *
     * Dialog options:
     * - "Activer" (Enable): Opens system location settings
     * - "Annuler" (Cancel): Dismisses dialog
     */
    private fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Localisation désactivée")  // Location disabled
            .setMessage("Veuillez activer la localisation pour utiliser le compteur")
            // Please enable location to use the meter
            .setPositiveButton("Activer") { _, _ ->
                // Open location settings
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Annuler", null)  // Cancel
            .show()
    }

    /**
     * Handles permission request result from EasyPermissions.
     *
     * If permission granted:
     * - Checks GPS status
     * - Starts ride if everything is ready
     *
     * If permission denied:
     * - Shows error toast
     *
     * @param requestCode Request code from permission request
     * @param permissions Array of permission strings
     * @param grantResults Array of grant/deny results
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission granted - proceed with ride start
            if (checkPermissionsAndLocation()) {
                startRide()
            }
        } else {
            // Permission denied
            Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================
    // RIDE CONTROL METHODS
    // ============================================

    /**
     * Starts a new ride - initializes tracking and UI.
     *
     * Actions performed:
     * 1. Set ride state to active
     * 2. Record start time
     * 3. Reset distance and location
     * 4. Update button appearance (START → STOP, yellow → red)
     * 5. Disable reset button during ride
     * 6. Start GPS tracking
     * 7. Start timer for display updates
     * 8. Show confirmation toast
     *
     * Prerequisites:
     * - Location permission granted
     * - GPS enabled
     * (checked by caller)
     */
    private fun startRide() {
        // ===== STATE INITIALIZATION =====
        isRideActive = true
        rideStartTime = System.currentTimeMillis()
        accumulatedDistance = 0.0
        lastLocation = null

        // ===== UI UPDATES =====
        btnStartStop.text = "Arrêter"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.rouge)
        btnReset.isEnabled = false  // Prevent reset during ride

        // ===== START TRACKING =====
        startLocationUpdates()  // Begin GPS tracking
        startTimer()            // Begin display updates

        // ===== USER FEEDBACK =====
        Toast.makeText(requireContext(), "Course démarrée", Toast.LENGTH_SHORT).show()
        // Ride started
    }

    /**
     * Stops the current ride - finalizes tracking and updates UI.
     *
     * Actions performed:
     * 1. Set ride state to inactive
     * 2. Update button appearance (STOP → START, red → yellow)
     * 3. Enable reset button
     * 4. Stop GPS tracking
     * 5. Stop timer
     * 6. Save ride to history
     * 7. Show completion notification
     * 8. Show confirmation toast
     */
    private fun stopRide() {
        // ===== STATE UPDATE =====
        isRideActive = false

        // ===== UI UPDATES =====
        btnStartStop.text = "Démarrer"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.jauneTaxi)
        btnReset.isEnabled = true  // Re-enable reset

        // ===== STOP TRACKING =====
        stopLocationUpdates()  // Stop GPS
        stopTimer()            // Stop display updates

        // ===== FINALIZATION =====
        saveToHistory()                     // Save ride record
        showRideCompletionNotification()    // Notify user

        // ===== USER FEEDBACK =====
        Toast.makeText(requireContext(), "Course terminée", Toast.LENGTH_SHORT).show()
        // Ride completed
    }

    /**
     * Resets the meter to initial state.
     *
     * Behavior:
     * - If ride active: Shows confirmation dialog
     * - If ride stopped: Resets immediately
     *
     * This prevents accidental resets during active rides.
     */
    private fun resetRide() {
        if (isRideActive) {
            // ===== ACTIVE RIDE - CONFIRM RESET =====
            AlertDialog.Builder(requireContext())
                .setTitle("initialiser")  // Reset
                .setMessage("Voulez-vous vraiment arrêter et initialiser la course ?")
                // Do you really want to stop and reset the ride?
                .setPositiveButton("Oui") { _, _ ->
                    doReset()
                }
                .setNegativeButton("Non", null)  // No
                .show()
        } else {
            // ===== INACTIVE RIDE - RESET IMMEDIATELY =====
            doReset()
        }
    }

    /**
     * Performs the actual reset operation.
     *
     * Resets:
     * - All state variables to defaults
     * - All display values to zero/base
     * - Button states to initial
     *
     * Does NOT clear history - only clears current ride data.
     */
    private fun doReset() {
        // ===== STOP EVERYTHING =====
        isRideActive = false
        stopLocationUpdates()
        stopTimer()

        // ===== RESET STATE =====
        accumulatedDistance = 0.0
        rideStartTime = 0
        lastLocation = null

        // ===== RESET DISPLAY =====
        tvDistance.text = "0.00"
        tvTempsEcoule.text = "00:00"
        tvTotalAPayer.text = String.format("%.2f", BASE_FARE)

        // ===== RESET BUTTONS =====
        btnStartStop.text = "Démarrer"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.jauneTaxi)
        btnReset.isEnabled = true

        // ===== USER FEEDBACK =====
        Toast.makeText(requireContext(), "Compteur initialisé", Toast.LENGTH_SHORT).show()
        // Meter reset
    }

    // ============================================
    // LOCATION TRACKING
    // ============================================

    /**
     * Starts requesting continuous GPS location updates.
     *
     * Configuration:
     * - Interval: 1000ms (1 second) - normal update frequency
     * - Fastest: 500ms - accept faster updates if available
     * - Priority: HIGH_ACCURACY - use GPS
     * - Smallest displacement: 5m - don't update if moved less than this
     *
     * Updates are received via locationCallback.
     */
    private fun startLocationUpdates() {
        // Create location request with configuration
        val locationRequest = LocationRequest.create().apply {
            interval = 1000L                                    // Update every second
            fastestInterval = 500L                              // Accept up to 2 updates/sec
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY   // Use GPS
            smallestDisplacement = MIN_DISTANCE_CHANGE          // Min 5m movement
        }

        try {
            // Start receiving updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Erreur de permission", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Stops receiving GPS location updates.
     *
     * Conserves battery by stopping GPS when not needed.
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Processes a new GPS location update and calculates distance.
     *
     * Quality filtering applied:
     * 1. First location: Just store, no distance calculation
     * 2. Speed check: Must be moving faster than 0.5 m/s
     * 3. Accuracy check: GPS accuracy must be better than 20m
     * 4. Distance check: Must have moved at least 5m
     *
     * Only locations passing all filters contribute to accumulated distance.
     * This prevents GPS drift and noise from inflating the fare.
     *
     * @param newLocation The new GPS location to process
     */
    private fun updateLocation(newLocation: Location) {
        // ===== FIRST LOCATION =====
        // First location - just store it as reference point
        if (lastLocation == null) {
            lastLocation = newLocation
            return  // Cannot calculate distance without previous location
        }

        // ===== FILTER 1: SPEED CHECK =====
        // Ignore locations where vehicle is stationary or very slow
        // Prevents counting GPS drift while stopped
        if (newLocation.speed < MIN_SPEED_THRESHOLD) {
            return  // Not moving fast enough
        }

        // ===== FILTER 2: ACCURACY CHECK =====
        // Ignore inaccurate GPS readings
        // Prevents bad GPS signals from inflating distance
        if (newLocation.accuracy > MAX_ACCURACY) {
            return  // GPS signal too inaccurate
        }

        // ===== DISTANCE CALCULATION =====
        // Calculate straight-line distance from last location
        val distance = lastLocation!!.distanceTo(newLocation).toDouble()

        // ===== FILTER 3: MINIMUM DISTANCE =====
        // Only count significant movements (5m or more)
        // Filters out GPS jitter and minor position adjustments
        if (distance >= MIN_DISTANCE_CHANGE) {
            // Distance is significant - add to total
            accumulatedDistance += distance / 1000.0  // Convert meters to km
            lastLocation = newLocation  // Update reference point
        }
        // If distance < 5m, ignore and keep previous lastLocation
    }

    // ============================================
    // TIMER AND DISPLAY
    // ============================================

    /**
     * Starts the timer that updates the display every second.
     *
     * Uses Kotlin coroutines for efficient periodic updates.
     * Timer runs on Main/UI thread for safe UI updates.
     *
     * Updates every 1000ms (1 second) while ride is active.
     */
    private fun startTimer() {
        // Cancel any existing timer
        timerJob?.cancel()

        // Start new timer coroutine
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isRideActive) {
                updateDisplay()  // Update UI
                delay(1000)      // Wait 1 second
            }
        }
    }

    /**
     * Stops the timer by canceling the coroutine.
     */
    private fun stopTimer() {
        timerJob?.cancel()
    }

    /**
     * Updates the display with current ride metrics.
     *
     * Called every second by the timer.
     *
     * Calculations:
     * 1. Elapsed time in MM:SS format (for display)
     * 2. Complete minutes elapsed (for fare calculation)
     * 3. Fare breakdown:
     *    - Distance fare = distance × price per km
     *    - Time fare = COMPLETE minutes × price per minute
     *    - Total = base + distance fare + time fare
     *
     * KEY IMPLEMENTATION DETAIL:
     * Uses floor() to only charge for COMPLETE minutes.
     * Example: 89 seconds = 1 minute (not 1.48 minutes)
     * This matches real taxi meter behavior.
     */
    private fun updateDisplay() {
        // ===== TIME CALCULATION =====
        val elapsedTimeMs = System.currentTimeMillis() - rideStartTime
        val totalSeconds = elapsedTimeMs / 1000

        // Format time as MM:SS for display
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        // ⭐ KEY FIX: Only charge for COMPLETE minutes (floor function)
        // This ensures fare only increases every 60 seconds
        val completeMinutes = floor(totalSeconds / 60.0).toInt()

        // ===== FARE CALCULATION =====
        // Calculate each component of the fare
        val distanceFare = accumulatedDistance * PRICE_PER_KM
        val timeFare = completeMinutes * PRICE_PER_MINUTE
        val totalFare = BASE_FARE + distanceFare + timeFare

        // ===== UPDATE UI =====
        tvDistance.text = String.format("%.2f", accumulatedDistance)
        tvTempsEcoule.text = String.format("%02d:%02d", minutes, seconds)
        tvTotalAPayer.text = String.format("%.2f", totalFare)
    }

    /**
     * Calculates the final fare breakdown when ride ends.
     *
     * @param totalSeconds Total elapsed time in seconds
     * @return FareBreakdown object with all fare components
     *
     * Used for:
     * - History storage
     * - Completion notification
     */
    private fun calculateFinalFare(totalSeconds: Long): FareBreakdown {
        // Calculate complete minutes (floor function)
        val completeMinutes = floor(totalSeconds / 60.0).toInt()

        // Calculate each fare component
        val baseFare = BASE_FARE
        val distanceFare = accumulatedDistance * PRICE_PER_KM
        val timeFare = completeMinutes * PRICE_PER_MINUTE
        val total = baseFare + distanceFare + timeFare

        return FareBreakdown(baseFare, distanceFare, timeFare, total, completeMinutes)
    }

    // ============================================
    // RIDE HISTORY
    // ============================================

    /**
     * Saves the completed ride to history.
     *
     * Creates RideRecord with:
     * - Date/time (timestamp)
     * - Distance traveled
     * - Time elapsed (complete minutes)
     * - Total fare
     *
     * Adds to beginning of list (newest first) and persists to SharedPreferences.
     */
    private fun saveToHistory() {
        // Calculate final metrics
        val elapsedTimeMs = System.currentTimeMillis() - rideStartTime
        val totalSeconds = elapsedTimeMs / 1000
        val fare = calculateFinalFare(totalSeconds)

        // Create ride record
        val record = RideRecord(
            date = System.currentTimeMillis(),
            distance = accumulatedDistance.toFloat(),
            time = fare.completeMinutes.toFloat(),
            fare = fare.total.toFloat()
        )

        // Add to history (newest first)
        rideHistory.add(0, record)

        // Persist to storage
        saveHistoryToPreferences()
    }

    /**
     * Saves ride history to SharedPreferences.
     *
     * Storage format:
     * - "history_count": Number of saved rides
     * - "date_N", "distance_N", "time_N", "fare_N": Data for ride N
     *
     * Limits storage to 20 most recent rides to prevent unlimited growth.
     */
    private fun saveHistoryToPreferences() {
        val prefs = requireContext().getSharedPreferences("RideHistory", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Only save the 20 most recent rides
        val historyToSave = rideHistory.take(20)
        editor.putInt("history_count", historyToSave.size)

        // Save each ride's data
        historyToSave.forEachIndexed { index, record ->
            editor.putLong("date_$index", record.date)
            editor.putFloat("distance_$index", record.distance)
            editor.putFloat("time_$index", record.time)
            editor.putFloat("fare_$index", record.fare)
        }

        editor.apply()
    }

    /**
     * Loads ride history from SharedPreferences.
     *
     * Called once when fragment is created.
     * Populates rideHistory list with saved rides.
     */
    private fun loadHistory() {
        val prefs = requireContext().getSharedPreferences("RideHistory", Context.MODE_PRIVATE)
        val count = prefs.getInt("history_count", 0)

        rideHistory.clear()
        for (i in 0 until count) {
            val record = RideRecord(
                date = prefs.getLong("date_$i", 0),
                distance = prefs.getFloat("distance_$i", 0f),
                time = prefs.getFloat("time_$i", 0f),
                fare = prefs.getFloat("fare_$i", 0f)
            )
            rideHistory.add(record)
        }
    }

    /**
     * Shows dialog displaying ride history.
     *
     * Dialog contents:
     * - List of all saved rides (up to 20)
     * - Each ride shows: date, distance, time, fare
     * - "OK" button to close
     * - "Effacer" (Clear) button to delete history
     *
     * If history is empty, shows toast instead.
     */
    private fun showHistoryDialog() {
        // Check if history is empty
        if (rideHistory.isEmpty()) {
            Toast.makeText(requireContext(), "Aucune course dans l'historique", Toast.LENGTH_SHORT).show()
            // No rides in history
            return
        }

        // Build history text
        val historyText = buildString {
            append("📊 HISTORIQUE DES COURSES\n\n")  // RIDE HISTORY
            rideHistory.forEachIndexed { index, record ->
                append("Course ${index + 1}\n")  // Ride N
                append("Date: ${formatDate(record.date)}\n")
                append("Distance: %.2f km\n".format(record.distance))
                append("Temps: %.0f min\n".format(record.time))
                append("Tarif: %.2f DH\n\n".format(record.fare))
            }
        }

        // Show dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Historique")  // History
            .setMessage(historyText)
            .setPositiveButton("OK", null)
            .setNeutralButton("Effacer") { _, _ ->
                // Clear button - show confirmation
                clearHistory()
            }
            .show()
    }

    /**
     * Clears all ride history with confirmation dialog.
     *
     * Shows confirmation before deletion to prevent accidental data loss.
     */
    private fun clearHistory() {
        AlertDialog.Builder(requireContext())
            .setTitle("Effacer l'historique")  // Clear history
            .setMessage("Voulez-vous vraiment effacer tout l'historique ?")
            // Do you really want to clear all history?
            .setPositiveButton("Oui") { _, _ ->
                // Clear memory and storage
                rideHistory.clear()
                val prefs = requireContext().getSharedPreferences("RideHistory", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                Toast.makeText(requireContext(), "Historique effacé", Toast.LENGTH_SHORT).show()
                // History cleared
            }
            .setNegativeButton("Non", null)  // No
            .show()
    }

    /**
     * Formats a timestamp into readable date/time string.
     *
     * Format: DD/MM/YYYY HH:MM
     * Example: 15/03/2024 14:30
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string
     */
    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    // ============================================
    // NOTIFICATIONS
    // ============================================

    /**
     * Shows a notification when ride is completed.
     *
     * Notification includes:
     * - Title: "Course terminée ✓" (Ride completed)
     * - Short text: Distance, time, and total fare
     * - Expanded text: Full fare breakdown
     *
     * Creates notification channel on Android O+ (required).
     *
     * Notification priority: HIGH (shows as heads-up notification)
     * Auto-cancel: true (dismissed when tapped)
     */
    private fun showRideCompletionNotification() {
        // ===== CREATE NOTIFICATION CHANNEL (Android 8.0+) =====
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifications de course",  // Ride notifications
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // ===== CALCULATE FINAL FARE =====
        val elapsedTimeMs = System.currentTimeMillis() - rideStartTime
        val totalSeconds = elapsedTimeMs / 1000
        val fare = calculateFinalFare(totalSeconds)

        // ===== BUILD NOTIFICATION =====
        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.taxi)
            .setContentTitle("Course terminée ✓")  // Ride completed
            .setContentText("%.2f km • %d min • %.2f DH".format(
                accumulatedDistance, fare.completeMinutes, fare.total))
            // BigTextStyle shows expanded view with full breakdown
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Distance: %.2f km\nTemps: %d minutes\n\nBase: %.2f DH\nDistance: %.2f DH\nTemps: %.2f DH\n\nTOTAL: %.2f DH".format(
                    accumulatedDistance, fare.completeMinutes,
                    fare.base, fare.distance, fare.time, fare.total)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)  // Dismiss when tapped
            .build()

        // ===== SHOW NOTIFICATION =====
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // ============================================
    // DATA CLASSES
    // ============================================

    /**
     * Represents a completed ride record.
     *
     * Stored in history and SharedPreferences.
     *
     * @property date Timestamp when ride was completed (milliseconds)
     * @property distance Total distance traveled (km)
     * @property time Total time elapsed (complete minutes)
     * @property fare Total fare charged (DH)
     */
    data class RideRecord(
        val date: Long,
        val distance: Float,
        val time: Float,
        val fare: Float
    )

    /**
     * Breakdown of fare calculation components.
     *
     * Used for displaying detailed fare information.
     *
     * @property base Base fare charged at start (DH)
     * @property distance Fare from distance traveled (DH)
     * @property time Fare from time elapsed (DH)
     * @property total Total fare (base + distance + time) (DH)
     * @property completeMinutes Number of complete minutes elapsed
     */
    data class FareBreakdown(
        val base: Double,
        val distance: Double,
        val time: Double,
        val total: Double,
        val completeMinutes: Int
    )
}