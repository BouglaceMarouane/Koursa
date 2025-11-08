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

class SignUpActivity : AppCompatActivity() {

    // View declarations
    private lateinit var edtEmail: com.google.android.material.textfield.TextInputEditText
    private lateinit var edtPassword: com.google.android.material.textfield.TextInputEditText
    private lateinit var edtName: com.google.android.material.textfield.TextInputEditText
    private lateinit var edtAge: com.google.android.material.textfield.TextInputEditText
    private lateinit var spinnerLicenseType: Spinner
    private lateinit var btnSignUp: com.google.android.material.button.MaterialButton
    private lateinit var ibPassword: ImageButton
    private lateinit var txtSignIn: TextView

    private var isPasswordVisible = false
    private val MIN_PASSWORD_LENGTH = 6
    private val MIN_AGE = 18
    private val MAX_AGE = 70

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar to black
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setContentView(R.layout.activity_sign_up)

        // Initialize views
        initializeViews()

        // Setup listeners
        setupListeners()

        // Setup spinner
        setupSpinner()
    }

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

    private fun setupListeners() {
        ibPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        txtSignIn.setOnClickListener {
            navigateToSignIn()
        }

        btnSignUp.setOnClickListener {
            signUp()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.license_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLicenseType.adapter = adapter
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

    private fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    private fun isValidName(name: String): Boolean {
        return name.isNotEmpty() && name.length >= 2 && name.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$"))
    }

    private fun isValidAge(age: String): Boolean {
        return try {
            val ageInt = age.toInt()
            ageInt in MIN_AGE..MAX_AGE
        } catch (e: NumberFormatException) {
            false
        }
    }

    // ============ SECURITY: PASSWORD HASHING ============

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ============ SIGN UP LOGIC ============

    private fun signUp() {
        val email = edtEmail.text.toString().trim().lowercase()
        val password = edtPassword.text.toString()
        val name = edtName.text.toString().trim()
        val age = edtAge.text.toString().trim()
        val licenseType = spinnerLicenseType.selectedItem.toString()

        // Validation de l'email
        if (!isValidEmail(email)) {
            edtEmail.error = "Email invalide"
            edtEmail.requestFocus()
            return
        }

        // Validation du mot de passe
        if (!isValidPassword(password)) {
            edtPassword.error = "Le mot de passe doit contenir au moins $MIN_PASSWORD_LENGTH caractères"
            edtPassword.requestFocus()
            return
        }

        // Validation du nom
        if (!isValidName(name)) {
            edtName.error = "Nom invalide (lettres uniquement, minimum 2 caractères)"
            edtName.requestFocus()
            return
        }

        // Validation de l'âge
        if (!isValidAge(age)) {
            edtAge.error = "Âge invalide (entre $MIN_AGE et $MAX_AGE ans)"
            edtAge.requestFocus()
            return
        }

        // Check if email already exists
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val existingPassword = sharedPreferences.getString("${email}_password", null)

        if (existingPassword != null) {
            Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_LONG).show()
            edtEmail.error = "Email déjà utilisé"
            edtEmail.requestFocus()
            return
        }

        // Hash the password before storing
        val passwordHash = hashPassword(password)

        // Save user data
        val editor = sharedPreferences.edit()
        editor.putString("${email}_password", passwordHash)
        editor.putString("${email}_name", name)
        editor.putString("${email}_age", age)
        editor.putString("${email}_licenseType", licenseType)
        editor.apply()

        Toast.makeText(this, "Inscription réussie ! Connectez-vous maintenant", Toast.LENGTH_LONG).show()

        // Navigate to sign in
        navigateToSignIn()
    }

    // ============ NAVIGATION ============

    private fun navigateToSignIn() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Go back to sign in
        navigateToSignIn()
    }
}