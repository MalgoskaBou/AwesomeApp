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
import com.awesomeapp.android.awesomeapp.adapters.UserAdapter
import com.awesomeapp.android.awesomeapp.data.Constant.CURRENT_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_1
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_2
import com.awesomeapp.android.awesomeapp.data.Constant.SLACK_NAME
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.myUsers
import com.awesomeapp.android.awesomeapp.model.UserModel
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : MenuActivity() {

    val users = ArrayList<UserModel>()
    var adapter = UserAdapter(users)
    lateinit var lastVisible: DocumentSnapshot
    lateinit var prewVisible: DocumentSnapshot
    lateinit var projectNameExtra: String
    lateinit var rv: RecyclerView

    val HOW_MUCH_TO_CHARGE = 10L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val intent = intent;
        projectNameExtra = intent.getStringExtra(WHICH_PROJECT)

        projectNameTxt.text = projectNameExtra
        noOneWorkCurrently.visibility = View.GONE

        rv = findViewById(R.id.usersList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        loadInitialData()

    }

    private fun loadInitialData(){

        val query = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra).limit(HOW_MUCH_TO_CHARGE)

        query.addSnapshotListener(this, { snapshots, e ->
            if (snapshots?.size()!! > 0) {
                users.clear()
                for (document in snapshots) {
                    val languagesToDisplay = "${document.get(LANGUAGE_1)}, ${document.get(LANGUAGE_2)}"
                    users.add(UserModel(document.get(SLACK_NAME).toString(), languagesToDisplay))
                }
                rv.adapter = adapter
            } else {
                progressBar.visibility = View.GONE
                noOneWorkCurrently.visibility = View.VISIBLE
                //TODO check what will happen when there will be no connection to the internet
                Log.e("Event ", e.toString())
            }
        })
    }


}
