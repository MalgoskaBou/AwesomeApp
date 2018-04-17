package com.awesomeapp.android.awesomeapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import com.awesomeapp.android.awesomeapp.model.MyUser
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
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
    lateinit var myHelpData: DocumentReference
    lateinit var myUserData: DocumentReference

    //get user data from user panel
    lateinit var userName: String
    lateinit var userEmail: String
    lateinit var userUid: String

    private val myUser: MyUser? = MyUser()

    //flag for registrated user
    val RC_SIGN_IN = 1

    //user data keys what I want to put in database
    val USER_NAME = "userName"
    val USER_EMAIL = "userEmail"
    val SLACK_NAME = "slackName"
    val LANGUAGE_1 = "languageFirst"
    val LANGUAGE_2 = "languageSecond"
    val CURRENT_PROJECT = "currentProject"
    val TRACK = "userTrack"

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
        userData[CURRENT_PROJECT] = projectsSpinner.selectedItem.toString()

        //put userdata to database (path to myUserdata is declared in userLogIn function)
        myUserData.set(userData).addOnSuccessListener({
            Toast.makeText(this@UserActivity, "Data saved", Toast.LENGTH_SHORT).show()

        }).addOnFailureListener {
            Toast.makeText(this@UserActivity, "Someting wrong :( try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun usrLogOut(){

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

    private fun fetchUserData() {

        //get data from user collection
        myUserData.get().addOnSuccessListener({ snapshots ->

            if (snapshots.exists()) {
                slackNick.setText(snapshots.getString(SLACK_NAME))
                myUser!!.slackNick = snapshots.getString(SLACK_NAME)

                myUser.currentProject = snapshots.getString(CURRENT_PROJECT)
                myUser.language1 = snapshots.getString(LANGUAGE_1)
                myUser.language2 = snapshots.getString(LANGUAGE_2)
                myUser.track = snapshots.getString(TRACK)

                updateUserData()
            }
        }).addOnFailureListener {
            Toast.makeText(this@UserActivity, "Someting wrong :( can't take your data", Toast.LENGTH_SHORT).show()
        }
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

        //get helpData (snapshotListener allow synchronize data in real time)
        myHelpData.addSnapshotListener(this, EventListener<DocumentSnapshot> { snapshots, e ->
            if (e != null) {
                Log.w("error - ", e)
                Toast.makeText(this@UserActivity, "Error :(", Toast.LENGTH_SHORT).show()
                return@EventListener

            } else if (snapshots.exists()) {
                val tracksTable = snapshots.get("tracksArray") as ArrayList<String>
                val langTable = snapshots.get("langsArray") as ArrayList<String>

                val andProjTable = snapshots.get("andProjectsArray") as ArrayList<String>
                val mwsProjTable = snapshots.get("mwsProjectsArray") as ArrayList<String>
                val abndProjTable = snapshots.get("abndProjectsArray") as ArrayList<String>
                val fendProjTable = snapshots.get("fendProjectsArray") as ArrayList<String>

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
