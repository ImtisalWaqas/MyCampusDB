package com.example.examtask.examtask22l6695

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddUpdateStudentActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var departmentInput: EditText
    private lateinit var spinnerYear: Spinner
    private lateinit var dateInput: EditText
    private lateinit var saveButton: Button

    private lateinit var selectedYear: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_update_student)

        nameInput = findViewById(R.id.etName)
        departmentInput = findViewById(R.id.etDepartment)
        spinnerYear = findViewById(R.id.spinnerYear)
        dateInput = findViewById(R.id.etDate)
        saveButton = findViewById(R.id.btnSave)

        // Setup Spinner for Year (1990 - Current Year)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1990..currentYear).map { it.toString() }.reversed()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        spinnerYear.adapter = adapter
        selectedYear = years[0] // default selection

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedYear = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Setup Date Picker
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                val formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                dateInput.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }

        // Save logic
        saveButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = nameInput.text.toString().trim()
            val dept = departmentInput.text.toString().trim()
            val date = dateInput.text.toString().trim()

            if (name.isEmpty() || dept.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "name" to name,
                "department" to dept,
                "year" to selectedYear,
                "dateOfRegistration" to date
            )

            FirebaseDatabase.getInstance().getReference("students")
                .child(uid)
                .updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK) // ✅ Notify caller that update succeeded
                    finish() // ✅ Go back to HomeActivity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}