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

package com.awesomeapp.android.awesomeapp.util

import android.util.Log
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import com.awesomeapp.android.awesomeapp.model.TrackModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*


/**
 * Created by Fabien Boismoreau on 25/04/2018.
 * <p>
 */
class QueryUtils private constructor() {

    companion object {

        val tracks = ArrayList<TrackModel>()
        val projects = HashMap<String, ArrayList<ProjectsModel>>()

        /**
         * Initialise the generic data
         */
        fun initialiseDBData() {

            loadTracks()
            loadProjects()

        }

        fun getProjects(track: String): ArrayList<ProjectsModel>? {
            return projects[track]
        }

        private fun loadTracks() {
            val tracksCollection = FirebaseFirestore.getInstance().collection("Tracks")

            tracksCollection.addSnapshotListener(EventListener<QuerySnapshot> { snapshots, e ->
                if (e != null) {
                    Log.w(QueryUtils::class.simpleName, "listen:error", e);
                    return@EventListener
                }

                if (snapshots == null) return@EventListener

                for (dc in snapshots.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(QueryUtils::class.simpleName, "New track: " + dc.document.data)
                            //var trackModel = TrackModel(dc.document.id)
                            val trackModel = dc.document.toObject(TrackModel::class.java).copy()
                            trackModel.id = dc.document.id
                            tracks.add(trackModel)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d(QueryUtils::class.simpleName, "Modified track: " + dc.document.data)
                            val track = tracks.filter { it.id == dc.document.id }[0]
                            track.name = dc.document.data["name"] as String
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(QueryUtils::class.simpleName, "Removed track: " + dc.document.data)
                            val track = tracks.filter { it.id == dc.document.id }[0]
                            tracks.removeAt(tracks.indexOf(track))
                        }
                    }
                }
            })
        }

        private fun loadProjects() {
            val projectsCollection = FirebaseFirestore.getInstance().collection("Projects")

            //projectsCollection.get().addOnCompleteListener {
            projectsCollection.addSnapshotListener(EventListener<QuerySnapshot> { snapshots, e ->
                if (e != null) {
                    Log.w(QueryUtils::class.simpleName, "listen:error", e);
                    return@EventListener
                }

                if (snapshots == null) return@EventListener

                for (dc in snapshots.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(QueryUtils::class.simpleName, "New project: " + dc.document.data)
                            val projectObj = dc.document.toObject(ProjectsModel::class.java)
                            projectObj.id = dc.document.id
                            val track = dc.document.id.split("_")[0]
                            val projectsByTrack = projects[track] ?: ArrayList()
                            projectsByTrack.add(projectObj)
                            projects[track] = projectsByTrack
                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d(QueryUtils::class.simpleName, "Modified project: " + dc.document.data)
                            val track = dc.document.id.split("_")[0]
                            val projectsByTrack = projects[track]
                            val project = projectsByTrack!!.filter { it.id == dc.document.id }[0]
                            project.name = dc.document.data["name"] as String
                            project.deadline = dc.document.data["deadline"] as Date
                            project.nbUsers = dc.document.data["nbUsers"] as Long
                            project.order = dc.document.data["order"] as Long
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(QueryUtils::class.simpleName, "Removed project: " + dc.document.data)
                            val track = dc.document.id.split("_")[0]
                            val projectsByTrack = projects[track]
                            val project = projectsByTrack!!.filter { it.id == dc.document.id }[0]
                            projectsByTrack.removeAt(projectsByTrack.indexOf(project))
                        }
                    }
                }
            })
        }

    }
}