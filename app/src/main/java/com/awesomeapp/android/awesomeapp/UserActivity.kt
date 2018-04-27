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
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import com.awesomeapp.android.awesomeapp.data.Constant.CURRENT_PROJECT
import com.awesomeapp.android.awesomeapp.data.Constant.LANGUAGE
import com.awesomeapp.android.awesomeapp.data.Constant.SLACK_NAME
import com.awesomeapp.android.awesomeapp.data.Constant.TRACK
import com.awesomeapp.android.awesomeapp.data.Constant.USER_EMAIL
import com.awesomeapp.android.awesomeapp.data.Constant.USER_NAME
import com.awesomeapp.android.awesomeapp.model.MyUser
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import com.awesomeapp.android.awesomeapp.util.QueryUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.*
import java.util.*
import kotlin.collections.HashMap


//flag for registered user
private const val RC_SIGN_IN = 1

class UserActivity : AppCompatActivity() {

    //hooks to database
    private lateinit var mAuth: FirebaseAuth
    private lateinit var myDatabase: FirebaseFirestore
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var myUserData: DocumentReference

    //get user data from user panel
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userUid: String

    private val myUser: MyUser = MyUser()

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

        //Fill spinner
        fillData()

        //DATABASE LOG IN -
        userLogIn()

        //Put user data to database
        saveBtn.setOnClickListener { _ ->
            saveUser()
        }

        logOutBtn.setOnClickListener { _ ->

            alert(getString(R.string.logoutConfirmation)) {
                title = getString(R.string.logout)
                yesButton { logOutUser() }
                noButton { }
            }.show()
        }

