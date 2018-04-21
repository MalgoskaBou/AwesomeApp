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
import kotlinx.android.synthetic.main.toolbar_layout.*

class DetailsActivity : MenuActivity() {

    val users = ArrayList<UserModel>()
    var adapter = UserAdapter(users)
    lateinit var lastVisible: DocumentSnapshot
    var positionOnList: Int = 0
    lateinit var projectNameExtra: String
    lateinit var rv: RecyclerView

    val HOW_MUCH_TO_CHARGE = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setSupportActionBar(myToolbar)

        val intent = intent;
        projectNameExtra = intent.getStringExtra(WHICH_PROJECT)

        projectNameTxt.text = projectNameExtra
        noOneWorkCurrently.visibility = View.GONE

        rv = findViewById(R.id.usersList)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        loadInitialData()

        loadMoreData.setOnClickListener{
            loadMoreData()
        }

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
                progressBar.visibility = View.GONE
                rv.adapter = adapter

                lastVisible = snapshots.documents[snapshots.size() -1]
                positionOnList += snapshots.size()

            } else {
                progressBar.visibility = View.GONE
                noOneWorkCurrently.visibility = View.VISIBLE

                //TODO check what will happend when there will be no connection to the internet
                Log.e("Event ", e.toString())
            }
        })
    }

    private fun loadMoreData(){
        val newQuery = myUsers.whereEqualTo(CURRENT_PROJECT, projectNameExtra).startAfter(lastVisible).limit(HOW_MUCH_TO_CHARGE)
        newQuery.addSnapshotListener(this, { snapshots, e ->
            if (snapshots?.size()!! > 0) {

                for (document in snapshots) {
                    val languagesToDisplay = "${document.get(LANGUAGE_1)}, ${document.get(LANGUAGE_2)}"
                    users.add(positionOnList, UserModel(document.get(SLACK_NAME).toString(), languagesToDisplay))
                }
                progressBar.visibility = View.GONE
                rv.adapter = adapter

                lastVisible = snapshots.documents[snapshots.size() -1]
                positionOnList += snapshots.size()

            } else {
                progressBar.visibility = View.GONE

                //TODO check what will happend when there will be no connection to the internet
                Log.e("Event ", e.toString())
            }
        })

    }
}
