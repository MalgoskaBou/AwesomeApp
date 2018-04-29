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

    //database model - user
    const val SLACK_NAME = "slackName"
    const val CURRENT_PROJECT = "currentProject"

    //key for intent in MainActivity
    const val TABLE_WITH_DATA = "TABLE"
    //key for intent in ProjectsActivity
    const val WHICH_PROJECT = "whichProject"
    const val WHICH_DEADLINE = "whichDeadline"
    const val WHICH_NB_USERS = "whichNbUsers"


    //help data hook
    val myUsers = FirebaseFirestore.getInstance().collection("Users")
}
