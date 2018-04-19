package com.awesomeapp.android.awesomeapp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awesomeapp.android.awesomeapp.R
import com.awesomeapp.android.awesomeapp.model.UserModel
import kotlinx.android.synthetic.main.user_element.view.*

class UserAdapter (val usersList: ArrayList<UserModel>): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.user_element, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.slackName?.text = usersList[position].slackName
        holder?.workOrFinished?.text = usersList[position].projProgress
        holder?.languages?.text = usersList[position].userLanguages
    }

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val slackName = itemView.userSlackName
        val workOrFinished = itemView.duringWorkOrFinished
        val languages = itemView.languagesTxt
    }


}