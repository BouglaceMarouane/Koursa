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

// The fare only increases every 60 seconds
class MeterFragment : Fragment(R.layout.fragment_meter) {

    private lateinit var tvDistance: TextView
    private lateinit var tvTempsEcoule: TextView
    private lateinit var tvTotalAPayer: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnReset: Button
    private lateinit var btnHistory: ImageButton

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // State variables
    private var rideStartTime: Long = 0
    private var lastLocation: Location? = null
    private var accumulatedDistance: Double = 0.0
    private var isRideActive: Boolean = false
    private var timerJob: Job? = null

    // EXACT TARIFS FROM CAHIER DE CHARGE
    private val BASE_FARE: Double = 2.50
    private val PRICE_PER_KM: Double = 1.50
    private val PRICE_PER_MINUTE: Double = 0.50

    // Location tracking configuration
    private val MIN_DISTANCE_CHANGE: Float = 5f
    private val MIN_SPEED_THRESHOLD: Float = 0.5f
    private val MAX_ACCURACY: Float = 20f

    private val PERMISSION_LOCATION_CODE = 123
    private val CHANNEL_ID = "ride_channel"
    private val NOTIFICATION_ID = 1

    private val rideHistory = mutableListOf<RideRecord>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        initializeViews(view)
        setupLocationClient()
        setupClickListeners()
        loadHistory()
    }

    private fun initializeViews(view: View) {
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

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (isRideActive) {
                        updateLocation(location)
                    }
                }
            }
        }
    }

    private fun updateLocation(newLocation: Location) {
        // First location - just store it
        if (lastLocation == null) {
            lastLocation = newLocation
            return
        }

        // Check if moving (speed threshold)
        if (newLocation.speed < MIN_SPEED_THRESHOLD) {
            return
        }

        // Check GPS accuracy
        if (newLocation.accuracy > MAX_ACCURACY) {
            return
        }

        // Calculate distance from last location
        val distance = lastLocation!!.distanceTo(newLocation).toDouble()

        // Only count significant movements
        if (distance >= MIN_DISTANCE_CHANGE) {
            accumulatedDistance += distance / 1000.0 // Convert to km
            lastLocation = newLocation
        }
    }

    private fun setupClickListeners() {
        btnStartStop.setOnClickListener {
            if (isRideActive) {
                stopRide()
            } else {
                if (checkPermissionsAndLocation()) {
                    startRide()
                }
            }
        }

        btnReset.setOnClickListener {
            resetRide()
        }

        btnHistory.setOnClickListener {
            showHistoryDialog()
        }
    }

    private fun checkPermissionsAndLocation(): Boolean {
        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, PERMISSION_LOCATION_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    .build()
            )
            return false
        }

        if (!isLocationEnabled()) {
            showEnableLocationDialog()
            return false
        }

        return true
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Localisation désactivée")
            .setMessage("Veuillez activer la localisation pour utiliser le compteur")
            .setPositiveButton("Activer") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun startRide() {
        isRideActive = true
        rideStartTime = System.currentTimeMillis()
        accumulatedDistance = 0.0
        lastLocation = null

        btnStartStop.text = "STOP"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.rouge)
        btnReset.isEnabled = false

        startLocationUpdates()
        startTimer()

        Toast.makeText(requireContext(), "Course démarrée", Toast.LENGTH_SHORT).show()
    }

    private fun stopRide() {
        isRideActive = false

        btnStartStop.text = "START"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.jauneTaxi)
        btnReset.isEnabled = true

        stopLocationUpdates()
        stopTimer()

        saveToHistory()
        showRideCompletionNotification()

        Toast.makeText(requireContext(), "Course terminée", Toast.LENGTH_SHORT).show()
    }

    private fun resetRide() {
        if (isRideActive) {
            AlertDialog.Builder(requireContext())
                .setTitle("Réinitialiser")
                .setMessage("Voulez-vous vraiment arrêter et réinitialiser la course ?")
                .setPositiveButton("Oui") { _, _ ->
                    doReset()
                }
                .setNegativeButton("Non", null)
                .show()
        } else {
            doReset()
        }
    }

    private fun doReset() {
        isRideActive = false
        stopLocationUpdates()
        stopTimer()

        accumulatedDistance = 0.0
        rideStartTime = 0
        lastLocation = null

        tvDistance.text = "0.00"
        tvTempsEcoule.text = "00:00"
        tvTotalAPayer.text = String.format("%.2f", BASE_FARE)

        btnStartStop.text = "START"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.jauneTaxi)
        btnReset.isEnabled = true

        Toast.makeText(requireContext(), "Compteur réinitialisé", Toast.LENGTH_SHORT).show()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000L
            fastestInterval = 500L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = MIN_DISTANCE_CHANGE
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Erreur de permission", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isRideActive) {
                updateDisplay()
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private fun updateDisplay() {
        // Calculate elapsed time in seconds
        val elapsedTimeMs = System.currentTimeMillis() - rideStartTime
        val totalSeconds = elapsedTimeMs / 1000

        // Format time as MM:SS for display
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        // ⭐ KEY FIX: Only charge for COMPLETE minutes (floor function)
        val completeMinutes = floor(totalSeconds / 60.0).toInt()

        // Calculate fare: Base + (Distance × PricePerKm) + (CompleteMinutes × PricePerMinute)
        val distanceFare = accumulatedDistance * PRICE_PER_KM
        val timeFare = completeMinutes * PRICE_PER_MINUTE
        val totalFare = BASE_FARE + distanceFare + timeFare

        // Update UI
        tvDistance.text = String.format("%.2f", accumulatedDistance)
        tvTempsEcoule.text = String.format("%02d:%02d", minutes, seconds)
        tvTotalAPayer.text = String.format("%.2f", totalFare)
    }

    private fun calculateFinalFare(totalSeconds: Long): FareBreakdown {
        val completeMinutes = floor(totalSeconds / 60.0).toInt()

        val baseFare = BASE_FARE
        val distanceFare = accumulatedDistance * PRICE_PER_KM
        val timeFare = completeMinutes * PRICE_PER_MINUTE
        val total = baseFare + distanceFare + timeFare

        return FareBreakdown(baseFare, distanceFare, timeFare, total, completeMinutes)
    }

    private fun saveToHistory() {
        val elapsedTimeMs = System.currentTimeMillis() - rideStartTime
        val totalSeconds = elapsedTimeMs / 1000
        val fare = calculateFinalFare(totalSeconds)

        val record = RideRecord(
            date = System.currentTimeMillis(),
            distance = accumulatedDistance.toFloat(),
            time = fare.completeMinutes.toFloat(),
            fare = fare.total.toFloat()
        )

        rideHistory.add(0, record)
        saveHistoryToPreferences()
    }

    private fun saveHistoryToPreferences() {
        val prefs = requireContext().getSharedPreferences("RideHistory", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val historyToSave = rideHistory.take(20)
        editor.putInt("history_count", historyToSave.size)

        historyToSave.forEachIndexed { index, record ->
            editor.putLong("date_$index", record.date)
            editor.putFloat("distance_$index", record.distance)
            editor.putFloat("time_$index", record.time)
            editor.putFloat("fare_$index", record.fare)
        }

        editor.apply()
    }

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

    private fun showHistoryDialog() {
        if (rideHistory.isEmpty()) {
            Toast.makeText(requireContext(), "Aucune course dans l'historique", Toast.LENGTH_SHORT).show()
            return
        }

        val historyText = buildString {
            append("📊 HISTORIQUE DES COURSES\n\n")
            rideHistory.forEachIndexed { index, record ->
                append("Course ${index + 1}\n")
                append("Date: ${formatDate(record.date)}\n")
                append("Distance: %.2f km\n".format(record.distance))
                append("Temps: %.0f min\n".format(record.time))
                append("Tarif: %.2f DH\n\n".format(record.fare))
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Historique")
            .setMessage(historyText)
            .setPositiveButton("OK", null)
            .setNeutralButton("Effacer") { _, _ ->
                clearHistory()
            }
            .show()
    }

    private fun clearHistory() {
        AlertDialog.Builder(requireContext())
            .setTitle("Effacer l'historique")
            .setMessage("Voulez-vous vraiment effacer tout l'historique ?")
            .setPositiveButton("Oui") { _, _ ->
                rideHistory.clear()
                val prefs = requireContext().getSharedPreferences("RideHistory", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
                Toast.makeText(requireContext(), "Historique effacé", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun showRideCompletionNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifications de course",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val elapsedTimeMs = System.currentTimeMillis() - rideStartTime
        val totalSeconds = elapsedTimeMs / 1000
        val fare = calculateFinalFare(totalSeconds)

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.taxi)
            .setContentTitle("Course terminée ✓")
            .setContentText("%.2f km • %d min • %.2f DH".format(
                accumulatedDistance, fare.completeMinutes, fare.total))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Distance: %.2f km\nTemps: %d minutes\n\nBase: %.2f DH\nDistance: %.2f DH\nTemps: %.2f DH\n\nTOTAL: %.2f DH".format(
                    accumulatedDistance, fare.completeMinutes,
                    fare.base, fare.distance, fare.time, fare.total)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (checkPermissionsAndLocation()) {
                startRide()
            }
        } else {
            Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isRideActive) {
            stopLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isRideActive && checkPermissionsAndLocation()) {
            startLocationUpdates()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationUpdates()
        stopTimer()
    }

    data class RideRecord(
        val date: Long,
        val distance: Float,
        val time: Float,
        val fare: Float
    )

    data class FareBreakdown(
        val base: Double,
        val distance: Double,
        val time: Double,
        val total: Double,
        val completeMinutes: Int
    )
}