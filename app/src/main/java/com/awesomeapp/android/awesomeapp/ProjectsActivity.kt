/*
 *    Copyright 2018 MalgoskaG & Bwaim
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
import kotlinx.android.synthetic.main.activity_projects.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class ProjectsActivity : MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)
        setSupportActionBar(myToolbar)

        val intent = intent
        val chosenProjectsExtra = intent.getStringExtra(TABLE_WITH_DATA)


        val rv = findViewById<RecyclerView>(R.id.projectsList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val projects = ArrayList<ProjectsModel>()
        val adapter = ProjectsAdapter(projects, this, chosenProjectsExtra)

        myHelpData.addSnapshotListener(this, { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {

                @Suppress("UNCHECKED_CAST")
                val list = snapshot[chosenProjectsExtra] as ArrayList<String>
                for (value in list) {
                    projects.add(ProjectsModel(value, "deadline", "somePercent%"))
                }
                progressBar2.visibility = View.GONE
                rv.adapter = adapter

            } else {
                Toast.makeText(this, "Data don't exist :(", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
