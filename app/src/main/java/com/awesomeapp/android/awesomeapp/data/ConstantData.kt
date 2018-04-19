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

package com.awesomeapp.android.awesomeapp.data

import android.app.Activity
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