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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.security.MessageDigest

class AuthenticationActivity : AppCompatActivity() {

    // View declarations
    private lateinit var edtEmail: com.google.android.material.textfield.TextInputEditText
    private lateinit var edtPassword: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnSignIn: com.google.android.material.button.MaterialButton
    private lateinit var ibPassword: ImageButton
    private lateinit var txtSignUp: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // Check if user is already logged in
        checkIfUserLoggedIn()

        setContentView(R.layout.activity_authentication)

        // Initialize views
        initializeViews()

        // Setup listeners
        setupListeners()
    }

    private fun initializeViews() {
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        txtSignUp = findViewById(R.id.txtSignUp)
        ibPassword = findViewById(R.id.ibPassword)
    }

    private fun setupListeners() {
        ibPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        txtSignUp.setOnClickListener {
            navigateToSignUp()
        }

        btnSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun checkIfUserLoggedIn() {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString("loggedInUser", null)

        if (loggedInUser != null) {
            // User is already logged in, go to MainActivity
            navigateToMainActivity()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            ibPassword.setImageResource(R.drawable.visibility_off)
        } else {
            // Show password
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ibPassword.setImageResource(R.drawable.visibility)
        }

        isPasswordVisible = !isPasswordVisible
        edtPassword.setSelection(edtPassword.text?.length ?: 0)
    }

    // ============ VALIDATION METHODS ============

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // ============ SECURITY: PASSWORD HASHING ============

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ============ SIGN IN LOGIC ============

    private fun signIn() {
        val email = edtEmail.text.toString().trim().lowercase()
        val password = edtPassword.text.toString()

        // Validation
        if (!isValidEmail(email)) {
            edtEmail.error = "Email invalide"
            edtEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            edtPassword.error = "Mot de passe requis"
            edtPassword.requestFocus()
            return
        }

        // Check credentials
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val storedPasswordHash = sharedPreferences.getString("${email}_password", null)

        if (storedPasswordHash == null) {
            Toast.makeText(this, "Compte inexistant. Veuillez vous inscrire", Toast.LENGTH_LONG).show()
            return
        }

        val enteredPasswordHash = hashPassword(password)

        if (storedPasswordHash == enteredPasswordHash) {
            // Successful login
            val editor = sharedPreferences.edit()
            editor.putString("loggedInUser", email)
            editor.apply()

            Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show()
            edtPassword.error = "Mot de passe incorrect"
            edtPassword.requestFocus()
        }
    }

    // ============ NAVIGATION ============

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Prevent going back to onboarding or splash
        finishAffinity()
    }
}