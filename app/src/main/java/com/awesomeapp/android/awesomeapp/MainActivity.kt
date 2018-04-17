package com.awesomeapp.android.awesomeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class MainActivity : MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(myToolbar)

        val TABLE_WITH_DATA = "TABLE"
        val intentToData = Intent(this, ProjectsActivity::class.java)

        val clickListener = View.OnClickListener { v ->
            when (v.getId()) {
                R.id.andAndroid -> intentToData.putExtra(TABLE_WITH_DATA, "")
                R.id.abndAndroid -> intentToData.putExtra(TABLE_WITH_DATA, "")
                R.id.mwsAndroid -> intentToData.putExtra(TABLE_WITH_DATA, "")
                R.id.fendAndroid -> intentToData.putExtra(TABLE_WITH_DATA, "")
            }
            startActivity(intentToData)
        }

        andAndroid.setOnClickListener(clickListener)
        abndAndroid.setOnClickListener(clickListener)
        mwsAndroid.setOnClickListener(clickListener)
        fendAndroid.setOnClickListener(clickListener)
    }
}
