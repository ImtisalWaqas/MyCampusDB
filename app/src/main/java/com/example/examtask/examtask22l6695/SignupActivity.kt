package com.example.examtask.examtask22l6695

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var studentIdInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var departmentInput: EditText
    private lateinit var yearSpinner: Spinner
    private lateinit var dateInput: EditText
    private lateinit var signupButton: Button
    private lateinit var goToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        studentIdInput = findViewById(R.id.etStudentId)
        nameInput = findViewById(R.id.etName)
        departmentInput = findViewById(R.id.etDepartment)
        yearSpinner = findViewById(R.id.spinnerYear)
        dateInput = findViewById(R.id.etDate)
        signupButton = findViewById(R.id.btnSignup)
        goToLogin = findViewById(R.id.tvGoToLogin)

        // 🔽 Populate Spinner with years
        val yearList = (1990..Calendar.getInstance().get(Calendar.YEAR)).map { it.toString() }.reversed()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, yearList)
        yearSpinner.adapter = adapter

        // 📅 Show DatePicker on EditText click
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                dateInput.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val studentId = studentIdInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val department = departmentInput.text.toString().trim()
            val year = yearSpinner.selectedItem.toString()
            val date = dateInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                    val student = Student(studentId, name, department, year, date, email)

                    FirebaseDatabase.getInstance()
                        .getReference("students")
                        .child(uid)
                        .setValue(student)

                    Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    if (exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this,
                            "Email already registered. Redirecting to login...",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Signup failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}