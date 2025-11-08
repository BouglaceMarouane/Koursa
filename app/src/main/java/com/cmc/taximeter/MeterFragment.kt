package com.cmc.taximeter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class MeterFragment : Fragment(R.layout.fragment_meter) {

    private lateinit var tvDistance: TextView
    private lateinit var tvTempsEcoule: TextView
    private lateinit var tvTotalAPayer: TextView
    private lateinit var btnDemarrerTaxi: Button
    private lateinit var btnReset: Button
    private lateinit var btnStop: Button

    private lateinit var clientLocalisation: FusedLocationProviderClient

    private var heureDebut: Long = 0
    private var localisationActuelle: Location? = null
    private var distanceTotale: Float = 0f
    private var courseEnCours: Boolean = false
    private var tarifDeBase: Float = 2.5f
    private var prixParKm: Float = 2.0f

    private val PERMISSION_LOCALISATION = 123
    private val CHANNEL_ID = "course_channel"
    private val NOTIFICATION_ID = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set status bar to black
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Initialisation des vues
        tvDistance = view.findViewById(R.id.tvDistance)
        tvTempsEcoule = view.findViewById(R.id.tvTempsEcoule)
        tvTotalAPayer = view.findViewById(R.id.tvTotalAPayer)
        btnDemarrerTaxi = view.findViewById(R.id.btnDemarrerTaxi)
        btnReset = view.findViewById(R.id.btnReset)
        btnStop = view.findViewById(R.id.btnStop)

        // Initialisation du client de localisation
        clientLocalisation = LocationServices.getFusedLocationProviderClient(requireContext())

        // Demarrer la course
        btnDemarrerTaxi.setOnClickListener {
            if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                demarrerCourse()
            } else {
                EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(this, PERMISSION_LOCALISATION, Manifest.permission.ACCESS_FINE_LOCATION)
                        .build()
                )
            }
        }

        // Réinitialiser la course
        btnReset.setOnClickListener {
            resetCourse()
        }

        // Arrêter la course
        btnStop.setOnClickListener {
            stopCourse()
        }
    }

    private fun demarrerCourse() {
        courseEnCours = true
        heureDebut = SystemClock.elapsedRealtime()

        btnDemarrerTaxi.isEnabled = false
        btnDemarrerTaxi.isClickable = false

        // Lancer la coroutine pour mettre à jour la distance et le temps en temps réel
        CoroutineScope(Dispatchers.Main).launch {
            while (courseEnCours) {
                val localisation = obtenirLocalisation()
                localisation?.let {
                    // Calcul du temps écoulé
                    val tempsEcouleSec = (SystemClock.elapsedRealtime() - heureDebut) / 1000
                    val minutes = tempsEcouleSec / 60
                    val secondes = tempsEcouleSec % 60
                    val tempsFormatte = String.format("%02d:%02d", minutes, secondes)

                    // Mise à jour de la distance et du montant total
                    tvDistance.text = "%.2f".format(distanceTotale / 1000)
                    tvTempsEcoule.text = tempsFormatte
                    tvTotalAPayer.text = "%.2f".format(calculerMontantTotal(distanceTotale))

                    delay(1000)  // Met à jour toutes les secondes
                }
            }
        }
    }

    private fun stopCourse() {
        btnDemarrerTaxi.isEnabled = true  // Réactive le bouton
        btnDemarrerTaxi.isClickable = true

        courseEnCours = false
        btnDemarrerTaxi.text = "DÉMARRER"  // Remet le texte du bouton

        // Afficher un message ou un toast indiquant que la course est terminée
        Toast.makeText(requireContext(), "Course terminée", Toast.LENGTH_SHORT).show()

        // Appeler la méthode pour afficher la notification de fin de course
        afficherNotificationFinCourse()
    }

    private fun resetCourse() {
        btnDemarrerTaxi.isEnabled = true  // Réactive le bouton
        btnDemarrerTaxi.isClickable = true

        courseEnCours = false
        distanceTotale = 0f
        tvDistance.text = "0.00"
        tvTempsEcoule.text = "00:00"
        tvTotalAPayer.text = "0.00"
        btnDemarrerTaxi.text = "DÉMARRER"
        // Réinitialiser tout et afficher un message
        Toast.makeText(requireContext(), "Course réinitialisée", Toast.LENGTH_SHORT).show()
    }

    private suspend fun obtenirLocalisation(): Location? {
        return withContext(Dispatchers.IO) @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) {
            try {
                val resultatLocalisation: Task<Location> = clientLocalisation.lastLocation
                val localisation = Tasks.await(resultatLocalisation)
                if (localisation != null) {
                    if (localisationActuelle != null) {
                        distanceTotale += localisation.distanceTo(localisationActuelle!!)
                    }
                    localisationActuelle = localisation
                }
                localisationActuelle
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun calculerMontantTotal(distance: Float): Float {
        val distanceEnKm = distance / 1000
        return tarifDeBase + (distanceEnKm * prixParKm)
    }

    private fun afficherNotificationFinCourse() {
        // Créer un canal de notification (nécessaire pour Android O et plus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifications de course",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Canal pour les notifications de fin de course"
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Création de la notification
        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.map)  // Remplacez par votre icône de notification
            .setContentTitle("Course terminée")
            .setContentText("Distance : %.2f km\nTarif total : %.2f Dhs".format(distanceTotale / 1000, calculerMontantTotal(distanceTotale)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Afficher la notification
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            demarrerCourse()
        } else {
            Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }
}