        deleteUserButton.setOnClickListener {
            alert(getString(R.string.deleteConfirmation)) {
                title = getString(R.string.deleteAccount)
                yesButton { deleteUser() }
                noButton { }
            }.show()
        }
    }

    private fun ifUserIsVerified() {

        if (!mAuth.currentUser?.isEmailVerified!!) {

            mAuth.currentUser?.sendEmailVerification()

            alert(getString(R.string.verifyEmail)) {
                positiveButton(getString(R.string.sendLink)) { mAuth.currentUser?.sendEmailVerification() }
                negativeButton(getString(R.string.refresh)) { mAuth.currentUser?.reload() }

            }.show()
        }
    }

    private fun saveUser() {

        //create hashMap with necessary user data what I want to put to database
        val userData = HashMap<String, Any>()
        userData[USER_NAME] = userName
        userData[USER_EMAIL] = userEmail
        if (slackNick.text == null || slackNick.text.toString().trim() == "")
            userData[SLACK_NAME] = getString(R.string.undefined)
        else
            userData[SLACK_NAME] = slackNick.text.toString()

        var lang1 = ""
        var lang2 = ""
        if (lang1Spinner.selectedItemPosition > 0) {
            lang1 = lang1Spinner.selectedItem.toString()
        }

        if (lang2Spinner.selectedItemPosition > 0) {
            lang2 = lang2Spinner.selectedItem.toString()
        }
        userData[LANGUAGE] = "$lang1,$lang2"

        if (trackSpinner.selectedItemPosition > 0) {
            userData[TRACK] = trackSpinner.selectedItem.toString()
        }

        if (projectsSpinner.selectedItemPosition > 0) {
            userData[CURRENT_PROJECT] = projectsSpinner.selectedItem.toString()
        }

        val oldProject = QueryUtils.getProject(myUser.track, myUser.currentProject ?: "")
        var newProject: ProjectsModel? = null
        if (userData[TRACK] != null && userData[CURRENT_PROJECT] != null) {
            newProject = QueryUtils.getProject(userData[TRACK] as String
                    , userData[CURRENT_PROJECT] as String)
        }
        val oldLang1 = myUser.language1
        val oldLang2 = myUser.language2

        //put userdata to database (path to myUserdata is declared in userLogIn function)
        myUserData.set(userData).addOnSuccessListener({
            Toast.makeText(this@UserActivity, getString(R.string.dataSaved), Toast.LENGTH_SHORT).show()

            if (newProject?.id ?: "" != oldProject?.id ?: "") {
                if (oldProject != null) {
                    updateProject(oldProject, -1)
                }
                if (newProject != null) {
                    updateProject(newProject, 1)
                }
            }

            if (oldLang1 != lang1) {
                updateUserByLang(myUserData.id, userData[CURRENT_PROJECT] as String, oldLang1, lang1)
            }

            if (oldLang2 != lang2) {
                updateUserByLang(myUserData.id, userData[CURRENT_PROJECT] as String, oldLang2, lang2)
            }

        }).addOnFailureListener {
            Toast.makeText(this@UserActivity, getString(R.string.somethingWrong), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProject(project: ProjectsModel, value: Long) {
        project.nbUsers = project.nbUsers + value
        myDatabase.document("Projects/${project.id}").set(project).addOnSuccessListener({
            Log.d(UserActivity::class.simpleName, "Project ${project.id} saved")
        }).addOnFailureListener {
            Log.d(UserActivity::class.simpleName, "Fail in Project ${project.id} saving")
        }
    }

    private fun updateUserByLang(user: String, project: String, old: String, new: String) {
        myDatabase.document("UsersByLanguage/${old}_$user").delete().addOnSuccessListener({
            Log.d(UserActivity::class.simpleName, "User $user deleted on $old")
        }).addOnFailureListener {
            Log.d(UserActivity::class.simpleName, "Fail in deletion $user on $old")
        }

        if (new != "") {
            val userByLanguageData = HashMap<String, Any>()
            userByLanguageData["project"] = project
            myDatabase.collection("UsersByLanguage").document("${new}_$user").set(userByLanguageData)
                    .addOnSuccessListener({
                        Log.d(UserActivity::class.simpleName, "User $user saved on $new")
                    }).addOnFailureListener {
                        Log.d(UserActivity::class.simpleName, "Fail in saving $user on $new")
                    }
        }
    }

    private fun updateProject(project: ProjectsModel, value: Long) {
        project.nbUsers = project.nbUsers + value
        myDatabase.document("Projects/${project.id}").set(project).addOnSuccessListener({
            Log.d(UserActivity::class.simpleName, "Project ${project.id} saved")
        }).addOnFailureListener {
            Log.d(UserActivity::class.simpleName, "Fail in Project ${project.id} saving")
        }
    }

    private fun updateUserByLang(user: String, old: String, new: String) {
        myDatabase.document("UsersByLanguage/${old}_$user").delete().addOnSuccessListener({
            Log.d(UserActivity::class.simpleName, "User $user deleted on $old")
        }).addOnFailureListener {
            Log.d(UserActivity::class.simpleName, "Fail in deletion $user on $old")
        }

        if (new != "") {
            val userByLanguageData = HashMap<String, Any>()
            userByLanguageData["know"] = true
            myDatabase.collection("UsersByLanguage").document("${new}_$user").set(userByLanguageData)
                    .addOnSuccessListener({
                        Log.d(UserActivity::class.simpleName, "User $user saved on $new")
                    }).addOnFailureListener {
                        Log.d(UserActivity::class.simpleName, "Fail in saving $user on $new")
                    }
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
                                    if (task.isSuccessful) {

                                        //Calling delete to remove the user and wait for a result.
                                        currentUser.delete().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                                toast(getString(R.string.userDeleted))
                                                //Delete data from database

                                                //Decrease nbUser of the project
                                                val project = QueryUtils.getProject(myUser.track, myUser.currentProject
                                                        ?: "")
                                                if (project != null) {
                                                    updateProject(project, -1)
                                                }

                                                //Delete user from UsersByLanguage
                                                updateUserByLang(myUserData.id, "", myUser.language1, "")
                                                updateUserByLang(myUserData.id, "", myUser.language2, "")

                                                myUserData.delete()

                                            } else {

                                                alert(getString(R.string.log_out_message_for_delete_user)) {
                                                    positiveButton(getString(R.string.ok)) { }
                                                }.show()

                                                Log.e("usun usera ", "${task.exception}")
                                            }
                                        }
                                        startActivity<MainActivity>()
                                        finish()
                                    }
                                })
                    }
                }
    }

    private fun logOutUser() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            startActivity<MainActivity>()
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
                ifUserIsVerified()

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
        myUserData.addSnapshotListener(this, { snapshot, _ ->

            if (snapshot?.exists()!!) {
                slackNick.setText(snapshot.getString(SLACK_NAME))
                myUser.slackNick = snapshot.getString(SLACK_NAME)

                myUser.currentProject = snapshot.getString(CURRENT_PROJECT)
                myUser.language1 = snapshot.getString(LANGUAGE)?.split(",")?.get(0) ?: ""
                myUser.language2 = snapshot.getString(LANGUAGE)?.split(",")?.get(1) ?: ""
                myUser.track = snapshot.getString(TRACK) ?: ""

                updateUserData()
            } else {
                toast(getString(R.string.chooseMessage))
                saveUser()
            }
        })
    }

    private fun updateUserData() {
        //take data in correct order
        val spinnerPositionLang1 = spinnerAdapterLanguages1.getPosition(myUser.language1)
        val spinnerPositionLang2 = spinnerAdapterLanguages2.getPosition(myUser.language2)
        val spinnerPositionTracks = spinnerAdapterTracks.getPosition(myUser.track)

        //for tracks
        trackSpinner.setSelection(spinnerPositionTracks)

        //lang1
        lang1Spinner.setSelection(spinnerPositionLang1)

        //lang2
        lang2Spinner.setSelection(spinnerPositionLang2)
    }

    private fun fillData() {

        val tracks = QueryUtils.getStringTracks()
        val languages = QueryUtils.getStringLanguages()

        for (track in tracks) {
            val projects = QueryUtils.getStringProjects(track)
            projects.add(0, getString(R.string.selectProject))
            spinnerAdapterProjects[track] = ArrayAdapter(applicationContext,
                    android.R.layout.simple_spinner_item, projects)
        }

        tracks.add(0, getString(R.string.selectTrack))
        languages.add(0, getString(R.string.selectLanguage))

        //Tracks list
        spinnerAdapterTracks = ArrayAdapter(this, android.R.layout.simple_spinner_item, tracks)
        spinnerAdapterTracks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        trackSpinner.adapter = spinnerAdapterTracks

        //Languages lists (there are 2 fields for this) but need get position this why I create 2 adapters
        spinnerAdapterLanguages1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        spinnerAdapterLanguages1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        lang1Spinner.adapter = spinnerAdapterLanguages1

        spinnerAdapterLanguages2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        spinnerAdapterLanguages2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        lang2Spinner.adapter = spinnerAdapterLanguages2

        // Here we have all the spinner data available, we can now fill connect the user
        updateUserData()
    }

    private fun updateProjectSpinner(selectedTrack: String) {

        val selectedSpinnerAdapterProjects = spinnerAdapterProjects[selectedTrack]
        if (selectedSpinnerAdapterProjects != null) {
            selectedSpinnerAdapterProjects.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            projectsSpinner.adapter = selectedSpinnerAdapterProjects

            //If the user is logged, we should try to select the right project
            val spinnerPositionProjects = selectedSpinnerAdapterProjects
                    .getPosition(myUser.currentProject)
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