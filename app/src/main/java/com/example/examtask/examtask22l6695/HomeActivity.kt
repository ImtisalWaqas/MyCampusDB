package com.example.examtask.examtask22l6695

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: StudentAdapter
    private val students = ArrayList<Student>()
    private lateinit var dbRef: DatabaseReference
    private var userDepartment: String? = null
    private lateinit var logoutButton: FloatingActionButton
    private lateinit var updateButton: FloatingActionButton

    private lateinit var updateLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        listView = findViewById(R.id.studentListView)
        logoutButton = findViewById(R.id.logoutButton)
        updateButton = findViewById(R.id.updateButton)

        adapter = StudentAdapter(this, students)
        listView.adapter = adapter
        dbRef = FirebaseDatabase.getInstance().getReference("students")

        // 🔁 Logout action
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // ✅ Register for result from update activity
        updateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Re-fetch department and update list
                dbRef.child(userId).get().addOnSuccessListener { snapshot ->
                    val currentUser = snapshot.getValue(Student::class.java)
                    userDepartment = currentUser?.department
                    fetchStudents()
                }
            }
        }

        // ✏️ Update profile
        updateButton.setOnClickListener {
            val intent = Intent(this, AddUpdateStudentActivity::class.java)
            intent.putExtra("userId", userId)
            updateLauncher.launch(intent)
        }

        // Initial fetch of user department
        dbRef.child(userId).get().addOnSuccessListener { snapshot ->
            val currentUser = snapshot.getValue(Student::class.java)
            userDepartment = currentUser?.department
            fetchStudents()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get user info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStudents() {
        dbRef.orderByChild("dateOfRegistration").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                students.clear()
                for (child in snapshot.children) {
                    val student = child.getValue(Student::class.java)
                    if (student != null) {
                        students.add(student)
                    }
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                students.sortBy {
                    try {
                        sdf.parse(it.dateOfRegistration)
                    } catch (e: Exception) {
                        null
                    }
                }

                adapter.setHighlightDepartment(userDepartment)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}