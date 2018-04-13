package com.awesomeapp.android.awesomeapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

open class MenuActivity : AppCompatActivity() {



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.account -> {
                startActivity(Intent(this, UserActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
