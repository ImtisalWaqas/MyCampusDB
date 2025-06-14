package com.example.examtask.examtask22l6695

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class StudentAdapter(private val context: Context, private val data: List<Student>) : BaseAdapter() {

    private var highlightDept: String? = null

    fun setHighlightDepartment(department: String?) {
        highlightDept = department
    }

    override fun getCount() = data.size

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val student = data[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.student_item, parent, false)

        val nameView: TextView = view.findViewById(R.id.tvStudentName)
        val deptView: TextView = view.findViewById(R.id.tvStudentDept)

        nameView.text = student.name
        deptView.text = "Dept: ${student.department}"

        if (student.department == highlightDept) {
            view.setBackgroundColor(Color.parseColor("#C8E6C9")) // light green
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }

        return view
    }
}