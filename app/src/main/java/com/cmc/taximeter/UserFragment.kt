package com.cmc.taximeter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import androidx.core.content.ContextCompat
import kotlin.text.get


class UserFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userAgeTextView: TextView
    private lateinit var userLicenseTextView: TextView
    private lateinit var userQRCodeImageView: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_user, container, false)

        // Set status bar to black
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Initialisation des vues
        userNameTextView = binding.findViewById(R.id.userName)
        userAgeTextView = binding.findViewById(R.id.userAge)
        userLicenseTextView = binding.findViewById(R.id.userPermis)
        userQRCodeImageView = binding.findViewById(R.id.userQRCodeImageView)

        // Récupérer l'email de l'utilisateur (passé via arguments)
        val email = arguments?.getString("userEmail") ?: ""

        if (email.isNotEmpty()) {
            // Accéder aux informations de l'utilisateur à partir de SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

            // Récupérer les informations de l'utilisateur depuis SharedPreferences
            val name = sharedPreferences.getString(email + "_name", "Nom Inconnu") ?: "Nom Inconnu"
            val age = sharedPreferences.getString(email + "_age", "Age Inconnu") ?: "Age Inconnu"
            val licenseType = sharedPreferences.getString(email + "_licenseType", "Permis Inconnu") ?: "Permis Inconnu"

            // Afficher les informations dans les TextViews
            userNameTextView.text = name
            userAgeTextView.text = "$age ans"
            userLicenseTextView.text = licenseType



            val textToEncode = "Nom : $name \nAge : $age ans  \nType de permis : $licenseType"


            // Générer le QR code
            try {
                val bitmap = generateQRCode(textToEncode)
                userQRCodeImageView.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                e.printStackTrace()
            }

        }


        return binding
    }
    @Throws(WriterException::class)
    fun generateQRCode(text: String): Bitmap {
        val writer = MultiFormatWriter()
        // Générer un QR code avec les paramètres appropriés
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 500, 500)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        // Remplir le bitmap avec les pixels du QR code
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0x000000.toInt() else 0xD3D3D3.toInt()) // Noir ou blanc
            }
        }
        return bitmap
    }
}
