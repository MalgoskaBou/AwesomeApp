package com.awesomeapp.android.awesomeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.awesomeapp.android.awesomeapp.data.Constant.ABND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.AND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.FEND_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.MWS_PROJECTS
import com.awesomeapp.android.awesomeapp.data.Constant.TABLE_WITH_DATA
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class MainActivity : MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(myToolbar)

        val intentToData = Intent(this, ProjectsActivity::class.java)

        val clickListener = View.OnClickListener { v ->
            when (v.id) {
                R.id.andAndroid -> intentToData.putExtra(TABLE_WITH_DATA, AND_PROJECTS)
                R.id.abndAndroid -> intentToData.putExtra(TABLE_WITH_DATA, ABND_PROJECTS)
                R.id.mwsAndroid -> intentToData.putExtra(TABLE_WITH_DATA, MWS_PROJECTS)
                R.id.fendAndroid -> intentToData.putExtra(TABLE_WITH_DATA, FEND_PROJECTS)
            }
            startActivity(intentToData)
        }

        andAndroid.setOnClickListener(clickListener)
        abndAndroid.setOnClickListener(clickListener)
        mwsAndroid.setOnClickListener(clickListener)
        fendAndroid.setOnClickListener(clickListener)
    }
}
