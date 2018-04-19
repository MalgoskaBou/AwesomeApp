package com.awesomeapp.android.awesomeapp.data

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore

class ConstantData{

    var myDatabase = FirebaseFirestore.getInstance()
    var myHelpData = myDatabase.document("helpData/tracks")


    fun getDataFromDatabase(activity: Activity) {

        myHelpData.addSnapshotListener(activity, EventListener<DocumentSnapshot> { snapshots, e ->
            if (e != null) {
                Log.w("blad - ", e)
                Toast.makeText(activity, "Error :(", Toast.LENGTH_SHORT).show()
                return@EventListener

            } else if (snapshots.exists()) {
                var tracksTable = snapshots.get("tracksArray") as ArrayList<String>
                var langTable = snapshots.get("langsArray") as ArrayList<String>

                var andProjTable = snapshots.get("andProjectsArray") as ArrayList<String>
                var mwsProjTable = snapshots.get("mwsProjectsArray") as ArrayList<String>
                var abndProjTable = snapshots.get("abndProjectsArray") as ArrayList<String>
                var fendProjTable = snapshots.get("fendProjectsArray") as ArrayList<String>
            }
        })
    }
}