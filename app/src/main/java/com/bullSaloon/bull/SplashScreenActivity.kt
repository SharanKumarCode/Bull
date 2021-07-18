package com.bullSaloon.bull

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.bullSaloon.bull.databinding.ActivitySplashScreenBinding
import com.bullSaloon.bull.genericClasses.UserBasicDataParcelable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val EXTRA_INTENT: String = "userBasicData"
    private val TAG: String = "TAG"

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

        Log.i(TAG,"user id: ${auth.currentUser?.uid}")

        Handler().postDelayed({
            if (auth.currentUser == null){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
//                val intent = Intent(this, MainActivity::class.java)
                val db = Firebase.firestore
                db.collection("Users")
                    .document(auth.currentUser?.uid!!)
                    .get()
                    .addOnSuccessListener {
                        if (it?.exists()!!) {
                            val userBasicData = UserBasicDataParcelable(
                                it.getString("user_id")!!,
                                it.getString("user_name")!!,
                                it.getString("mobile_number")!!
                            )
                            intent.putExtra(EXTRA_INTENT, userBasicData)
                            startActivity(intent)
                            finish()
                            }
                        }
                    .addOnFailureListener {
                        Log.i(TAG,"error: ${it.message}")
                        Toast.makeText(this, "Error occurred. Please check your internet connection", Toast.LENGTH_SHORT).show()
                        }
                    }
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
        val d = binding.splashScreenImage.drawable as AnimatedVectorDrawable
        d.start()
    }
}