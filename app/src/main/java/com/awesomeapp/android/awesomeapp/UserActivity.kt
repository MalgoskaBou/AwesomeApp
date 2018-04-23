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

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
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
import com.awesomeapp.android.awesomeapp.model.MyUser
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.*
import kotlin.collections.HashMap


//flag for registered user
private const val RC_SIGN_IN = 1

class UserActivity : AppCompatActivity() {

    //hooks to database
    private lateinit var mAuth: FirebaseAuth
    private lateinit var myDatabase: FirebaseFirestore
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var myHelpData: DocumentReference
    private lateinit var myUserData: DocumentReference

    //get user data from user panel
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userUid: String

    private val myUser: MyUser? = MyUser()

    //spinner adapters
    private lateinit var spinnerAdapterTracks: ArrayAdapter<String>
    private lateinit var spinnerAdapterLanguages1: ArrayAdapter<String>
    private lateinit var spinnerAdapterLanguages2: ArrayAdapter<String>
    private val spinnerAdapterProjects: HashMap<String, ArrayAdapter<String>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(myToolbar)

        //get database hook
        myDatabase = FirebaseFirestore.getInstance()
        //get helpData->tracks document
        myHelpData = myDatabase.document("helpData/tracks")
        //get access to users data
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
        saveBtn.setOnClickListener { _ ->
            saveUser()
        }
        logOutBtn.setOnClickListener { _ ->

            alert("are you sure that you want log out?") {
                title = "Log out"
                yesButton { logOutUser() }
                noButton { }
            }.show()
        }

