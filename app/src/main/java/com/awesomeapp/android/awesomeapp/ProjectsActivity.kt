package com.awesomeapp.android.awesomeapp

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.awesomeapp.android.awesomeapp.adapters.ProjectsAdapter
import com.awesomeapp.android.awesomeapp.data.Constant.TABLE_WITH_DATA
import com.awesomeapp.android.awesomeapp.data.Constant.myHelpData
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_projects.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class ProjectsActivity : MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)
        setSupportActionBar(myToolbar)

        val intent = intent;
        var choosenProjectsExtra = intent.getStringExtra(TABLE_WITH_DATA)


        val rv = findViewById<RecyclerView>(R.id.projectsList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val projects = ArrayList<ProjectsModel>()
        val adapter = ProjectsAdapter(projects, this, choosenProjectsExtra)

        myHelpData.addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    var list = document.get(choosenProjectsExtra) as ArrayList<String>
                    for (value in list) {
                        projects.add(ProjectsModel(value, "deadline", "somePercent%"))
                    }
                    progressBar2.visibility = View.GONE
                    rv.adapter = adapter

                    Toast.makeText(this, "work :)", Toast.LENGTH_SHORT).show()
                } else {
                    projects.add(ProjectsModel("no data", "deadline", "somePercent%"))
                    Toast.makeText(this, "Data don't exist :(", Toast.LENGTH_SHORT).show()
                }
                return@addOnCompleteListener
            } else {
                Toast.makeText(this, "Error :(", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
