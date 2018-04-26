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

import com.google.firebase.firestore.FirebaseFirestore


object Constant {

    const val TRACK_ABND = "ABND"
    const val TRACK_AND = "AND"
    const val TRACK_FEND = "FEND"
    const val TRACK_MWS = "MWS"

    //database model - helpdata
    const val TRACKS_ARRAY = "tracksArray"
    const val LANG_TABLE = "langsArray"

    const val AND_PROJECTS = "andProjectsArray"
    const val MWS_PROJECTS = "mwsProjectsArray"
    const val ABND_PROJECTS = "abndProjectsArray"
    const val FEND_PROJECTS = "fendProjectsArray"

    //database model - user
    const val USER_NAME = "userName"
    const val USER_EMAIL = "userEmail"
    const val SLACK_NAME = "slackName"
    const val LANGUAGE_1 = "languageFirst"
    const val LANGUAGE_2 = "languageSecond"
    const val CURRENT_PROJECT = "currentProject"
    const val TRACK = "userTrack"

    //key for intent in MainActivity
    const val TABLE_WITH_DATA = "TABLE"
    //key for intent in ProjectsActivity
    const val WHICH_PROJECT = "whichProject"
    const val WHICH_TRACT = "whichTrack"

    //help data hook
    val myHelpData = FirebaseFirestore.getInstance().document("helpData/tracks")
    val myUsers = FirebaseFirestore.getInstance().collection("Users")
}