        deleteUserButton.setOnClickListener {
            alert("are you sure that you want delete your account?") {
                title = "Delete account"
                yesButton { deleteUser() }
                noButton { }
            }.show()
        }
    }

    private fun saveUser() {

        //create hashMap with necessary user data what I want to put to database
        val userData = HashMap<String, Any>()
        userData[USER_NAME] = userName
        userData[USER_EMAIL] = userEmail
        if (slackNick.text == null || slackNick.text.toString().trim() == "")
            userData[SLACK_NAME] = "undefined"
        else
            userData[SLACK_NAME] = slackNick.text.toString()

        if (lang1Spinner.selectedItemPosition > 0) {
            userData[LANGUAGE_1] = lang1Spinner.selectedItem.toString()
        }

        if (lang2Spinner.selectedItemPosition > 0) {
            userData[LANGUAGE_2] = lang2Spinner.selectedItem.toString()
        }

        if (trackSpinner.selectedItemPosition > 0) {
            userData[TRACK] = trackSpinner.selectedItem.toString()
        }

        if (projectsSpinner.selectedItemPosition > 0) {
            userData[CURRENT_PROJECT] = projectsSpinner.selectedItem.toString()
        }

        //put userdata to database (path to myUserdata is declared in userLogIn function)
        myUserData.set(userData).addOnSuccessListener({
            Toast.makeText(this@UserActivity, "Data saved", Toast.LENGTH_SHORT).show()

        }).addOnFailureListener {
            Toast.makeText(this@UserActivity, "Something wrong :( try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteUser() {

        val currentUser = mAuth.currentUser

        currentUser!!.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result.token!!

                        val credential = if (token.isEmpty()) {
                            val userPassword = EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD
                            EmailAuthProvider.getCredential(userEmail, userPassword)
                        } else {
                            //Doesn't matter if it was Facebook Sign-in or others. It will always work using GoogleAuthProvider for whatever the provider.
                            GoogleAuthProvider.getCredential(token, null)
                        }

                        currentUser.reauthenticate(credential)
                                .addOnCompleteListener({

                                    //Delete data from database
                                    myUserData.delete()

                                    //Calling delete to remove the user and wait for a result.
                                    currentUser.delete().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {

                                            Toast.makeText(this@UserActivity, "User deleted successfully", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    startActivity(Intent(this@UserActivity, MainActivity::class.java))
                                    finish()
                                })
                    }
                }
    }

    private fun logOutUser() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            startActivity(Intent(this@UserActivity, MainActivity::class.java))
            finish()
        }
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
                myUserData = myDatabase.document("Users/$userUid")

                //fetch data from database
                fetchUserData()

                //TODO save a new user with empty fields to the database immediately after registration

                welcomeText.text = getString(R.string.welcome_message, userName, userEmail)
            } else {
                // User is signed out - show login screen
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

    private fun fetchUserData() {

        //get data from user collection
        myUserData.addSnapshotListener({ snapshot, _ ->

            if (snapshot?.exists()!!) {
                slackNick.setText(snapshot.getString(SLACK_NAME))
                myUser!!.slackNick = snapshot.getString(SLACK_NAME)

                myUser.currentProject = snapshot.getString(CURRENT_PROJECT)
                myUser.language1 = snapshot.getString(LANGUAGE_1)
                myUser.language2 = snapshot.getString(LANGUAGE_2)
                myUser.track = snapshot.getString(TRACK)

                updateUserData()
            } else {
                toast("Choose your patch and current project")
                saveUser()
            }
        })
    }

    private fun updateUserData() {
        //take data in correct order
        val spinnerPositionLang1 = spinnerAdapterLanguages1.getPosition(myUser!!.language1)
        val spinnerPositionLang2 = spinnerAdapterLanguages2.getPosition(myUser.language2)
        val spinnerPositionTracks = spinnerAdapterTracks.getPosition(myUser.track)

        //for tracks
        trackSpinner.setSelection(spinnerPositionTracks)

        //lang1
        lang1Spinner.setSelection(spinnerPositionLang1)

        //lang2
        lang2Spinner.setSelection(spinnerPositionLang2)

    }

    private fun getDataFromDatabase() {

        //get helpData (snapshotListener allow synchronize data in real time)
        myHelpData.addSnapshotListener(this, EventListener<DocumentSnapshot> { snapshots, e ->
            if (e != null) {
                Log.w("error - ", e)
                toast("Error :(")
                return@EventListener

            } else if (snapshots!!.exists()) {
                @Suppress("UNCHECKED_CAST")
                val tracksTable = snapshots[TRACKS_ARRAY] as ArrayList<String>
                tracksTable.sort()
                tracksTable.add(0, getString(R.string.selectTrack))

                @Suppress("UNCHECKED_CAST")
                val langTable = snapshots[LANG_TABLE] as ArrayList<String>
                langTable.sort()
                langTable.add(0, getString(R.string.selectLanguage))

                @Suppress("UNCHECKED_CAST")
                val andProjTable = snapshots[AND_PROJECTS] as ArrayList<String>
                andProjTable.sort()
                andProjTable.add(0, getString(R.string.selectProject))

                @Suppress("UNCHECKED_CAST")
                val mwsProjTable = snapshots[MWS_PROJECTS] as ArrayList<String>
                mwsProjTable.sort()
                mwsProjTable.add(0, getString(R.string.selectProject))

                @Suppress("UNCHECKED_CAST")
                val abndProjTable = snapshots[ABND_PROJECTS] as ArrayList<String>
                abndProjTable.sort()
                abndProjTable.add(0, getString(R.string.selectProject))

                @Suppress("UNCHECKED_CAST")
                val fendProjTable = snapshots[FEND_PROJECTS] as ArrayList<String>
                fendProjTable.sort()
                fendProjTable.add(0, getString(R.string.selectProject))


                spinnerAdapterProjects["AND"] = ArrayAdapter(applicationContext,
                        android.R.layout.simple_spinner_item, andProjTable)
                spinnerAdapterProjects["ABND"] = ArrayAdapter(applicationContext,
                        android.R.layout.simple_spinner_item, abndProjTable)
                spinnerAdapterProjects["MWS"] = ArrayAdapter(applicationContext,
                        android.R.layout.simple_spinner_item, mwsProjTable)
                spinnerAdapterProjects["FEND"] = ArrayAdapter(applicationContext,
                        android.R.layout.simple_spinner_item, fendProjTable)

                //I add a table taken from the database to the appropriate spinners
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

                // Here we have all the spinner data available, we can now fill connect the user
                updateUserData()
            }
        })
    }

    private fun updateProjectSpinner(selectedTrack: String) {

        val selectedSpinnerAdapterProjects = spinnerAdapterProjects[selectedTrack]
        if (selectedSpinnerAdapterProjects != null) {
            selectedSpinnerAdapterProjects.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            projectsSpinner.adapter = selectedSpinnerAdapterProjects

            //If the user is logged, we should try to select the right project
            val spinnerPositionProjects = selectedSpinnerAdapterProjects
                    .getPosition(myUser!!.currentProject)
            projectsSpinner.setSelection(spinnerPositionProjects)
        } else {
            projectsSpinner.adapter = null
        }
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