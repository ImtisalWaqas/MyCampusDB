package com.example.examtask.examtask22l6695

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var goToSignup: TextView
    private lateinit var forgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        goToSignup = findViewById(R.id.tvGoToSignup)
        forgotPassword = findViewById(R.id.tvForgotPassword)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Step 1: Check if email is registered
            auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener { result ->
                    val signInMethods = result.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // ❌ Email not registered: Redirect to signup
                        Toast.makeText(this, "Email not registered. Redirecting to signup...", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, SignupActivity::class.java)
                        intent.putExtra("email", email) // optional: prefill email
                        startActivity(intent)
                        finish()
                    } else {
                        // ✅ Email exists: Try to log in
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                                        .putString("user_id", uid).apply()

                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                }
                            }
                            .addOnFailureListener { exception ->
                                if (exception is FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(this, "Wrong password. Try again or reset.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error checking email: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        forgotPassword.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send reset link: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}