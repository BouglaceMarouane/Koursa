package com.cmc.taximeter

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.security.MessageDigest

/**
 * SignUpActivity
 *
 * Handles new user registration for the Taximeter application.
 *
 * Key Features:
 * - User registration with email and password
 * - Driver-specific information collection (name, age, license type)
 * - Comprehensive input validation
 * - Secure password hashing (SHA-256)
 * - Duplicate email detection
 * - Password visibility toggle
 * - Navigation to sign-in after successful registration
 *
 * Validation Rules:
 * - Email: Must be valid format
 * - Password: Minimum 6 characters
 * - Name: Letters only, minimum 2 characters (supports accented characters)
 * - Age: Between 18 and 70 years
 * - License Type: Selected from predefined list
 *
 * Data Storage:
 * User data stored in SharedPreferences with email-based keys:
 * - {email}_password: Hashed password
 * - {email}_name: Driver name
 * - {email}_age: Driver age
 * - {email}_licenseType: License type
 *
 * Security:
 * - Passwords hashed using SHA-256 before storage
 * - Email normalized (lowercase, trimmed)
 * - No plain-text password storage
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class SignUpActivity : AppCompatActivity() {

    // ============================================
    // VIEW PROPERTIES
    // ============================================

    /** Input field for email address */
    private lateinit var edtEmail: TextInputEditText

    /** Input field for password */
    private lateinit var edtPassword: TextInputEditText

    /** Input field for driver's full name */
    private lateinit var edtName: TextInputEditText

    /** Input field for driver's age */
    private lateinit var edtAge: TextInputEditText

    /** Dropdown selector for driver's license type */
    private lateinit var spinnerLicenseType: Spinner

    /** Button to submit registration */
    private lateinit var btnSignUp: MaterialButton

    /** Toggle button for password visibility */
    private lateinit var ibPassword: ImageButton

    /** Clickable text to navigate back to sign-in */
    private lateinit var txtSignIn: TextView

    // ============================================
    // STATE VARIABLES
    // ============================================

    /** Tracks whether password is currently visible */
    private var isPasswordVisible = false

    // ============================================
    // VALIDATION CONSTANTS
    // ============================================

    /** Minimum password length required */
    private val MIN_PASSWORD_LENGTH = 6

    /** Minimum age allowed for drivers */
    private val MIN_AGE = 18

    /** Maximum age allowed for drivers */
    private val MAX_AGE = 70

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Called when the activity is created.
     *
     * Initialization sequence:
     * 1. Sets status bar color
     * 2. Inflates layout
     * 3. Initializes view references
     * 4. Sets up click listeners
     * 5. Configures license type spinner
     *
     * @param savedInstanceState Previously saved state (if any)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black for consistent UI
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setContentView(R.layout.activity_sign_up)

        // Initialize components
        initializeViews()
        setupListeners()
        setupSpinner()
    }

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes all view references from the layout.
     *
     * Binds XML elements to Kotlin properties using findViewById().
     */
    private fun initializeViews() {
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        edtName = findViewById(R.id.edtName)
        edtAge = findViewById(R.id.edtAge)
        spinnerLicenseType = findViewById(R.id.spinnerLicenseType)
        btnSignUp = findViewById(R.id.btnSignUp)
        txtSignIn = findViewById(R.id.txtSignIn)
        ibPassword = findViewById(R.id.ibPassword)
    }

    /**
     * Sets up click listeners for interactive UI elements.
     *
     * Listeners configured:
     * - Password visibility toggle
     * - Sign-in navigation link
     * - Sign-up submission button
     */
    private fun setupListeners() {
        // Password visibility toggle
        ibPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Navigate to sign-in screen
        txtSignIn.setOnClickListener {
            navigateToSignIn()
        }

        // Attempt registration
        btnSignUp.setOnClickListener {
            signUp()
        }
    }

    /**
     * Configures the license type spinner with predefined options.
     *
     * License types are loaded from string array resource (R.array.license_types).
     * Uses standard Android spinner layouts for dropdown display.
     */
    private fun setupSpinner() {
        // Create adapter from string array resource
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.license_types,  // Defined in strings.xml
            android.R.layout.simple_spinner_item
        )

        // Set dropdown view layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attach adapter to spinner
        spinnerLicenseType.adapter = adapter
    }

    // ============================================
    // UI INTERACTION METHODS
    // ============================================

    /**
     * Toggles password visibility between masked and plain text.
     *
     * States:
     * - Visible: Shows actual characters, open eye icon
     * - Hidden: Shows dots/asterisks, closed eye icon
     *
     * Maintains cursor position at end of text after toggle.
     */
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // ===== HIDE PASSWORD =====
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ibPassword.setImageResource(R.drawable.visibility_off)
        } else {
            // ===== SHOW PASSWORD =====
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ibPassword.setImageResource(R.drawable.visibility)
        }

        // Toggle state flag
        isPasswordVisible = !isPasswordVisible

        // Keep cursor at end of text
        edtPassword.setSelection(edtPassword.text?.length ?: 0)
    }

    // ============================================
    // VALIDATION METHODS
    // ============================================

    /**
     * Validates email address format.
     *
     * Checks:
     * - Email is not empty
     * - Email matches standard email pattern (user@domain.ext)
     *
     * Uses Android's built-in email pattern matcher.
     *
     * @param email Email string to validate
     * @return true if valid, false otherwise
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password meets minimum length requirement.
     *
     * @param password Password string to validate
     * @return true if password is at least MIN_PASSWORD_LENGTH characters
     */
    private fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Validates driver name.
     *
     * Requirements:
     * - Not empty
     * - Minimum 2 characters
     * - Contains only letters (a-z, A-Z) and accented characters (À-ÿ)
     * - Allows spaces for compound names
     *
     * Regex pattern: ^[a-zA-ZÀ-ÿ\\s]+$
     *
     * @param name Name string to validate
     * @return true if valid, false otherwise
     */
    private fun isValidName(name: String): Boolean {
        return name.isNotEmpty() &&
                name.length >= 2 &&
                name.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$"))
    }

    /**
     * Validates driver age.
     *
     * Requirements:
     * - Must be a valid integer
     * - Between MIN_AGE (18) and MAX_AGE (70) inclusive
     *
     * @param age Age string to validate
     * @return true if valid, false otherwise
     */
    private fun isValidAge(age: String): Boolean {
        return try {
            val ageInt = age.toInt()
            ageInt in MIN_AGE..MAX_AGE
        } catch (e: NumberFormatException) {
            false  // Not a valid number
        }
    }

    // ============================================
    // SECURITY: PASSWORD HASHING
    // ============================================

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * Process:
     * 1. Convert password string to byte array
     * 2. Apply SHA-256 hash algorithm
     * 3. Convert hash bytes to hexadecimal string
     * 4. Return 64-character hex string
     *
     * Security note:
     * SHA-256 is a one-way function - passwords cannot be recovered from hash.
     * For production apps, consider stronger algorithms like bcrypt or Argon2.
     *
     * @param password Plain-text password to hash
     * @return 64-character hexadecimal hash string
     */
    private fun hashPassword(password: String): String {
        // Apply SHA-256 algorithm
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())

        // Convert to hexadecimal string
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ============================================
    // REGISTRATION LOGIC
    // ============================================

    /**
     * Processes user registration with comprehensive validation.
     *
     * Registration flow:
     * 1. Retrieve and normalize input data
     * 2. Validate all fields sequentially
     * 3. Check for duplicate email
     * 4. Hash password
     * 5. Save user data to SharedPreferences
     * 6. Show success message
     * 7. Navigate to sign-in screen
     *
     * Validation order (stops at first error):
     * 1. Email format
     * 2. Password length
     * 3. Name format
     * 4. Age range
     * 5. Email uniqueness
     *
     * Data storage format:
     * - Key: "{email}_password" → Value: Password hash
     * - Key: "{email}_name" → Value: Driver name
     * - Key: "{email}_age" → Value: Age string
     * - Key: "{email}_licenseType" → Value: License type
     *
     * Error handling:
     * - Shows specific error message for each validation failure
     * - Sets focus on problematic field
     * - Prevents registration if any validation fails
     */
    private fun signUp() {
        // ===== INPUT RETRIEVAL =====
        val email = edtEmail.text.toString().trim().lowercase()  // Normalize email
        val password = edtPassword.text.toString()
        val name = edtName.text.toString().trim()
        val age = edtAge.text.toString().trim()
        val licenseType = spinnerLicenseType.selectedItem.toString()

        // ===== VALIDATION 1: EMAIL =====
        if (!isValidEmail(email)) {
            edtEmail.error = "Email invalide"  // Invalid email
            edtEmail.requestFocus()
            return
        }

        // ===== VALIDATION 2: PASSWORD =====
        if (!isValidPassword(password)) {
            edtPassword.error = "Le mot de passe doit contenir au moins $MIN_PASSWORD_LENGTH caractères"
            // Password must contain at least N characters
            edtPassword.requestFocus()
            return
        }

        // ===== VALIDATION 3: NAME =====
        if (!isValidName(name)) {
            edtName.error = "Nom invalide (lettres uniquement, minimum 2 caractères)"
            // Invalid name (letters only, minimum 2 characters)
            edtName.requestFocus()
            return
        }

        // ===== VALIDATION 4: AGE =====
        if (!isValidAge(age)) {
            edtAge.error = "Âge invalide (entre $MIN_AGE et $MAX_AGE ans)"
            // Invalid age (between N and M years)
            edtAge.requestFocus()
            return
        }

        // ===== DUPLICATE EMAIL CHECK =====
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val existingPassword = sharedPreferences.getString("${email}_password", null)

        if (existingPassword != null) {
            // Account with this email already exists
            Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_LONG).show()
            // This email is already in use
            edtEmail.error = "Email déjà utilisé"  // Email already used
            edtEmail.requestFocus()
            return
        }

        // ===== PASSWORD HASHING =====
        val passwordHash = hashPassword(password)

        // ===== SAVE USER DATA =====
        val editor = sharedPreferences.edit()
        editor.putString("${email}_password", passwordHash)
        editor.putString("${email}_name", name)
        editor.putString("${email}_age", age)
        editor.putString("${email}_licenseType", licenseType)
        editor.apply()

        // ===== SUCCESS =====
        Toast.makeText(this, "Inscription réussie ! Connectez-vous maintenant", Toast.LENGTH_LONG).show()
        // Registration successful! Sign in now

        // Navigate to sign-in screen
        navigateToSignIn()
    }

    // ============================================
    // NAVIGATION METHODS
    // ============================================

    /**
     * Navigates to the authentication (sign-in) screen.
     *
     * Calls finish() to remove SignUpActivity from back stack,
     * preventing double back press to return here.
     */
    private fun navigateToSignIn() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()  // Remove from back stack
    }

    // ============================================
    // BACK BUTTON HANDLING
    // ============================================

    /**
     * Handles device back button press.
     *
     * Navigates back to sign-in screen instead of previous activity.
     * This ensures consistent navigation flow.
     *
     * @deprecated Use OnBackPressedDispatcher for modern implementation
     */
    override fun onBackPressed() {
        super.onBackPressed()
        // Go back to sign in
        navigateToSignIn()
    }
}