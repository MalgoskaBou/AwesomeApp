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
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import com.awesomeapp.android.awesomeapp.data.Constant.ABND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.AND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.CURRENT_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.FEND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_1
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE_2
import com.awesomeapp.android.awesomeapp.data.Constant.LANG_TABLE
import com.awesomeapp.android.awesomeapp.data.Constant.MWS_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.SLACK_NAME
import com.awesomeapp.android.awesomeapp.data.Constant.TRACK
import com.awesomeapp.android.awesomeapp.data.Constant.TRACKS_ARRAY
import com.awesomeapp.android.awesomeapp.data.Constant.USER_EMAIL
import com.awesomeapp.android.awesomeapp.data.Constant.USER_NAME
import com.awesomeapp.android.awesomeapp.data.Constant.myHelpData
import com.awesomeapp.android.awesomeapp.model.MyUser
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*
import kotlin.collections.HashMap

class UserActivity : AppCompatActivity() {

    //hooks to database
    lateinit var mAuth: FirebaseAuth
    lateinit var myDatabase: FirebaseFirestore
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    lateinit var myUserData: DocumentReference

    //get user data from user panel
    lateinit var userName: String
    lateinit var userEmail: String
    lateinit var userUid: String

    private val myUser: MyUser? = MyUser()

    //flag for registered user
    val RC_SIGN_IN = 1

    //spinner adapters
    lateinit var spinnerAdapterTracks: ArrayAdapter<String>
    lateinit var spinnerAdapterLanguages1: ArrayAdapter<String>
    lateinit var spinnerAdapterLanguages2: ArrayAdapter<String>
    private val spinnerAdapterProjects: HashMap<String, ArrayAdapter<String>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        setSupportActionBar(myToolbar)

        //get database hook
        myDatabase = FirebaseFirestore.getInstance()

        //get access to users data - I do it here and not in const because here the user is definitely logged in
        mAuth = FirebaseAuth.getInstance()

        // Set the listeners
        trackSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {

                updateProjectSpinner(trackSpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // do nothing
            }
        }

        //GET DATA FROM DATABASE - helpData
        getDataFromDatabase()

        //DATABASE LOG IN -
        userLogIn()

