package com.cmc.taximeter

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.security.MessageDigest
import androidx.core.content.edit

/**
 * AuthenticationActivity
 *
 * Handles user authentication (sign-in) functionality for the Taximeter application.
 *
 * Key Features:
 * - Email/password validation
 * - Secure password hashing using SHA-256
 * - Persistent login session management
 * - Password visibility toggle
 * - Navigation to sign-up and main screens
 *
 * Security:
 * - Passwords are hashed with SHA-256 before storage/comparison
 * - Email addresses are normalized (lowercase, trimmed)
 * - Session data stored in SharedPreferences
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class AuthenticationActivity : AppCompatActivity() {

    // ============================================
    // VIEW DECLARATIONS
    // ============================================

    /** Input field for user email address */
    private lateinit var edtEmail: com.google.android.material.textfield.TextInputEditText

    /** Input field for user password (supports masked/visible toggle) */
    private lateinit var edtPassword: com.google.android.material.textfield.TextInputEditText

    /** Button to submit sign-in credentials */
    private lateinit var btnSignIn: com.google.android.material.button.MaterialButton

    /** Toggle button to show/hide password characters */
    private lateinit var ibPassword: ImageButton

    /** Clickable text to navigate to registration screen */
    private lateinit var txtSignUp: TextView

    // ============================================
    // STATE VARIABLES
    // ============================================

    /** Tracks whether password is currently visible (true) or masked (false) */
    private var isPasswordVisible = false

    // ============================================
    // LIFECYCLE METHODS
    // ============================================

    /**
     * Called when the activity is first created.
     *
     * Responsibilities:
     * 1. Sets status bar color to black
     * 2. Inflates the activity layout
     * 3. Checks for existing user session
     * 4. Initializes view references
     * 5. Sets up click listeners
     *
     * @param savedInstanceState Bundle containing activity's previously saved state (if any)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black for consistent UI appearance
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Inflate the authentication screen layout
        setContentView(R.layout.activity_authentication)

        // Check if user is already logged in (AFTER setContentView to avoid null context)
        checkIfUserLoggedIn()

        // Initialize all view references from the layout
        initializeViews()

        // Setup click listeners for interactive elements
        setupListeners()
    }

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes view references by binding XML layout elements to Kotlin properties.
     *
     * This method uses findViewById() to link each view component from the XML layout
     * to its corresponding property in this activity class.
     *
     * Views initialized:
     * - Email input field
     * - Password input field
     * - Sign-in button
     * - Sign-up text link
     * - Password visibility toggle button
     */
    private fun initializeViews() {
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        txtSignUp = findViewById(R.id.txtSignUp)
        ibPassword = findViewById(R.id.ibPassword)
    }

    /**
     * Sets up click listeners for all interactive UI elements.
     *
     * Listeners configured:
     * - Password visibility toggle: Shows/hides password characters
     * - Sign-up text: Navigates to registration screen
     * - Sign-in button: Initiates authentication process
     */
    private fun setupListeners() {
        // Toggle password visibility when eye icon is clicked
        ibPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Navigate to sign-up screen when "Sign Up" text is clicked
        txtSignUp.setOnClickListener {
            navigateToSignUp()
        }

        // Attempt sign-in when button is clicked
        btnSignIn.setOnClickListener {
            signIn()
        }
    }

    // ============================================
    // SESSION MANAGEMENT
    // ============================================

    /**
     * Checks if a user is already logged in and redirects to MainActivity if true.
     *
     * This method implements persistent login functionality by checking SharedPreferences
     * for an existing user session. If found, the user is automatically redirected to
     * the main app screen, bypassing the login form.
     *
     * Flow:
     * 1. Access SharedPreferences ("UserPreferences")
     * 2. Retrieve "loggedInUser" value (email of logged-in user)
     * 3. If value exists, navigate to MainActivity
     * 4. If null, user must authenticate normally
     *
     * Storage Key: "loggedInUser" - Contains email of currently logged-in user
     */
    private fun checkIfUserLoggedIn() {
        // Access app's SharedPreferences for user data
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        // Check if there's a logged-in user stored
        val loggedInUser = sharedPreferences.getString("loggedInUser", null)

        if (loggedInUser != null) {
            // User session exists - skip login and go directly to main app
            navigateToMainActivity()
        }
        // If loggedInUser is null, continue with normal login flow
    }

    // ============================================
    // UI INTERACTION METHODS
    // ============================================

    /**
     * Toggles password visibility between masked (dots) and plain text.
     *
     * When password is hidden (default):
     * - Input type: TYPE_TEXT_VARIATION_PASSWORD (shows dots/asterisks)
     * - Icon: visibility_off (closed eye)
     *
     * When password is visible:
     * - Input type: TYPE_TEXT_VARIATION_VISIBLE_PASSWORD (shows characters)
     * - Icon: visibility (open eye)
     *
     * After toggling:
     * - Updates isPasswordVisible state
     * - Maintains cursor position at end of text
     *
     * Called by: Click listener on ibPassword (eye icon)
     */
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Currently visible - switch to hidden/masked mode
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ibPassword.setImageResource(R.drawable.visibility_off)
        } else {
            // Currently hidden - switch to visible/plain text mode
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ibPassword.setImageResource(R.drawable.visibility)
        }

        // Toggle the state flag
        isPasswordVisible = !isPasswordVisible

        // Keep cursor at the end of the text after changing input type
        edtPassword.setSelection(edtPassword.text?.length ?: 0)
    }

    // ============================================
    // VALIDATION METHODS
    // ============================================

    /**
     * Validates whether a given string is a properly formatted email address.
     *
     * Validation criteria:
     * 1. Email must not be empty
     * 2. Email must match standard email pattern (user@domain.ext)
     *
     * Uses Android's built-in Patterns.EMAIL_ADDRESS regex matcher for validation.
     *
     * @param email The email string to validate
     * @return true if email is valid, false otherwise
     *
     * Examples:
     * - "user@example.com" → true
     * - "invalid.email" → false
     * - "" → false
     * - "user@domain" → true (depending on Patterns implementation)
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // ============================================
    // SECURITY: PASSWORD HASHING
    // ============================================

    /**
     * Hashes a plain-text password using SHA-256 cryptographic algorithm.
     *
     * Security benefits:
     * - One-way function: Cannot reverse hash to get original password
     * - Same input always produces same hash (for comparison)
     * - Different inputs produce vastly different hashes
     *
     * Process:
     * 1. Convert password string to byte array
     * 2. Apply SHA-256 algorithm using MessageDigest
     * 3. Convert resulting hash bytes to hexadecimal string
     * 4. Return 64-character hex string
     *
     * @param password The plain-text password to hash
     * @return A 64-character hexadecimal string representing the SHA-256 hash
     *
     * Example:
     * Input: "myPassword123"
     * Output: "a1b2c3d4e5f6..." (64 hex characters)
     *
     * Note: SHA-256 is a standard hashing algorithm. For production apps,
     * consider using more robust algorithms like bcrypt or Argon2.
     */
    private fun hashPassword(password: String): String {
        // Get SHA-256 MessageDigest instance
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())

        // Convert byte array to hexadecimal string
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ============================================
    // AUTHENTICATION LOGIC
    // ============================================

    /**
     * Authenticates user credentials and establishes a login session if valid.
     *
     * Authentication Flow:
     * 1. INPUT RETRIEVAL
     *    - Get email (trimmed, converted to lowercase)
     *    - Get password (as entered)
     *
     * 2. VALIDATION
     *    - Validate email format using isValidEmail()
     *    - Check that password is not empty
     *    - Show errors and return if validation fails
     *
     * 3. CREDENTIAL VERIFICATION
     *    - Retrieve stored password hash from SharedPreferences
     *    - Key format: "{email}_password"
     *    - If no stored hash exists → Account doesn't exist
     *    - Hash entered password using SHA-256
     *    - Compare entered hash with stored hash
     *
     * 4. SUCCESS PATH
     *    - Save email to SharedPreferences as "loggedInUser"
     *    - Show success toast message
     *    - Navigate to MainActivity
     *
     * 5. FAILURE PATH
     *    - Show "incorrect password" error
     *    - Keep focus on password field for retry
     *
     * SharedPreferences Keys Used:
     * - Read: "{email}_password" - Stored password hash for the user
     * - Write: "loggedInUser" - Email of currently authenticated user
     *
     * Error Messages:
     * - "Invalid email" - Invalid email format
     * - "Password required" - Password required
     * - "Account does not exist. Please sign up" - Account doesn't exist
     * - "Incorrect password" - Incorrect password
     * - "Login successful!" - Login successful
     */
    private fun signIn() {
        // ===== INPUT RETRIEVAL =====
        // Get email and normalize it (trim whitespace, convert to lowercase)
        val email = edtEmail.text.toString().trim().lowercase()

        // Get password as entered (no normalization)
        val password = edtPassword.text.toString()

        // ===== EMAIL VALIDATION =====
        if (!isValidEmail(email)) {
            edtEmail.error = "Invalid email"
            edtEmail.requestFocus()  // Focus on email field for correction
            return  // Stop execution
        }

        // ===== PASSWORD VALIDATION =====
        if (password.isEmpty()) {
            edtPassword.error = "Password required"
            edtPassword.requestFocus()  // Focus on password field
            return  // Stop execution
        }

        // ===== CREDENTIAL VERIFICATION =====
        // Access SharedPreferences to retrieve stored user data
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)

        // Retrieve stored password hash for this email
        // Storage format: "{email}_password" → hash value
        val storedPasswordHash = sharedPreferences.getString("${email}_password", null)

        // Check if account exists
        if (storedPasswordHash == null) {
            // No stored hash = account doesn't exist
            Toast.makeText(this, "Account does not exist. Please sign up", Toast.LENGTH_LONG).show()
            return  // Stop execution - user needs to sign up first
        }

        // Hash the entered password for comparison
        val enteredPasswordHash = hashPassword(password)

        // ===== AUTHENTICATION DECISION =====
        if (storedPasswordHash == enteredPasswordHash) {
            // ===== SUCCESS PATH =====
            // Passwords match - authentication successful

            // Save user session by storing email as "loggedInUser"
            sharedPreferences.edit {
                putString("loggedInUser", email)
            }

            // Show success message to user
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

            // Navigate to main application screen
            navigateToMainActivity()
        } else {
            // ===== FAILURE PATH =====
            // Passwords don't match - authentication failed

            // Show error toast
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()

            // Show inline error on password field
            edtPassword.error = "Incorrect password"

            // Focus on password field for retry
            edtPassword.requestFocus()
        }
    }

    // ============================================
    // NAVIGATION METHODS
    // ============================================

    /**
     * Navigates to the main application screen (MainActivity).
     *
     * This method is called in two scenarios:
     * 1. After successful authentication (from signIn())
     * 2. When an existing session is detected (from checkIfUserLoggedIn())
     *
     * Navigation behavior:
     * - Creates Intent targeting MainActivity
     * - Starts the MainActivity
     * - Calls finish() to remove AuthenticationActivity from back stack
     * - Prevents user from pressing back button to return to login
     *
     * Security consideration:
     * Calling finish() ensures users cannot navigate back to the authentication
     * screen after logging in, which would be confusing UX and a potential
     * security issue.
     */
    private fun navigateToMainActivity() {
        // Create intent to start MainActivity
        val intent = Intent(this, MainActivity::class.java)

        // Start the new activity
        startActivity(intent)

        // Remove this activity from the back stack
        // User cannot press back to return to login screen
        finish()
    }

    /**
     * Navigates to the registration screen (SignUpActivity).
     *
     * This method is called when the user clicks the "Sign Up" text,
     * indicating they want to create a new account.
     *
     * Navigation behavior:
     * - Creates Intent targeting SignUpActivity
     * - Starts the SignUpActivity
     * - Does NOT call finish() - keeps AuthenticationActivity in back stack
     * - Allows user to press back button to return to login if needed
     *
     * User flow:
     * Login Screen → [Click "Sign Up"] → Registration Screen
     * Registration Screen → [Press Back] → Login Screen (returns here)
     */
    private fun navigateToSignUp() {
        // Create intent to start SignUpActivity
        val intent = Intent(this, SignUpActivity::class.java)

        // Start the registration activity
        startActivity(intent)

        // Note: finish() is NOT called here
        // User can return to login screen via back button
    }

    // ============================================
    // BACK NAVIGATION HANDLING
    // ============================================

    /**
     * Handles the device back button press event.
     *
     * Behavior:
     * - Calls super.onBackPressed() to execute default back behavior
     * - Calls finishAffinity() to close this activity AND all parent activities
     * - Prevents user from navigating back to onboarding/splash screens
     *
     * Use case:
     * If user presses back on the login screen, the app should close entirely
     * rather than returning to the splash/onboarding flow.
     *
     * finishAffinity() explanation:
     * - Finishes this activity and all activities below it in the task stack
     * - Ensures clean exit from the authentication flow
     *
     * DEPRECATION NOTE:
     * This method is deprecated in favor of OnBackPressedDispatcher.
     * Modern implementation should use:
     *
     * onBackPressedDispatcher.addCallback(this) {
     *     finishAffinity()
     * }
     *
     * However, this implementation still works for backward compatibility.
     *
     * @deprecated Use OnBackPressedDispatcher with OnBackPressedCallback instead
     */
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        // Execute default back button behavior
        super.onBackPressed()

        // Close this activity and all parent activities in the task
        // Prevents returning to splash/onboarding screens
        finishAffinity()
    }
}