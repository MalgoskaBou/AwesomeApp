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
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Created by Fabien Boismoreau on 25/04/2018.
 * <p>
 */
class QueryUtils private constructor() {

    companion object {

        val tracks = ArrayList<String>()
        val projects = HashMap<String, ArrayList<ProjectsModel>>()
        var projectsLoaded = false

        /**
         * Initialise the generic data
         */
        fun initialiseDBData() {

            loadTracks()
            loadProjects()

        }

        fun getProjects(track: String): ArrayList<ProjectsModel> {
            return projects[track]!!
        }

        private fun loadTracks() {
            val tracksCollection = FirebaseFirestore.getInstance().collection("Tracks")

            tracksCollection.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (track in it.result) {
                        Log.d(QueryUtils::class.simpleName, track.id + " => " + track.data)
                        tracks.add(track.get("name") as String)
                    }
                } else {
                    Log.d(QueryUtils::class.simpleName, "Error getting tracks: ", it.exception)
                }
            }
        }

        private fun loadProjects() {
            val projectsCollection = FirebaseFirestore.getInstance().collection("Projects")

            projectsCollection.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (project in it.result) {
                        Log.d(QueryUtils::class.simpleName, project.id + " => " + project.data)
                        val projectObj = project.toObject(ProjectsModel::class.java)
                        Log.d(QueryUtils::class.simpleName, "Obj => $projectObj")

                        val track = project.id.split("_")[0]
                        val projectsByTrack = projects[track] ?: ArrayList()
                        projectsByTrack.add(projectObj)
                        projects[track] = projectsByTrack
                    }
                    projectsLoaded = true
                } else {
                    Log.d(QueryUtils::class.simpleName, "Error getting projects: ", it.exception)
                }
            }
        }

    }
}