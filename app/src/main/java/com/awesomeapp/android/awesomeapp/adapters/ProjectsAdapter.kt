package com.awesomeapp.android.awesomeapp.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awesomeapp.android.awesomeapp.DetailsActivity
import com.awesomeapp.android.awesomeapp.R
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_TRACT
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import kotlinx.android.synthetic.main.activity_details.view.*


class ProjectsAdapter (val projectsList: ArrayList<ProjectsModel>, val context: Context, val whihTrack: String): RecyclerView.Adapter<ProjectsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.projects_element, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return projectsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.projectNameTxt?.text = projectsList[position].projName
        holder?.deadLineTxt?.text = projectsList[position].deadline
        holder?.percentOfUsersTxt?.text = projectsList[position].percentOfUsers

        val intentToDetails = Intent(context, DetailsActivity::class.java)

        holder.itemView.setOnClickListener{
            intentToDetails.putExtra(WHICH_PROJECT, projectsList[position].projName)
            intentToDetails.putExtra(WHICH_TRACT, whihTrack)
            context.startActivity(intentToDetails)
        }
    }

    inner class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val projectNameTxt = itemView.projectNameTxt
        val deadLineTxt = itemView.deadLineTxt
        val percentOfUsersTxt = itemView.percentOfUsersTxt
    }
}