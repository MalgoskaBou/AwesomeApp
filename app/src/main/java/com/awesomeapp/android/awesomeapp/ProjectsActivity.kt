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
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.awesomeapp.android.awesomeapp.R.id.*
import com.awesomeapp.android.awesomeapp.adapters.ProjectsAdapter
import com.awesomeapp.android.awesomeapp.data.Constant.TABLE_WITH_DATA
import com.awesomeapp.android.awesomeapp.util.QueryUtils
import kotlinx.android.synthetic.main.activity_projects.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


class ProjectsActivity : MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        val intent = intent
        val track = intent.getStringExtra(TABLE_WITH_DATA)

        myToolbar.title = track
        setSupportActionBar(myToolbar)

        val rv = findViewById<RecyclerView>(R.id.projectsList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        var projects = QueryUtils.getProjects(track) ?: ArrayList()

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
    }
}
