package com.bullSaloon.bull

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.ThumbnailUtils
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import com.bullSaloon.bull.databinding.ActivitySplashScreenBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val TAG: String = "TAG"
    private lateinit var auth: FirebaseAuth

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

        auth = Firebase.auth

        Log.i(TAG,"user id: ${auth.currentUser?.uid}")

        Handler().postDelayed({
            if (auth.currentUser == null){
//                launch Home Activity
                launchHomeActivity()
                finish()
            } else {
//                get user data and launch Main Activity
                SingletonUserData.updateUserData(this,"MainActivity")
                }
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
        val d = binding.splashScreenImage.drawable as AnimatedVectorDrawable
        d.start()
    }

    private fun launchHomeActivity(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}