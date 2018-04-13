package com.awesomeapp.android.awesomeapp

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseUser


class UserActivity : MenuActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var myDatabase: FirebaseFirestore
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    lateinit var myHelpData: DocumentReference

    val RC_SIGN_IN = 1


    lateinit var userName: String
    lateinit var userEmail: String
    lateinit var userUid:String
    //lateinit var userEmailVerified: Boolean




    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        setSupportActionBar(myToolbar)


        myDatabase = FirebaseFirestore .getInstance()
        myHelpData =  myDatabase.document("helpData/tracks")


        //GET DATA FROM DATABASE===================================

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
        //END == GET DATA FROM DATABASE==========================


        //DATABASE LOG IN========================================
        mAuth = FirebaseAuth.getInstance()

        mAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {

                userName  = user.displayName!!
                userEmail = user.email!!
                //userEmailVerified = user.emailVerified
                userUid = user.uid

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
        } // END == OF DATABASE LOG IN===========================

    }//End onCreate


    override fun onResume() {
        super.onResume()
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onPause() {
        super.onPause()
        mAuth.removeAuthStateListener(mAuthStateListener)
    }
}
