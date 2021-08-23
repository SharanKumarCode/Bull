package com.bullSaloon.bull

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import com.bullSaloon.bull.databinding.ActivitySplashScreenBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


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

//        assigning Basic user data (user_id, user_name, mobile_number) to UserData View Model to be used in all app
        val auth = SingletonInstances.getAuthInstance()
        val db = SingletonInstances.getFireStoreInstance()
        val storageRef = SingletonInstances.getStorageReference()

        if (auth.currentUser == null){
            auth.signInWithEmailAndPassword(
            "sharankumaraero@gmail.com" , "Sharan"
                ).addOnSuccessListener {
                    Log.i("TAGSplashScreenActivity", "auth login : ${it.credential}")
                }.addOnFailureListener {
                    Log.i("TAGSplashScreenActivity", "auth login error : ${it.message}")
                }
        }

//        db.collection("Users")
//            .document("total_users")
//            .get()
//            .addOnSuccessListener {
//                if (it.exists()){
//                    val data = it.getLong("total_users")
//                    Log.i(TAG,"total users : $data")
//                } else {
//                    Log.i(TAG,"total users does not exists")
//                }
//            }
//            .addOnFailureListener {
//                Log.i(TAG,"total users error : ${it.message}")
//            }

//        val mapData = hashMapOf("user_id" to auth.currentUser?.uid,
//                                "user_name" to "sharan Kumar",
//                                "mobile_number" to "+919941677517")

//        db.collection("Users")
//            .document(auth.currentUser?.uid!!)
//            .set(mapData)
//            .addOnSuccessListener {
//                Log.i(TAG," added success")
//            }
//            .addOnFailureListener {
//                Log.i(TAG," added error : ${it.message}")
//            }

//        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
//        val data = hashMapOf("user_id" to timeStamp)
//
//        db.collection("Users")
//            .document("total_users")
//            .update("data",data)
//            .addOnSuccessListener {
//                Log.i(TAG," added success")
//            }
//            .addOnFailureListener {
//                Log.i(TAG," added error : ${it.message}")
//            }

        Log.i(TAG, "user id : ${auth.currentUser?.uid}")

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

    companion object {
        private const val TAG = "TAGSplashScreenActivity"
    }
}