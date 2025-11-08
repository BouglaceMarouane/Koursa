package com.cmc.taximeter

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
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
import android.os.Looper
import android.content.Intent
import android.provider.Settings

class MeterFragment : Fragment(R.layout.fragment_meter) {

    private lateinit var tvDistance: TextView
    private lateinit var tvTempsEcoule: TextView
    private lateinit var tvTotalAPayer: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnReset: Button
    private lateinit var btnHistory: ImageButton

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var previousLocation: Location? = null
    private var totalDistance: Float = 0f
    private var isRideActive: Boolean = false
    private var updateJob: Job? = null

    // Tarifs selon le cahier de charge
    private val BASE_FARE: Float = 2.5f        // Tarif de base en DH
    private val PRICE_PER_KM: Float = 1.5f     // Prix par kilomètre en DH
    private val PRICE_PER_MINUTE: Float = 0.5f // Prix par minute en DH

    private val PERMISSION_LOCATION_CODE = 123
    private val CHANNEL_ID = "ride_channel"
    private val NOTIFICATION_ID = 1

    // Liste pour l'historique
    private val rideHistory = mutableListOf<RideRecord>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set status bar to black
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

        // Initialiser l'affichage
        updateDisplay()
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { newLocation ->
                    if (isRideActive && previousLocation != null) {
                        // Calculer la distance seulement si on bouge (plus de 5 mètres)
                        val distance = previousLocation!!.distanceTo(newLocation)
                        if (distance > 5f) { // Filtre pour éviter les petites variations GPS
                            totalDistance += distance
                        }
                    }
                    previousLocation = newLocation
                }
            }
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
            if (isRideActive) {
                showResetConfirmationDialog()
            } else {
                resetRide()
            }
        }

        btnHistory.setOnClickListener {
            showHistoryDialog()
        }
    }

    private fun checkPermissionsAndLocation(): Boolean {
        // Vérifier les permissions
        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, PERMISSION_LOCATION_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    .build()
            )
            return false
        }

        // Vérifier si le GPS est activé
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
        startTime = SystemClock.elapsedRealtime() - pausedTime
        previousLocation = null

        btnStartStop.text = "STOP"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.rouge)
        btnReset.isEnabled = false

        startLocationUpdates()
        startTimeUpdates()

        Toast.makeText(requireContext(), "Course démarrée", Toast.LENGTH_SHORT).show()
    }

    private fun stopRide() {
        isRideActive = false
        pausedTime = SystemClock.elapsedRealtime() - startTime

        btnStartStop.text = "DÉMARRER"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.jauneTaxi)
        btnReset.isEnabled = true

        stopLocationUpdates()
        stopTimeUpdates()

        // Sauvegarder dans l'historique
        saveToHistory()

        // Afficher la notification
        showRideCompletionNotification()

        Toast.makeText(requireContext(), "Course terminée", Toast.LENGTH_SHORT).show()
    }

    private fun resetRide() {
        totalDistance = 0f
        startTime = 0
        pausedTime = 0
        previousLocation = null

        updateDisplay()

        btnStartStop.text = "DÉMARRER"
        btnStartStop.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.jauneTaxi)
        btnReset.isEnabled = true

        Toast.makeText(requireContext(), "Compteur réinitialisé", Toast.LENGTH_SHORT).show()
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Réinitialiser")
            .setMessage("Êtes-vous sûr de vouloir arrêter et réinitialiser la course ?")
            .setPositiveButton("Oui") { _, _ ->
                stopRide()
                resetRide()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // 1 seconde
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 5f // Mise à jour tous les 5 mètres minimum
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

    private fun startTimeUpdates() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isRideActive) {
                updateDisplay()
                delay(1000) // Mise à jour chaque seconde
            }
        }
    }

    private fun stopTimeUpdates() {
        updateJob?.cancel()
    }

    private fun updateDisplay() {
        // Calcul du temps écoulé
        val elapsedTimeMillis = if (isRideActive) {
            SystemClock.elapsedRealtime() - startTime
        } else {
            pausedTime
        }
        val totalSeconds = elapsedTimeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        // Calcul du tarif total
        val distanceInKm = totalDistance / 1000f
        val timeInMinutes = totalSeconds / 60f
        val totalFare = BASE_FARE + (distanceInKm * PRICE_PER_KM) + (timeInMinutes * PRICE_PER_MINUTE)

        // Mise à jour de l'affichage
        tvDistance.text = String.format("%.2f", distanceInKm)
        tvTempsEcoule.text = String.format("%02d:%02d", minutes, seconds)
        tvTotalAPayer.text = String.format("%.2f", totalFare)
    }

    private fun calculateTotalFare(): Float {
        val distanceInKm = totalDistance / 1000f
        val elapsedTimeMillis = if (isRideActive) {
            SystemClock.elapsedRealtime() - startTime
        } else {
            pausedTime
        }
        val timeInMinutes = (elapsedTimeMillis / 1000f) / 60f
        return BASE_FARE + (distanceInKm * PRICE_PER_KM) + (timeInMinutes * PRICE_PER_MINUTE)
    }

    private fun saveToHistory() {
        val distanceInKm = totalDistance / 1000f
        val timeInMinutes = (pausedTime / 1000f) / 60f
        val fare = calculateTotalFare()

        val record = RideRecord(
            date = System.currentTimeMillis(),
            distance = distanceInKm,
            time = timeInMinutes,
            fare = fare
        )

        rideHistory.add(0, record) // Ajouter au début de la liste

        // Sauvegarder dans SharedPreferences
        saveHistoryToPreferences()
    }

    private fun saveHistoryToPreferences() {
        val prefs = requireContext().getSharedPreferences("RideHistory", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Sauvegarder les 20 dernières courses
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
                append("Temps: %.1f min\n".format(record.time))
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
        // Créer le canal de notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifications de course",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val distanceInKm = totalDistance / 1000f
        val fare = calculateTotalFare()
        val timeInMinutes = (pausedTime / 1000f) / 60f

        // Créer la notification
        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.taxi)
            .setContentTitle("Course terminée ✓")
            .setContentText("%.2f km • %.1f min • %.2f DH".format(distanceInKm, timeInMinutes, fare))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Distance: %.2f km\nTemps: %.1f min\nTarif total: %.2f DH".format(
                    distanceInKm, timeInMinutes, fare)))
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
        stopTimeUpdates()
    }

    // Classe pour stocker les données d'une course
    data class RideRecord(
        val date: Long,
        val distance: Float,
        val time: Float,
        val fare: Float
    )
}