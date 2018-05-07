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
import android.widget.LinearLayout
import com.awesomeapp.android.awesomeapp.adapters.ProjectsAdapter
import com.awesomeapp.android.awesomeapp.data.Constant.TABLE_WITH_DATA
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import com.awesomeapp.android.awesomeapp.util.QueryUtils
import kotlinx.android.synthetic.main.activity_projects.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton


class ProjectsActivity : MenuActivity(), ProjectRefreshable {

    private var projects: ArrayList<ProjectsModel> = ArrayList()
    private lateinit var rv: RecyclerView
    private lateinit var track: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        val intent = intent
        track = intent.getStringExtra(TABLE_WITH_DATA)

        myToolbar.title = track
        setSupportActionBar(myToolbar)

        rv = findViewById(R.id.projectsList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        projects.addAll(QueryUtils.getProjects(track) ?: ArrayList())

        var adapter = ProjectsAdapter(projects, this)

        rv.adapter = adapter


        //if the data in nbUsers will change or the projects will not be able to load when the application is first time turned on
        swipyrefreshlayout.setOnRefreshListener({
            adapter.notifyDataSetChanged()
            swipyrefreshlayout.isRefreshing = false

            if (projects.size == 0) {
                projects = QueryUtils.getProjects(track) ?: ArrayList()
                adapter = ProjectsAdapter(projects, this)
                rv.adapter = adapter
            }
        })

        if (projects.size == 0) {
            alert(getText(R.string.refresh_data_by_swip)) {
                yesButton { }
            }.show()
        }
        QueryUtils.addProjectRefreshableActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        QueryUtils.removeProjectRefreshableActivity(this)
    }

    override fun refreshUI(p: ProjectsModel) {
        projects.clear()
        projects.addAll(QueryUtils.getProjects(track) ?: ArrayList())
        rv.adapter.notifyDataSetChanged()
    }
}

interface ProjectRefreshable {
    fun refreshUI(p: ProjectsModel)
}
