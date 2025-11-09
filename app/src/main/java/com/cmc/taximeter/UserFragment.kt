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

/**
 * UserFragment
 *
 * Displays driver profile information and provides account management.
 *
 * Key Features:
 * - Displays driver information (name, email, age, license type)
 * - Generates and displays QR code with driver details
 * - Edit profile button (future functionality)
 * - Logout functionality with confirmation
 * - Receives user email via fragment arguments
 *
 * QR Code Content:
 * The generated QR code contains:
 * - Driver name
 * - Age
 * - License type
 * - Email address
 *
 * QR Code Specifications:
 * - Format: Standard QR Code
 * - Size: 500x500 pixels
 * - Color: Black on white background
 * - Library: ZXing (Zebra Crossing)
 *
 * Data Flow:
 * 1. Receives user email from MainActivity via Bundle arguments
 * 2. Loads profile data from SharedPreferences using email as key
 * 3. Displays profile information
 * 4. Generates QR code with profile data
 *
 * Logout Behavior:
 * - Shows confirmation dialog
 * - Clears "loggedInUser" from SharedPreferences
 * - Navigates to AuthenticationActivity
 * - Clears activity stack (prevents back navigation)
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class UserFragment : Fragment() {

    // ============================================
    // VIEW PROPERTIES
    // ============================================

    /** Displays driver's full name */
    private lateinit var userName: TextView

    /** Displays driver's email address */
    private lateinit var userEmail: TextView

    /** Displays driver's age */
    private lateinit var userAge: TextView

    /** Displays driver's license type */
    private lateinit var userPermis: TextView

    /** Displays QR code image */
    private lateinit var userQRCodeImageView: ImageView

    /** Button to edit profile (future feature) */
    private lateinit var btnEdit: MaterialButton

    /** Button to logout from account */
    private lateinit var btnLogout: MaterialButton

    // ============================================
    // STATE VARIABLES
    // ============================================

    /** Email of currently logged-in user (received via arguments) */
    private var currentUserEmail: String = ""

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Creates and returns the view hierarchy for this fragment.
     *
     * Initialization sequence:
     * 1. Inflates fragment layout
     * 2. Sets status bar color
     * 3. Initializes view references
     * 4. Retrieves user email from arguments
     * 5. Loads and displays user data
     * 6. Sets up button listeners
     *
     * @param inflater LayoutInflater to inflate views
     * @param container Parent view to attach to (nullable)
     * @param savedInstanceState Previously saved state (if any)
     * @return The root view of the fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ===== INFLATE LAYOUT =====
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        // ===== SET STATUS BAR =====
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // ===== INITIALIZE VIEWS =====
        initializeViews(view)

        // ===== GET USER EMAIL FROM ARGUMENTS =====
        // Email is passed from MainActivity via Bundle
        currentUserEmail = arguments?.getString("userEmail") ?: ""

        // ===== LOAD USER DATA =====
        if (currentUserEmail.isNotEmpty()) {
            // Valid email received - load profile
            loadUserData()
        } else {
            // No email or empty email - show error
            showError("Error: User not found")
        }

        // ===== SETUP LISTENERS =====
        setupListeners()

        return view
    }

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes all view references from the layout.
     *
     * Binds XML elements to Kotlin properties using findViewById().
     *
     * @param view The root view containing all UI elements
     */
    private fun initializeViews(view: View) {
        userName = view.findViewById(R.id.userName)
        userEmail = view.findViewById(R.id.userEmail)
        userAge = view.findViewById(R.id.userAge)
        userPermis = view.findViewById(R.id.userPermis)
        userQRCodeImageView = view.findViewById(R.id.userQRCodeImageView)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    /**
     * Sets up click listeners for interactive buttons.
     *
     * Listeners configured:
     * - Edit button: Shows "coming soon" message (future feature)
     * - Logout button: Shows confirmation dialog
     */
    private fun setupListeners() {
        // ===== EDIT BUTTON =====
        // Future feature - currently shows placeholder message
        btnEdit.setOnClickListener {
            Toast.makeText(requireContext(), "Edit functionality coming soon", Toast.LENGTH_SHORT).show()
            // TODO: Implement edit profile functionality
        }

        // ===== LOGOUT BUTTON =====
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // ============================================
    // DATA LOADING
    // ============================================

    /**
     * Loads user profile data from SharedPreferences and displays it.
     *
     * Data retrieval:
     * - Accesses "UserPreferences" SharedPreferences
     * - Retrieves data using email-based keys:
     *   - {email}_name: Driver name
     *   - {email}_age: Driver age
     *   - {email}_licenseType: License type
     *
     * Display updates:
     * - Sets name, email, age, and license type in TextViews
     * - Generates and displays QR code with profile information
     *
     * Default values:
     * - Name: "Unknown Name"
     * - Age: "N/A"
     * - License: "Not specified"
     */
    private fun loadUserData() {
        // ===== ACCESS SHARED PREFERENCES =====
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // ===== RETRIEVE USER DATA =====
        // Use email as key prefix to get user-specific data
        val name = sharedPreferences.getString("${currentUserEmail}_name", "Unknown Name") ?: "Unknown Name"
        val age = sharedPreferences.getString("${currentUserEmail}_age", "N/A") ?: "N/A"
        val licenseType = sharedPreferences.getString("${currentUserEmail}_licenseType", "Not specified") ?: "Not specified"

        // ===== UPDATE UI =====
        userName.text = name
        userEmail.text = currentUserEmail
        userAge.text = "$age years"
        userPermis.text = licenseType

        // ===== GENERATE QR CODE =====
        generateAndDisplayQRCode(name, age, licenseType)
    }

    // ============================================
    // QR CODE GENERATION
    // ============================================

    /**
     * Generates a QR code containing driver information and displays it.
     *
     * QR Code content format:
     * ```
     * Taxi Driver
     * ==================
     * Name: [name]
     * Age: [age] years
     * License: [licenseType]
     * Email: [email]
     * ```
     *
     * Process:
     * 1. Formats driver information as multi-line string
     * 2. Generates QR code bitmap using ZXing library
     * 3. Displays bitmap in ImageView
     *
     * Error handling:
     * - Catches WriterException if QR generation fails
     * - Shows error toast to user
     *
     * @param name Driver's full name
     * @param age Driver's age (as string)
     * @param licenseType Driver's license type
     */
    private fun generateAndDisplayQRCode(name: String, age: String, licenseType: String) {
        try {
            // ===== FORMAT QR CODE DATA =====
            val qrData = """
                Taxi Driver
                ==================
                Name: $name
                Age: $age years
                License: $licenseType
                Email: $currentUserEmail
            """.trimIndent()

            // ===== GENERATE QR CODE =====
            val qrBitmap = generateQRCode(qrData)

            // ===== DISPLAY QR CODE =====
            userQRCodeImageView.setImageBitmap(qrBitmap)

        } catch (e: WriterException) {
            // ===== ERROR HANDLING =====
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates a QR code bitmap from text data.
     *
     * Technical details:
     * - Uses ZXing MultiFormatWriter
     * - Format: BarcodeFormat.QR_CODE
     * - Size: 500x500 pixels
     * - Color scheme: Black QR code on white background
     * - Bitmap config: RGB_565 (efficient memory usage)
     *
     * Process:
     * 1. Encode text into BitMatrix (2D array of boolean values)
     * 2. Create empty bitmap
     * 3. Iterate through matrix and set pixel colors:
     *    - true (QR code) → Black (0xFF000000)
     *    - false (background) → White (0xFFFFFFFF)
     * 4. Return completed bitmap
     *
     * @param text The text data to encode in QR code
     * @return Bitmap containing the QR code image
     * @throws WriterException If encoding fails
     */
    @Throws(WriterException::class)
    private fun generateQRCode(text: String): Bitmap {
        // ===== ENCODE TEXT TO MATRIX =====
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 500, 500)

        // ===== GET MATRIX DIMENSIONS =====
        val width = bitMatrix.width
        val height = bitMatrix.height

        // ===== CREATE BITMAP =====
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        // ===== FILL BITMAP WITH QR CODE PIXELS =====
        // Iterate through each pixel position
        for (x in 0 until width) {
            for (y in 0 until height) {
                // Set pixel color based on matrix value
                // bitMatrix[x, y] = true → QR code module → Black
                // bitMatrix[x, y] = false → Background → White
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }

        return bitmap
    }

    // ============================================
    // LOGOUT FUNCTIONALITY
    // ============================================

    /**
     * Shows confirmation dialog before logging out.
     *
     * Dialog options:
     * - "Yes": Performs logout
     * - "Cancel": Dismisses dialog
     *
     * Uses Material Design dialog for consistent UI.
     */
    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // User confirmed - proceed with logout
                performLogout()
            }
            .setNegativeButton("Cancel", null)  // Cancel - dismiss dialog
            .show()
    }

    /**
     * Performs the logout operation.
     *
     * Logout process:
     * 1. Removes "loggedInUser" from SharedPreferences (clears session)
     * 2. Shows success toast
     * 3. Creates Intent to AuthenticationActivity
     * 4. Sets Intent flags to clear activity stack
     * 5. Starts AuthenticationActivity
     * 6. Finishes current activity
     *
     * Intent flags used:
     * - FLAG_ACTIVITY_NEW_TASK: Start in a new task
     * - FLAG_ACTIVITY_CLEAR_TASK: Clear all activities in the task
     *
     * Result: User is returned to login screen with no way to navigate
     * back to authenticated screens via back button.
     */
    private fun performLogout() {
        // ===== CLEAR SESSION DATA =====
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("loggedInUser").apply()

        // ===== SHOW SUCCESS MESSAGE =====
        Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show()

        // ===== NAVIGATE TO AUTHENTICATION =====
        val intent = Intent(requireActivity(), AuthenticationActivity::class.java)

        // Set flags to clear the entire activity stack
        // This prevents user from pressing back to return to authenticated screens
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Start login screen
        startActivity(intent)

        // Finish current activity
        requireActivity().finish()
    }

    // ============================================
    // ERROR HANDLING
    // ============================================

    /**
     * Displays error state in the UI when user data cannot be loaded.
     *
     * Error display:
     * - Shows error message as toast
     * - Sets "Error" as name
     * - Shows error message in email field
     * - Sets "N/A" for age and license type
     *
     * This provides user feedback when:
     * - No email was provided in arguments
     * - User data doesn't exist in SharedPreferences
     * - Data loading fails for any reason
     *
     * @param message Error message to display
     */
    private fun showError(message: String) {
        // Show error toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        // Update UI to show error state
        userName.text = "Error"
        userEmail.text = message
        userAge.text = "N/A"
        userPermis.text = "N/A"
    }
}