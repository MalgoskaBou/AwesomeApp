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
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import com.awesomeapp.android.awesomeapp.data.Constant.SLACK_NAME
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import com.awesomeapp.android.awesomeapp.model.UserModel
import com.awesomeapp.android.awesomeapp.util.QueryUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.*


@Suppress("DEPRECATION")
class UserActivity : AppCompatActivity() {

    //hooks to database
    private lateinit var mAuth: FirebaseAuth
    private lateinit var myDatabase: FirebaseFirestore
    private lateinit var myUserData: DocumentReference

    //get user data from user panel
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userUid: String

    private var myUser: UserModel = UserModel()

    //data listener
    private lateinit var fetchDataListener: ListenerRegistration

    //spinner adapters
    private lateinit var spinnerAdapterTracks: ArrayAdapter<String>
    private lateinit var spinnerAdapterLanguages1: ArrayAdapter<String>
    private lateinit var spinnerAdapterLanguages2: ArrayAdapter<String>
    private val spinnerAdapterProjects: HashMap<String, ArrayAdapter<String>> = HashMap()

    //anko loading window - because the first connection to the database takes quite a long time
    private var myProgressBar: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(myToolbar)

        //get database hook
        myDatabase = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //loader window
        myProgressBar = indeterminateProgressDialog(getString(R.string.data_saving))
        myProgressBar?.dismiss()
        myProgressBar?.setOnCancelListener {
            alert(getString(R.string.cancel_progressbar_info)) {
                yesButton { startActivity<MainActivity>() }
            }.show()
        }

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
        getUserInfos()

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

        //information for the user that have to first select the track and then the project
        projectsSpinner.setOnTouchListener({ _, _ ->
            if (projectsSpinner.adapter == null) {
                toast(getString(R.string.choose_your_track_first))
            }
            false
        })
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

        // Get old values
        val oldLang1 = myUser.getLanguage(0)
        val oldLang2 = myUser.getLanguage(1)
        val oldProject = QueryUtils.getProject(myUser.userTrack, myUser.currentProject)
        val oldSlackName = myUser.slackName

        if (slackNick.text == null || slackNick.text.toString().trim() == "") {
            myUser.slackName = getString(R.string.undefined)
        } else {
            myUser.slackName = slackNick.text.toString()
        }

        var lang1 = ""
        var lang2 = ""
        if (lang1Spinner.selectedItemPosition > 0) {
            lang1 = lang1Spinner.selectedItem.toString()
        }

        if (lang2Spinner.selectedItemPosition > 0) {
            lang2 = lang2Spinner.selectedItem.toString()
        }
        myUser.language = "$lang1,$lang2"

        if (trackSpinner.selectedItemPosition > 0) {
            myUser.userTrack = trackSpinner.selectedItem.toString()
        } else {
            myUser.userTrack = ""
        }

        if (projectsSpinner.selectedItemPosition > 0) {
            myUser.currentProject = projectsSpinner.selectedItem.toString()
        } else {
            myUser.currentProject = ""
        }

        var newProject: ProjectsModel? = null
        if (myUser.userTrack != "" && myUser.currentProject != "") {
            newProject = QueryUtils.getProject(myUser.userTrack, myUser.currentProject)
        }

        myProgressBar?.show()

        //put userdata to database (path to myUserdata is declared in getUserInfos function)
        myUserData.set(myUser).addOnSuccessListener({

            var flagForceUpdateLanguage = false
            if (newProject?.id ?: "" != oldProject?.id ?: "") {
                if (oldProject != newProject) {
                    if (oldProject != null) {
                        updateProject(oldProject, -1)
                    }
                    if (newProject != null) {
                        updateProject(newProject, 1)
                        flagForceUpdateLanguage = true
                    }
                }
            }

            if (oldSlackName != slackNick.text.toString()) {
                flagForceUpdateLanguage = true
            }

            if (oldLang1 != lang1 || flagForceUpdateLanguage) {
                updateUserByLang(myUserData.id, myUser, oldLang1, lang1)
            }

            if (oldLang2 != lang2 || flagForceUpdateLanguage) {
                updateUserByLang(myUserData.id, myUser, oldLang2, lang2)
            }

            toast(getString(R.string.dataSaved))
            startActivity<MainActivity>()

        }).addOnFailureListener {
            toast(getString(R.string.somethingWrong))
            myProgressBar?.dismiss()
        }

        if (!QueryUtils.checkInternetConnection(this)) {
            toast("No internet connection, your data will be saved later")
            startActivity<MainActivity>()
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

    private fun updateUserByLang(user: String, userModel: UserModel?, old: String, new: String) {
        myDatabase.document("UsersByLanguage/${old}_$user").delete().addOnSuccessListener({
            Log.d(UserActivity::class.simpleName, "User $user deleted on $old")
        }).addOnFailureListener {
            Log.d(UserActivity::class.simpleName, "Fail in deletion $user on $old")
        }

        if (new != "") {
            val userByLanguageData = HashMap<String, Any>()
            userByLanguageData["project"] = userModel!!.currentProject
            userByLanguageData["slackName"] = userModel.slackName
            userByLanguageData["languages"] = userModel.language
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

                        currentUser.reauthenticate(credential).addOnCompleteListener({
                            if (task.isSuccessful) {

                                //Calling delete to remove the user and wait for a result.
                                currentUser.delete().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        toast(getString(R.string.userDeleted))
                                        //Delete data from database

                                        //remove fetch data snapshot listenet for do not retrive data to database
                                        fetchDataListener.remove()

                                        //Decrease nbUser of the project
                                        val project = QueryUtils.getProject(myUser.userTrack
                                                , myUser.currentProject)
                                        if (project != null) {
                                            updateProject(project, -1)
                                        }

                                        //Delete user from UsersByLanguage
                                        updateUserByLang(myUserData.id, null
                                                , myUser.getLanguage(0), "")
                                        updateUserByLang(myUserData.id, null
                                                , myUser.getLanguage(1), "")

                                        myUserData.delete()

                                    } else {

                                        toast(getString(R.string.log_out_message_for_delete_user))
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

    private fun getUserInfos() {

        val user = mAuth.currentUser
        // User is signed in - get data from log in
        userName = user!!.displayName!!
        userEmail = user.email!!
        userUid = user.uid

        //get document with actual user from database
        myUserData = myDatabase.document("Users/$userUid")

        //fetch data from database
        fetchUserData()
        ifUserIsVerified()

        welcomeText.text = getString(R.string.welcome_message, userName, userEmail)
    }

    private fun fetchUserData() {

        //get data from user collection
        fetchDataListener = myUserData.addSnapshotListener(this, { snapshot, _ ->

            if (snapshot?.exists()!!) {
                slackNick.setText(snapshot.getString(SLACK_NAME))

                myUser = snapshot.toObject(UserModel::class.java) ?: myUser

                updateUserData()
            } else {
                toast(getString(R.string.chooseMessage))
                saveUser()
            }
        })
    }

    private fun updateUserData() {
        //take data in correct order
        val spinnerPositionLang1 = spinnerAdapterLanguages1.getPosition(myUser.getLanguage(0))
        val spinnerPositionLang2 = spinnerAdapterLanguages2.getPosition(myUser.getLanguage(1))
        val spinnerPositionTracks = spinnerAdapterTracks.getPosition(myUser.userTrack)

        //for tracks
        trackSpinner.setSelection(spinnerPositionTracks)

        //lang1
        lang1Spinner.setSelection(spinnerPositionLang1)

        //lang2
        lang2Spinner.setSelection(spinnerPositionLang2)
    }

    /**
     * Fill the spinner with Generic data
     */
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
}