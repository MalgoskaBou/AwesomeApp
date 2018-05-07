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

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.awesomeapp.android.awesomeapp.Refreshable
import com.awesomeapp.android.awesomeapp.model.LanguageModel
import com.awesomeapp.android.awesomeapp.model.ProjectsModel
import com.awesomeapp.android.awesomeapp.model.TrackModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Created by Fabien Boismoreau on 25/04/2018.
 * <p>
 */
class QueryUtils private constructor() {

    companion object {

        private val tracks = ArrayList<TrackModel>()
        private val projects = HashMap<String, ArrayList<ProjectsModel>>()
        private val languages = ArrayList<LanguageModel>()
        private var isLoaded = false
        private var getLimit = 1L
        private var projectsActivity: Refreshable? = null


        /**
         * Check internet connection
         */
        fun checkInternetConnection(context: Context): Boolean {
            //check connection with internet
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

        /**
         * Initialise the generic data
         */
        fun initialiseDBData() {
            if (!isLoaded) {
                loadTracks()
                loadProjects()
                loadLanguages()
                isLoaded = true
            }
        }

        fun getTrack(track: String): TrackModel {
            return tracks.filter { it.id == track }[0]
        }

        fun getStringTracks(): ArrayList<String> {
            val array = ArrayList<String>()
            for (track in tracks) {
                array.add(track.name)
            }
            return array
        }

        fun getProjects(track: String): ArrayList<ProjectsModel>? {
            return projects[track]
        }

        fun getProject(track: String, project: String): ProjectsModel? {
            val list = projects[track]?.filter { projectsModel -> projectsModel.name == project }
                    ?.map { it }
            return if (list != null && list.isNotEmpty()) list[0] else null
        }

        fun getStringProjects(track: String): ArrayList<String> {
            val array = ArrayList<String>()
            val projectsArray = projects[track] ?: return array

            for (language in projectsArray) {
                array.add(language.name)
            }
            return array
        }

        fun getLanguage(language: String): LanguageModel {
            return languages.filter { it.id == language }[0]
        }

        fun getStringLanguages(): ArrayList<String> {
            val array = ArrayList<String>()
            for (language in languages) {
                array.add(language.name)
            }
            return array
        }

        fun setGetLimit(value: Long) {
            getLimit = value
        }

        fun getGetLimit(): Long {
            return getLimit
        }

        fun setProjectActivity(a: Refreshable) {
            projectsActivity = a
        }

        fun removeProjectActivity() {
            projectsActivity = null
        }

        private fun refreshUI() {
            projectsActivity?.refreshUI()
        }

        private fun loadTracks() {
            val tracksCollection = FirebaseFirestore.getInstance().collection("Tracks")

            tracksCollection.addSnapshotListener(EventListener<QuerySnapshot> { snapshots, e ->
                if (e != null) {
                    Log.w(QueryUtils::class.simpleName, "listen:error", e)
                    return@EventListener
                }

                if (snapshots == null) return@EventListener

                for (dc in snapshots.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(QueryUtils::class.simpleName, "New track: " + dc.document.data)
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

            projectsCollection.addSnapshotListener(EventListener<QuerySnapshot> { snapshots, e ->
                if (e != null) {
                    Log.w(QueryUtils::class.simpleName, "listen:error", e)
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

                refreshUI()
            })
        }

        private fun loadLanguages() {
            val languageCollection = FirebaseFirestore.getInstance().collection("Languages")

            languageCollection.addSnapshotListener(EventListener<QuerySnapshot> { snapshots, e ->
                if (e != null) {
                    Log.w(QueryUtils::class.simpleName, "listen:error", e)
                    return@EventListener
                }

                if (snapshots == null) return@EventListener

                for (dc in snapshots.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(QueryUtils::class.simpleName, "New language: " + dc.document.data)
                            val languageModel = dc.document.toObject(LanguageModel::class.java).copy()
                            languageModel.id = dc.document.id
                            languages.add(languageModel)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d(QueryUtils::class.simpleName, "Modified language: " + dc.document.data)
                            val language = languages.filter { it.id == dc.document.id }[0]
                            language.name = dc.document.data["name"] as String
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(QueryUtils::class.simpleName, "Removed language: " + dc.document.data)
                            val language = languages.filter { it.id == dc.document.id }[0]
                            languages.removeAt(languages.indexOf(language))
                        }
                    }
                }
            })
        }
    }
}