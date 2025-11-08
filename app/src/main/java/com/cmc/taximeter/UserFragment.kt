package com.cmc.taximeter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class UserFragment : Fragment() {

    // View declarations
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userAge: TextView
    private lateinit var userPermis: TextView
    private lateinit var userQRCodeImageView: ImageView
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private var currentUserEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        // Set status bar to black
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Initialize views
        initializeViews(view)

        // Get user email from arguments
        currentUserEmail = arguments?.getString("userEmail") ?: ""

        // Load and display user data
        if (currentUserEmail.isNotEmpty()) {
            loadUserData()
        } else {
            showError("Erreur: Utilisateur non trouvé")
        }

        // Setup button listeners
        setupListeners()

        return view
    }

    private fun initializeViews(view: View) {
        userName = view.findViewById(R.id.userName)
        userEmail = view.findViewById(R.id.userEmail)
        userAge = view.findViewById(R.id.userAge)
        userPermis = view.findViewById(R.id.userPermis)
        userQRCodeImageView = view.findViewById(R.id.userQRCodeImageView)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun loadUserData() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // Get user data from SharedPreferences
        val name = sharedPreferences.getString("${currentUserEmail}_name", "Nom Inconnu") ?: "Nom Inconnu"
        val age = sharedPreferences.getString("${currentUserEmail}_age", "N/A") ?: "N/A"
        val licenseType = sharedPreferences.getString("${currentUserEmail}_licenseType", "Non spécifié") ?: "Non spécifié"

        // Display user information
        userName.text = name
        userEmail.text = currentUserEmail
        userAge.text = "$age ans"
        userPermis.text = licenseType

        // Generate and display QR code
        generateAndDisplayQRCode(name, age, licenseType)
    }

    private fun generateAndDisplayQRCode(name: String, age: String, licenseType: String) {
        try {
            val qrData = """
                Chauffeur de Taxi
                ==================
                Nom: $name
                Âge: $age ans
                Permis: $licenseType
                Email: $currentUserEmail
            """.trimIndent()

            val qrBitmap = generateQRCode(qrData)
            userQRCodeImageView.setImageBitmap(qrBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Erreur lors de la génération du QR code", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(WriterException::class)
    private fun generateQRCode(text: String): Bitmap {
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 500, 500)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        // Fill bitmap with QR code pixels
        for (x in 0 until width) {
            for (y in 0 until height) {
                // Black for QR code, white for background
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bitmap
    }

    private fun setupListeners() {
        // Edit button - for future implementation
        btnEdit.setOnClickListener {
            Toast.makeText(requireContext(), "Fonctionnalité de modification bientôt disponible", Toast.LENGTH_SHORT).show()
            // TODO: Implement edit profile functionality
        }

        // Logout button with confirmation dialog
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Oui") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun performLogout() {
        // Clear logged in user from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("loggedInUser").apply()

        // Show success message
        Toast.makeText(requireContext(), "Déconnexion réussie", Toast.LENGTH_SHORT).show()

        // Navigate to Authentication Activity
        val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        userName.text = "Erreur"
        userEmail.text = message
        userAge.text = "N/A"
        userPermis.text = "N/A"
    }
}