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

    //database model - helpdata
    val TRACKS_ARRAY = "tracksArray"
    val LANG_TABLE = "langsArray"

    val AND_PROJECTS = "andProjectsArray"
    val MWS_PROJECTS = "mwsProjectsArray"
    val ABND_PROJECTS = "abndProjectsArray"
    val FEND_PROJECTS = "fendProjectsArray"

    //database model - user
    val USER_NAME = "userName"
    val USER_EMAIL = "userEmail"
    val SLACK_NAME = "slackName"
    val LANGUAGE_1 = "languageFirst"
    val LANGUAGE_2 = "languageSecond"
    val CURRENT_PROJECT = "currentProject"
    val TRACK = "userTrack"

    //key for intent in MainActivity
    val TABLE_WITH_DATA = "TABLE"
    //key for intent in ProjectsActivity
    val WHICH_PROJECT = "whihProject"
    val WHICH_TRACT = "whihTrack"

    //help data hook
    val myHelpData = FirebaseFirestore.getInstance().document("helpData/tracks").get()
    val myUsers = FirebaseFirestore.getInstance().collection("Users")
}
