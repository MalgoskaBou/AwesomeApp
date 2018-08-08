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

package com.awesomeapp.android.awesomeapp.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.awesomeapp.android.awesomeapp.DetailsActivity
import com.awesomeapp.android.awesomeapp.R
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_DEADLINE
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_NB_USERS
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_PROJECT
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import kotlinx.android.synthetic.main.activity_details.view.*
import java.text.SimpleDateFormat
import java.util.*

class ProjectsAdapter(val projectsList: ArrayList<ProjectsModel>, private val context: Context)
    : RecyclerView.Adapter<ProjectsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.projects_element, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = projectsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dateFormatter = SimpleDateFormat("MMM d yyyy", Locale.getDefault())
        val formatedData = "${context.resources.getString(R.string.deadline)} ${dateFormatter.format(projectsList[position].deadline).toUpperCase()}"

        holder.projectNameTxt?.text = projectsList[position].name
        holder.deadLineTxt?.text = formatedData
        holder.nbOfUsersTxt?.text = projectsList[position].nbUsers.toString()

        val intentToDetails = Intent(context, DetailsActivity::class.java)

        holder.itemView.setOnClickListener {
            with(intentToDetails) {
                putExtra(WHICH_PROJECT, projectsList[position].name)
                putExtra(WHICH_DEADLINE, formatedData)
                putExtra(WHICH_NB_USERS, projectsList[position].nbUsers.toString())
            }

            context.startActivity(intentToDetails)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectNameTxt: TextView? = itemView.projectNameTxt
        val deadLineTxt: TextView? = itemView.deadLineTxt
        val nbOfUsersTxt: TextView? = itemView.nbOfUsersTxt
    }
}