        //Put user data to database
        saveBtn.setOnClickListener {
            saveUser()
        }
        logOutBtn.setOnClickListener{
            usrLogOut()
        }
    }

    private fun saveUser() {

        //create hashmap with nescessary user data what I want to put to databse
        val userData = HashMap<String, Any>()
        userData[USER_NAME] = userName
        userData[USER_EMAIL] = userEmail
        if (slackNick.text == null || slackNick.text.toString().trim() == "")
            userData[SLACK_NAME] = "undefined"
        else
            userData[SLACK_NAME] = slackNick.text.toString()

        userData[LANGUAGE_1] = lang1Spinner.selectedItem.toString()
        userData[LANGUAGE_2] = lang2Spinner.selectedItem.toString()
        userData[TRACK] = trackSpinner.selectedItem.toString()

        //Try to save project data in table [project name, project position on the list] - and retrive directly from position
        var arrayProjectNameAndPosition = listOf(projectsSpinner.selectedItem.toString(), projectsSpinner.selectedItemPosition)
        userData[CURRENT_PROJECT] = arrayProjectNameAndPosition //projectsSpinner.selectedItem.toString()

        //put userdata to database (path to myUserdata is declared in userLogIn function)
        myUserData.set(userData).addOnSuccessListener({
            Toast.makeText(this@UserActivity, "Data saved", Toast.LENGTH_SHORT).show()

        }).addOnFailureListener {
            Toast.makeText(this@UserActivity, "Someting wrong :( try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun usrLogOut(){
        mAuth.signOut()
    }

    private fun userLogIn() {

        //check if user is login
        mAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User is signed in - get data from log in
                userName = user.displayName!!
                userEmail = user.email!!
                userUid = user.uid

                //get document with actual user from database
                myUserData = myDatabase.document("UsersCopyForTest/$userUid")

                //fetch data from database
                fetchUserData()

                welcomeText.text = getString(R.string.welcome_message, userName, userEmail)
            } else {
                // User is signed out - show loggin screen
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(
                                        Arrays.asList(AuthUI.IdpConfig.EmailBuilder().build(),
                                                AuthUI.IdpConfig.GoogleBuilder().build()))
                                .build(),
                        RC_SIGN_IN)
            }
        }
    }

    private fun fetchUserData(){

        //get data from user collection
        myUserData.addSnapshotListener({ snapshot, e ->

            if (snapshot?.exists()!!) {
                slackNick.setText(snapshot.getString(SLACK_NAME))
                myUser!!.slackNick = snapshot.getString(SLACK_NAME)

                myUser.currentProject = snapshot.getString(CURRENT_PROJECT)
                myUser.language1 = snapshot.getString(LANGUAGE_1)
                myUser.language2 = snapshot.getString(LANGUAGE_2)
                myUser.track = snapshot.getString(TRACK)

                updateUserData()
            }else{
                Toast.makeText(this@UserActivity, "Someting wrong :( can't take your data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserData() {
        //take data in correct order
        //val spinnerPositionProjects = spinnerAdapterProjects.getPosition(snapshots.getString(CURRENT_PROJECT))
        val spinnerPositionLang1 = spinnerAdapterLanguages1.getPosition(myUser!!.language1)
        val spinnerPositionLang2 = spinnerAdapterLanguages2.getPosition(myUser.language2)
        val spinnerPositionTracks = spinnerAdapterTracks.getPosition(myUser.track)
        //Log.v("pozycja ","$spinnerPositionProjects")

        //for tracks
        trackSpinner.setSelection(spinnerPositionTracks)

        //lang1
        lang1Spinner.setSelection(spinnerPositionLang1)

        //lang2
        lang2Spinner.setSelection(spinnerPositionLang2)

        //projects
        //projectsSpinner.setSelection(spinnerPositionProjects)
    }

    private fun getDataFromDatabase() {

        //get helpData addOnCompleteListener - we dont need still synchronize data - this data does not change constantly - it will work faster
        myHelpData.addOnCompleteListener( { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {

                    //get data to arrays
                    val tracksTable = document.get(TRACKS_ARRAY) as ArrayList<String>
                    val langTable = document.get(LANG_TABLE) as ArrayList<String>

                    val andProjTable = document.get(AND_PROJECTS) as ArrayList<String>
                    val mwsProjTable = document.get(MWS_PROJECTS) as ArrayList<String>
                    val abndProjTable = document.get(ABND_PROJECTS) as ArrayList<String>
                    val fendProjTable = document.get(FEND_PROJECTS) as ArrayList<String>

                    spinnerAdapterProjects.set("AND", ArrayAdapter(applicationContext,
                            android.R.layout.simple_spinner_item, andProjTable))
                    spinnerAdapterProjects.set("ABND", ArrayAdapter(applicationContext,
                            android.R.layout.simple_spinner_item, abndProjTable))
                    spinnerAdapterProjects.set("MWS", ArrayAdapter(applicationContext,
                            android.R.layout.simple_spinner_item, mwsProjTable))
                    spinnerAdapterProjects.set("FEND", ArrayAdapter(applicationContext,
                            android.R.layout.simple_spinner_item, fendProjTable))

                    //I add a table taken from the database to the appropriate spiners
                    //Tracks list
                    spinnerAdapterTracks = ArrayAdapter(this, android.R.layout.simple_spinner_item, tracksTable)
                    spinnerAdapterTracks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    trackSpinner.adapter = spinnerAdapterTracks

                    //Languages lists (there are 2 fields for this) but need get position this why I create 2 adapters
                    spinnerAdapterLanguages1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, langTable)
                    spinnerAdapterLanguages1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    lang1Spinner.adapter = spinnerAdapterLanguages1

                    spinnerAdapterLanguages2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, langTable)
                    spinnerAdapterLanguages2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    lang2Spinner.adapter = spinnerAdapterLanguages2
                }
                // Here we have all the spinner data available, we can now fill connect the user
                updateUserData()
            }
        })
    }

    private fun updateProjectSpinner(selectedTrack: String) {

        val selectedSpinnerAdapterProjects = spinnerAdapterProjects[selectedTrack]
        selectedSpinnerAdapterProjects!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        projectsSpinner.adapter = selectedSpinnerAdapterProjects

        //If the user is logged, we should try to select the right project
        val spinnerPositionProjects = selectedSpinnerAdapterProjects
                .getPosition(myUser!!.currentProject)
        projectsSpinner.setSelection(spinnerPositionProjects)
    }

    override fun onResume() {

        super.onResume()
        //we're adding a listening to the user's login
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onPause() {

        super.onPause()
        mAuth.removeAuthStateListener(mAuthStateListener)
    }
}
