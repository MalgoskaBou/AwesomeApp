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

    //help data hook
    val myHelpData = FirebaseFirestore.getInstance().document("helpData/tracks").get()
}
