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
import com.awesomeapp.android.awesomeapp.data.Constant.CURRENT_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_1
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_2
import com.awesomeapp.android.awesomeapp.data.Constant.SLACK_NAME
import com.awesomeapp.android.awesomeapp.data.Constant.WHICH_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.myHelpData
import com.awesomeapp.android.awesomeapp.data.Constant.myUsers
import com.awesomeapp.android.awesomeapp.model.UserModel
import com.awesomeapp.android.awesomeapp.util.QueryUtils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath.documentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout
import org.jetbrains.anko.startActivity


private const val HOW_MUCH_TO_CHARGE = 1L

class DetailsActivity : MenuActivity() {

    private var users: ArrayList<UserModel> = ArrayList()
    //    private val allUsers = ArrayList<UserModel>()
//    private val usersByLang = HashMap<String, ArrayList<UserModel>?>()
    private var adapter = UserAdapter(users)
    private var lastVisible: DocumentSnapshot? = null
    private lateinit var projectNameExtra: String
    private lateinit var rv: RecyclerView
    private var myProgressBar: ProgressDialog? = null
    private lateinit var swipeLayout: SwipyRefreshLayout

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
        rv.adapter = adapter

        initialiseLanguages()

        loadUsers()

        swipeLayout = findViewById(R.id.swipyrefreshlayout)
        swipeLayout.setOnRefreshListener({
            loadMoreUsers()
        })
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

//        myHelpData.addSnapshotListener(this, EventListener<DocumentSnapshot> { snapshots, e ->
//            if (e != null) {
//                Log.w("error - ", e)
//                toast("Error :(")
//                return@EventListener
//            } else if (snapshots!!.exists()) {
//                @Suppress("UNCHECKED_CAST")
//                val langTable = snapshots[Constant.LANG_TABLE] as java.util.ArrayList<String>
//                langTable.sort()
//                langTable.add(0, getString(R.string.noLanguageFilter))
//
//                val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, langTable)
//                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                languageSpinner.adapter = spinnerAdapter
//
//                addChangeLangListener()
//            }
//        })
    }

    /**
     * Load the users currently working on the selected project
     */
    private fun loadUsers() {
        val query = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra).limit(HOW_MUCH_TO_CHARGE)
        getUsers(query)
    }

    /**
     * Load more users
     */
    private fun loadMoreUsers() {
        if (lastVisible != null) {
            //Remove the filter
            languageSpinner.setSelection(0)
            val newQuery = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra).startAfter(lastVisible!!).limit(HOW_MUCH_TO_CHARGE)
            getUsers(newQuery)
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
                    val languagesToDisplay = "${document.get(LANGUAGE_1)}, ${document.get(LANGUAGE_2)}"
                    val user = UserModel(document.get(SLACK_NAME).toString(), languagesToDisplay)

                    rv.removeAllViews()
                    users.add(user)
//                    allUsers.add(user)
//                    addUserByLang(user, document.get(LANGUAGE_1).toString())
//                    addUserByLang(user, document.get(LANGUAGE_2).toString())
                }

                lastVisible = snapshots.documents[snapshots.size() - 1]

            } else {
                toast(getString(R.string.nothingToLoad))
                swipeLayout.isRefreshing = false
                Log.e("Event ", e.toString())
            }
            myProgressBar?.dismiss()
            swipeLayout.isRefreshing = false


            if (users.size == 0) {
                noOneWorkCurrently.visibility = View.VISIBLE
            }
        })
    }

    /**
     * Add a user in the usersByLang map
     */
//    private fun addUserByLang(user: UserModel, lang: String) {
//        var usersLang = usersByLang[lang]
//        if (usersLang == null) {
//            usersLang = ArrayList()
//        }
//        usersLang.add(user)
//        usersByLang[lang] = usersLang
//    }

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
        clearUsers()

        // If a language is selected
        if (languageSpinner.selectedItemPosition > 0) {
            val lang = languageSpinner.selectedItem.toString()
//            // If there is users with this language
//            if (usersByLang[lang] != null && usersByLang[lang]!!.size > 0) {
//                users.addAll(usersByLang[lang]!!)
//            } else {
//                noOneWorkCurrently.visibility = View.VISIBLE
//            }
//        } else {
//            users.addAll(allUsers)
//            if (users.size == 0) {
//                noOneWorkCurrently.visibility = View.VISIBLE
//            }
//        }

            val query = FirebaseFirestore.getInstance().collection("UsersByLanguage")
                    .whereGreaterThanOrEqualTo(documentId(), lang)
                    .whereLessThan(documentId(), lang + "z")
                    .whereEqualTo("project", projectNameExtra)
                    .limit(HOW_MUCH_TO_CHARGE)
            query.addSnapshotListener(this, { snapshots, e ->
                if (snapshots?.size()!! > 0) {
                    for (document in snapshots) {
                        Log.d(DetailsActivity::class.simpleName, "User $document.id")
                        val userId = document.id.split("_")[1]
                        myUsers.document(userId).get().addOnSuccessListener({
                            Log.d(DetailsActivity::class.simpleName, "Get User ${it.id}")

                            val languagesToDisplay = "${it.get(LANGUAGE)}"
                            val user = UserModel(it.get(SLACK_NAME).toString(), languagesToDisplay)

                            rv.removeAllViews()
                            users.add(user)
//                        allUsers.add(user)
//                        addUserByLang(user, document.get(LANGUAGE_1).toString())
//                        addUserByLang(user, document.get(LANGUAGE_2).toString())

                        })
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
