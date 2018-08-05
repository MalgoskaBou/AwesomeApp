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

//@file:Suppress("DEPRECATION")

package com.awesomeapp.android.awesomeapp

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
import com.google.firebase.firestore.*
import com.google.firebase.firestore.FieldPath.documentId
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.toast

class DetailsActivity : MenuActivity(), ProjectRefreshable {

    private var users: ArrayList<UserModel> = ArrayList()
    private var adapter = UserAdapter(users)
    private var lastVisible: DocumentSnapshot? = null
    private lateinit var projectNameExtra: String
    private lateinit var rv: RecyclerView
    private lateinit var swipeLayout: SwipyRefreshLayout
    private var isLoading = false
    private var isAllLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(myToolbar)

        with(intent) {
            projectNameExtra = getStringExtra(WHICH_PROJECT)
            deadLineTxt.text = getStringExtra(WHICH_DEADLINE)
            nbOfUsersTxt.text = getStringExtra(WHICH_NB_USERS)
        }

        projectNameTxt.text = projectNameExtra

        noOneWorkCurrently.visibility = View.GONE

        rv = findViewById(R.id.usersList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rv.adapter = adapter

        initialiseLanguages()

        swipeLayout = findViewById(R.id.swipyrefreshlayout)
        swipeLayout.setOnRefreshListener {
            loadMoreUsers()
        }

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
        getUsers(query)
    }

    /**
     * Load more users
     */
    private fun loadMoreUsers() {
        if (lastVisible != null && !isAllLoaded) {
            isLoading = true
            if (languageSpinner.selectedItemPosition > 0) {
                val lang = languageSpinner.selectedItem.toString()

                val query = FirebaseFirestore.getInstance().collection("UsersByLanguage")
                        .whereGreaterThanOrEqualTo(documentId(), lang)
                        .whereLessThan(documentId(), lang + "a")
                        .whereEqualTo("project", projectNameExtra)

                getUsers(query)
            } else {
                val query = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra)
                getUsers(query)
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
        //First we should get the first and last documents of the query
        var tmpQuery = query
        if (lastVisible != null) {
            tmpQuery = tmpQuery.startAfter(lastVisible!!)
        }
        tmpQuery.limit(QueryUtils.getGetLimit())
                .get().addOnSuccessListener {
                    if (it.documents.isNotEmpty()) {
                        val firstItem = it.documents[0]
                        val lastItem = it.documents[it.documents.size - 1]

                        if (lastVisible == null) {
                            //We should listen before the first item in case a new user is inserted
                            query.endBefore(firstItem)
                                    .addSnapshotListener(this, usersListener)
                        } else {
                            // We should listen between previous user and new first user
                            query.startAfter(lastVisible!!)
                                    .endBefore(firstItem)
                                    .addSnapshotListener(this, usersListener)
                        }

                        // We memorise the last visible user
                        lastVisible = lastItem

                        query.startAt(firstItem).endAt(lastItem)
                                .addSnapshotListener(this, usersListener)
                    } else {
                        indeterminateProgressBar.visibility = View.INVISIBLE
                        if (lastVisible != null) {
                            // We should listen after the first item in case a new user is inserted
                            query.startAfter(lastVisible!!)
                                    .addSnapshotListener(this, usersListener)
                            isAllLoaded = true
                        }

                        toast(getString(R.string.nothingToLoad))
                        swipeLayout.isRefreshing = false
                        isLoading = false

                        if (users.size == 0) {
                            noOneWorkCurrently.visibility = View.VISIBLE
                        }
                    }
                }
                .addOnFailureListener {
                    toast(getString(R.string.errorLoading))
                    swipeLayout.isRefreshing = false
                    Log.e("Event ", it.toString())
                    isLoading = false
                }
    }

    private val usersListener = EventListener<QuerySnapshot> { snapshots, _ ->
        if (snapshots != null && snapshots.documentChanges.size > 0) {
            for (dc in snapshots.documentChanges) {
                val user =
                        if (languageSpinner.selectedItemPosition > 0) {
                            UserModel(dc.document["project"] as String? ?: ""
                                    , dc.document["languages"] as String? ?: ""
                                    , dc.document["slackName"] as String? ?: "", "")
                        } else {
                            dc.document.toObject(UserModel::class.java)
                        }
                Log.d(DetailsActivity::class.simpleName, "Process User $user")
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        Log.d(DetailsActivity::class.simpleName, "Add User")
                        users.add(user)
                        rv.adapter.notifyItemInserted(users.size - 1)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        Log.d(DetailsActivity::class.simpleName, "Modify User")
                        val listUser = users.filter { it.slackName == dc.document["slackName"] }
                        if (listUser.isNotEmpty()) {
                            val index = users.indexOf(listUser[0])
                            users.removeAt(index)
                            users.add(index, user)
                        }
                        rv.adapter.notifyDataSetChanged()
                    }
                    DocumentChange.Type.REMOVED -> {
                        Log.d(DetailsActivity::class.simpleName, "Remove User")
                        val index = users.indexOf(user)
                        users.removeAt(index)
                        rv.adapter.notifyItemRemoved(index)
                        rv.adapter.notifyItemRangeChanged(index, users.size)
                    }
                }
            }
            isLoading = false
        } else {
//            Never goes there as size is checked before
        }
        indeterminateProgressBar.visibility = View.INVISIBLE
        swipeLayout.isRefreshing = false
    }

    /**
     * Add the listener to the lang spinner
     */
    private fun addChangeLangListener() {
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View
                                        , position: Int, id: Long) = filter()

            override fun onNothingSelected(parentView: AdapterView<*>) {/* do nothing */
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

        lastVisible = null
        isAllLoaded = false

        // If a language is selected
        if (languageSpinner.selectedItemPosition > 0) {
            val lang = languageSpinner.selectedItem.toString()

            val query = FirebaseFirestore.getInstance().collection("UsersByLanguage")
                    .whereGreaterThanOrEqualTo(documentId(), lang)
                    .whereLessThan(documentId(), lang + "a")
                    .whereEqualTo("project", projectNameExtra)
                    .limit(QueryUtils.getGetLimit())

            getUsers(query)
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
