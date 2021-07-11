package com.bullSaloon.bull

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.bullSaloon.bull.databinding.ActivitySplashScreenBinding
import com.bullSaloon.bull.genericClasses.UserDataClass
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //assigning Basic user data (user_id, user_name, mobile_number) to UserData View Model to be used in all app

        val auth = Firebase.auth
        val db = Firebase.firestore

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val userId = it.getString("user_data")
                    val userName = it.getString("user_name")
                    val mobileNumber = it.getString("mobile_number")
                    val userBasicData = UserDataClass(userId!!,userName!!,mobileNumber!!)
                    val userDataModel = ViewModelProvider(this).get(UserDataViewModel::class.java)

                    userDataModel.assignUserData(userBasicData)
                }
            }

        Handler().postDelayed({
            if (auth.currentUser == null){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
        val d = binding.splashScreenImage.drawable as AnimatedVectorDrawable
        d.start()
    }
}