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
import android.widget.Toast
import com.awesomeapp.android.awesomeapp.adapters.UserAdapter
import com.awesomeapp.android.awesomeapp.data.Constant.ABND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.AND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.CURRENT_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.FEND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_1
import com.awesomeapp.android.awesomeapp.data.Constant.MWS_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.SLACK_NAME
import com.awesomeapp.android.awesomeapp.data.Constant.TRACK
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_TRACT
import com.awesomeapp.android.awesomeapp.data.Constant.myUsers
import com.awesomeapp.android.awesomeapp.model.UserModel

class DetailsActivity : MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val intent = intent;
        val projectNameExtra = intent.getStringExtra(WHICH_PROJECT)

        projectNameTxt.text = projectNameExtra

        /* PROBABLY TO DELETE !!

        val trackExtra = intent.getStringExtra(WHICH_TRACT)
        val trackNameToCompare: String =
                when (trackExtra) {
                    AND_PROJECTS -> "AND"
                    MWS_PROJECTS -> "MWS"
                    ABND_PROJECTS -> "ABND"
                    FEND_PROJECTS -> "FEND"
                    else -> "unknow"
                }
        //val numberOfCurrentProjToCompare: String = CURRENT_PROJECT[1] as String
        */

        val users = ArrayList<UserModel>()
        var adapter = UserAdapter(users)

        val rv = findViewById<RecyclerView>(R.id.usersList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val query = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra)


        query.addSnapshotListener(this, { snapshots, e ->
            if(snapshots?.size()!! >0){
                users.clear()
                for(document in snapshots){
                    users.add(UserModel(document.get(SLACK_NAME).toString(), "During work", document.get(LANGUAGE_1).toString()))
                }
                rv.adapter = adapter
            } else {
                Toast.makeText(this, "Something wrong :(", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
