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

package com.awesomeapp.android.awesomeapp

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import java.util.*

//flag for registered user
private const val RC_SIGN_IN = 1

open class MenuActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.account -> {
                addAuthListener()
                mAuth.addAuthStateListener(mAuthStateListener)
            }
            R.id.slack->{
                val slackApp = Uri.parse("slack://channel?id=C94NC2CKW&team=C991Q405A-1524736492.000138")
                val webIntent = Intent(Intent.ACTION_VIEW, slackApp)
                startActivity(webIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addAuthListener() {
        //check if user is login
        mAuthStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                // User is signed out - show login screen
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setLogo(R.drawable.ic_awesomelogoreadyvwithtext)
                                .setAvailableProviders(
                                        Arrays.asList(AuthUI.IdpConfig.EmailBuilder().build(),
                                                AuthUI.IdpConfig.GoogleBuilder().build()))
                                .build(),
                        RC_SIGN_IN)
            } else {
                mAuth.removeAuthStateListener(mAuthStateListener)
                startActivity(Intent(this, UserActivity::class.java))
            }
        }
    }
}
