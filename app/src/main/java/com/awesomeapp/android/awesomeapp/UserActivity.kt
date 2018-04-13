package com.awesomeapp.android.awesomeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*
import kotlin.collections.ArrayList
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.HashMap


class UserActivity : MenuActivity() {

    //database
    lateinit var mAuth: FirebaseAuth
    lateinit var myDatabase: FirebaseFirestore
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    lateinit var myHelpData: DocumentReference
    lateinit var myUserData: DocumentReference

    //user data
    lateinit var userName: String
    lateinit var userEmail: String
    lateinit var userUid:String

    val RC_SIGN_IN = 1

    val SLACK_NAME = "slackName"
    val LANGUAGE_1 = "languageFirst"
    val LANGUAGE_2 = "languageSecond"
    val CURRENT_PROJECT = "currentProject"
    val TRACK = "userTrack"



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        setSupportActionBar(myToolbar)


        myDatabase = FirebaseFirestore .getInstance()
        myHelpData =  myDatabase.document("helpData/tracks")
        mAuth = FirebaseAuth.getInstance()


        //GET DATA FROM DATABASE===================================
        getDataFromDatabase()

        //DATABASE LOG IN========================================
        userLogIn()

        //Put user data to database==============================
        saveBtn.setOnClickListener{v ->
            saveUser()
        }

    }

    private fun saveUser() {
        var userData  = HashMap<String, Any>()
        userData[SLACK_NAME] = slackNick.text.toString()
        userData[LANGUAGE_1] = lang1Spinner.selectedItem.toString()
        userData[LANGUAGE_2] = lang2Spinner.selectedItem.toString()
        userData[TRACK] = trackSpinner.selectedItem.toString()
        userData[CURRENT_PROJECT] = projectsSpinner.selectedItem.toString()

        myUserData.set(userData).addOnSuccessListener({
            Toast.makeText(this@UserActivity, "Data saved", Toast.LENGTH_SHORT).show()

        }).addOnFailureListener{
            Toast.makeText(this@UserActivity, "Someting wrong :( try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun userLogIn(){

        mAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {

                userName  = user.displayName!!
                userEmail = user.email!!
                userUid = user.uid
                myUserData = myDatabase.document("Users/$userUid")

                testText.text = "$userName $userEmail"
                // User is signed in
            } else {
                // User is signed out
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

    private fun getDataFromDatabase(){

        myHelpData.addSnapshotListener(this, EventListener<DocumentSnapshot> { snapshots, e ->
            if (e != null) {
                Log.w("blad - ",e)
                Toast.makeText(this@UserActivity, "Error :(", Toast.LENGTH_SHORT).show()
                return@EventListener

            }else if (snapshots.exists()) {
                var tracksTable = snapshots.get("tracksArray") as ArrayList<String>
                var langTable = snapshots.get("langsArray") as ArrayList<String>

                var andProjTable = snapshots.get("andProjectsArray") as ArrayList<String>
                var mwsProjTable = snapshots.get("mwsProjectsArray") as ArrayList<String>
                var abndProjTable = snapshots.get("abndProjectsArray") as ArrayList<String>
                var fendProjTable = snapshots.get("fendProjectsArray") as ArrayList<String>


                //Tracks list
                var spinnerAdapterTracks = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tracksTable)
                spinnerAdapterTracks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                trackSpinner.adapter = spinnerAdapterTracks


                //Projects list - changing with tracks list
                lateinit var spinnerAdapterProjects: ArrayAdapter<String>

                //Check wchih one position is choosed - like a track
                trackSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {

                        when(trackSpinner.selectedItem.toString()){
                            "AND" -> {spinnerAdapterProjects = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, andProjTable)}
                            "ABND" -> {spinnerAdapterProjects = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, abndProjTable)}
                            "MWS" -> {spinnerAdapterProjects = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, mwsProjTable)}
                            "FEND" -> {spinnerAdapterProjects = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, fendProjTable)}
                        }
                        spinnerAdapterProjects.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        projectsSpinner.adapter = spinnerAdapterProjects

                    }
                    override fun onNothingSelected(parentView: AdapterView<*>) {
                        // do nothing
                    }
                }

                //Languages lists (there are 2 fields for this)
                var spinnerAdapterLanguages = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, langTable)
                spinnerAdapterLanguages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lang1Spinner.adapter = spinnerAdapterLanguages
                lang2Spinner.adapter = spinnerAdapterLanguages
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onPause() {
        super.onPause()
        mAuth.removeAuthStateListener(mAuthStateListener)
    }
}
