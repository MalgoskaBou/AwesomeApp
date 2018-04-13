package com.awesomeapp.android.awesomeapp

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class MainActivity : MenuActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button2.setOnClickListener{v ->
                startActivity(Intent(this, MenuActivity::class.java))
            }
        setSupportActionBar(myToolbar)

    }

}
