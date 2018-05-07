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

@file:Suppress("DEPRECATION")

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
import com.awesomeapp.android.awesomeapp.data.Constant.CURRENT_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_DEADLINE
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_NB_USERS
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.myUsers
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import com.awesomeapp.android.awesomeapp.model.UserModel
import com.awesomeapp.android.awesomeapp.util.QueryUtils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath.documentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

class DetailsActivity : MenuActivity(), ProjectRefreshable {

    private var users: ArrayList<UserModel> = ArrayList()
    private var adapter = UserAdapter(users)
    private var lastVisible: DocumentSnapshot? = null
    private lateinit var projectNameExtra: String
    private lateinit var rv: RecyclerView
    private var myProgressBar: ProgressDialog? = null
    private lateinit var swipeLayout: SwipyRefreshLayout
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(myToolbar)

        val intent = intent
        projectNameExtra = intent.getStringExtra(WHICH_PROJECT)
        val deadlineExtra = intent.getStringExtra(WHICH_DEADLINE)
        val nbUsersExtra = intent.getStringExtra(WHICH_NB_USERS)

        projectNameTxt.text = projectNameExtra
        deadLineTxt.text = deadlineExtra
        nbOfUsersTxt.text = nbUsersExtra

        noOneWorkCurrently.visibility = View.GONE
        myProgressBar = indeterminateProgressDialog(getString(R.string.waitingMessage))
        myProgressBar?.show()

        rv = findViewById(R.id.usersList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rv.adapter = adapter

        initialiseLanguages()

        swipeLayout = findViewById(R.id.swipyrefreshlayout)
        swipeLayout.setOnRefreshListener({
            loadMoreUsers()
        })

        QueryUtils.addProjectRefreshableActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        QueryUtils.removeProjectRefreshableActivity(this)
    }

    /**
     * Get all the languages
     */
    private fun initialiseLanguages() {
        val langTable = QueryUtils.getStringLanguages()
        langTable.add(0, getString(R.string.noLanguageFilter))

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, langTable)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = spinnerAdapter

        addChangeLangListener()
    }

    /**
     * Load the users currently working on the selected project
     */
    private fun loadUsers() {
        isLoading = true
        val query = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra)
                .limit(QueryUtils.getGetLimit())
        getUsers(query)
    }

    /**
     * Load more users
     */
    private fun loadMoreUsers() {
        if (lastVisible != null) {

            if (languageSpinner.selectedItemPosition > 0) {
                val lang = languageSpinner.selectedItem.toString()

                val query = FirebaseFirestore.getInstance().collection("UsersByLanguage")
                        .whereGreaterThanOrEqualTo(documentId(), lang)
                        .whereLessThan(documentId(), lang + "a")
                        .whereEqualTo("project", projectNameExtra)
                        .startAfter(lastVisible!!)
                        .limit(QueryUtils.getGetLimit())

                getUsersFiltered(query)
            } else {
                val newQuery = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra)
                        .startAfter(lastVisible!!).limit(QueryUtils.getGetLimit())
                getUsers(newQuery)
            }
        } else {
            toast(getString(R.string.nothingToLoad))
            swipeLayout.isRefreshing = false
        }
    }

    /**
     * Get the users from the database
     */
    private fun getUsers(query: Query) {
        query.addSnapshotListener(this, { snapshots, e ->
            if (snapshots?.size()!! > 0) {
                for (document in snapshots) {
                    val user = document.toObject(UserModel::class.java)

                    rv.removeAllViews()
                    users.add(user)
                }

                lastVisible = snapshots.documents[snapshots.size() - 1]
                isLoading = false
            } else {
                toast(getString(R.string.nothingToLoad))
                swipeLayout.isRefreshing = false
                Log.e("Event ", e.toString())
                isLoading = false
            }
            myProgressBar?.dismiss()
            swipeLayout.isRefreshing = false

            if (users.size == 0) {
                noOneWorkCurrently.visibility = View.VISIBLE
            }
        })
    }

    /**
     * Get the users from the database filtered by language
     */
    private fun getUsersFiltered(query: Query) {
        myProgressBar?.show()

        query.addSnapshotListener(this, { snapshots, e ->
            if (snapshots?.size()!! > 0) {
                for (document in snapshots) {
                    Log.d(DetailsActivity::class.simpleName, "User $document.id")

                    val user = UserModel(document["project"] as String? ?: ""
                            , document["languages"] as String? ?: ""
                            , document["slackName"] as String? ?: "", "")

                        rv.removeAllViews()
                        users.add(user)
                }
                lastVisible = snapshots.documents[snapshots.size() - 1]
            } else {
                if (users.size == 0) {
                    noOneWorkCurrently.visibility = View.VISIBLE
                }
                toast(getString(R.string.nothingToLoad))
                swipeLayout.isRefreshing = false
                Log.e("Event ", e.toString())
            }
            myProgressBar?.dismiss()
            swipeLayout.isRefreshing = false
        })
    }

    /**
     * Add the listener to the lang spinner
     */
    private fun addChangeLangListener() {
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                filter()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // do nothing
            }
        }
    }

    /**
     * Filter users on lang
     */
    private fun filter() {
        if (!isLoading) {
            clearUsers()
        }

        // If a language is selected
        if (languageSpinner.selectedItemPosition > 0) {
            val lang = languageSpinner.selectedItem.toString()

            val query = FirebaseFirestore.getInstance().collection("UsersByLanguage")
                    .whereGreaterThanOrEqualTo(documentId(), lang)
                    .whereLessThan(documentId(), lang + "a")
                    .whereEqualTo("project", projectNameExtra)
                    .limit(QueryUtils.getGetLimit())

            getUsersFiltered(query)
        } else {
            loadUsers()
        }
    }

    /**
     * Clean the activity before a reload of the users
     */
    private fun clearUsers() {
        noOneWorkCurrently.visibility = View.GONE
        users.clear()
        rv.removeAllViews()
    }

    override fun refreshUI(p: ProjectsModel) {
        if (p.name == projectNameExtra) {
            nbOfUsersTxt.text = p.nbUsers.toString()
        }
    }
}
