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

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.awesomeapp.android.awesomeapp.adapters.UserAdapter
import com.awesomeapp.android.awesomeapp.data.Constant
import com.awesomeapp.android.awesomeapp.data.Constant.CURRENT_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_1
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_2
import com.awesomeapp.android.awesomeapp.data.Constant.SLACK_NAME
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.myHelpData
import com.awesomeapp.android.awesomeapp.data.Constant.myUsers
import com.awesomeapp.android.awesomeapp.model.UserModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

private const val HOW_MUCH_TO_CHARGE = 1L

class DetailsActivity : MenuActivity() {

    private val users = ArrayList<UserModel>()
    private var adapter = UserAdapter(users)
    private lateinit var lastVisible: DocumentSnapshot
    private var positionOnList: Int = 0
    private lateinit var projectNameExtra: String
    private lateinit var rv: RecyclerView
    private var myProgressBar: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(myToolbar)

        val intent = intent
        projectNameExtra = intent.getStringExtra(WHICH_PROJECT)

        projectNameTxt.text = projectNameExtra
        noOneWorkCurrently.visibility = View.GONE
        myProgressBar = indeterminateProgressDialog("Wait for data loading")
        myProgressBar?.show()

        rv = findViewById(R.id.usersList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        initialiseLanguages()

        loadUsers(null)

        //TODO make loading of new users dependent on the recyclerView scroll position or use -> SwipeRefreshLayout!!!
        loadMoreData.setOnClickListener {
            loadMoreUsers()
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                clearUsers()
                //TODO do both filter better, it's ugly
                loadUsers(LANGUAGE_1)
                loadUsers(LANGUAGE_2)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // do nothing
            }
        }

    }

    private fun initialiseLanguages() {
        myHelpData.addSnapshotListener(this, EventListener<DocumentSnapshot> { snapshots, e ->
            if (e != null) {
                Log.w("error - ", e)
                toast("Error :(")
                return@EventListener

            } else if (snapshots!!.exists()) {
                @Suppress("UNCHECKED_CAST")
                val langTable = snapshots[Constant.LANG_TABLE] as java.util.ArrayList<String>
                langTable.add(0, getString(R.string.noLanguageFilter))

                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, langTable)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                languageSpinner.adapter = spinnerAdapter

            }
        })
    }

    /**
     * Load the users currently working on the selected project
     */
    private fun loadUsers(filterName: String?) {

        var query = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra).limit(HOW_MUCH_TO_CHARGE)

        // if a language is selected, we filter on this
        if (languageSpinner.selectedItemPosition > 0) {
            if (LANGUAGE_1 == filterName) {
                query = query.whereEqualTo(LANGUAGE_1, languageSpinner.selectedItem)
            } else if (LANGUAGE_2 == filterName) {
                query = query.whereEqualTo(LANGUAGE_2, languageSpinner.selectedItem)
            }
        }

        query.addSnapshotListener(this, { snapshots, e ->
            if (snapshots?.size()!! > 0) {
                for (document in snapshots) {
                    val languagesToDisplay = "${document.get(LANGUAGE_1)}, ${document.get(LANGUAGE_2)}"
                    users.add(UserModel(document.get(SLACK_NAME).toString(), languagesToDisplay))
                }
                myProgressBar?.dismiss()

                rv.adapter = adapter

                lastVisible = snapshots.documents[snapshots.size() - 1]
                positionOnList += snapshots.size()

            } else {

                myProgressBar?.dismiss()
                noOneWorkCurrently.visibility = View.VISIBLE

                //TODO check what will happen when there will be no connection to the internet
                Log.e("Event ", e.toString())
            }
        })
    }

    /**
     * Load more users
     */
    private fun loadMoreUsers() {
        val newQuery = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra).startAfter(lastVisible).limit(HOW_MUCH_TO_CHARGE)
        newQuery.addSnapshotListener(this, { snapshots, e ->
            if (snapshots?.size()!! > 0) {

                for (document in snapshots) {
                    val languagesToDisplay = "${document.get(LANGUAGE_1)}, ${document.get(LANGUAGE_2)}"
                    users.add(positionOnList, UserModel(document.get(SLACK_NAME).toString(), languagesToDisplay))
                }
                myProgressBar?.dismiss()
                rv.adapter = adapter

                lastVisible = snapshots.documents[snapshots.size() - 1]
                positionOnList += snapshots.size()

            } else {
                myProgressBar?.dismiss()
                toast("Nothing more to load")
                Log.e("Event ", e.toString())
            }
        })
    }

    /**
     * Clean the activity before a reload of the users
     */
    private fun clearUsers() {
        noOneWorkCurrently.visibility = View.GONE
        users.clear()
        rv.removeAllViews()
    }
}
