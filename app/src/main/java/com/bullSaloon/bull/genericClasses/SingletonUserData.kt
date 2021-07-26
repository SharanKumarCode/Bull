package com.bullSaloon.bull.genericClasses

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import com.bullSaloon.bull.MainActivity
import com.bullSaloon.bull.R
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

object SingletonUserData {

    lateinit var userData: UserDataClass
    private lateinit var userID: String
    private lateinit var imageUrl: String
    private lateinit var userName: String
    private lateinit var mobileNumber: String
    private lateinit var scrollState: Bundle

    private val storage = Firebase.storage
    private val auth = Firebase.auth
    private val TAG = "TAG"

    init {
        Log.i("TAG", "Singleton class for basic user data created")
        val db = Firebase.firestore

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {

                if (it?.exists()!!) {

                    val userName = it.getString("user_name")!!
                    val userID = it.getString("user_id")!!
                    val mobileNumber = it.getString("mobile_number")!!

                    userData =
                        UserDataClass(userID, userName, mobileNumber, null)

                }
            }

    }

    fun updateUserData(context: Context, activityFlag: String = "null"){

        val db = Firebase.firestore

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener { it ->

                if (it?.exists()!!) {

                    userName = it.getString("user_name")!!
                    userID = it.getString("user_id")!!
                    mobileNumber = it.getString("mobile_number")!!

                    val userNameUnderscore =
                        it.getString("user_name")!!.replace("\\s".toRegex(), "_")
                    imageUrl =
                        "User_Images/${userID}/${userNameUnderscore}_profilePicture.jpg"

                    userData = UserDataClass(userID, userName, mobileNumber, null)

                    if (activityFlag == "MainActivity") {
                        launchMainActivity(context)
                    }

                    }
                }.addOnFailureListener {
                    Log.i(TAG, "error: ${it.message}")
                }
    }

    fun getProfilePic(mainActivity: MainActivity) {

        val profilePicFileTemp = File(
            getOutputDirectory(mainActivity),
            "profile_picture" + ".jpg"
        )

        storage.reference.child(imageUrl).getFile(profilePicFileTemp)
            .addOnSuccessListener {

                val ei = ExifInterface(profilePicFileTemp).rotationDegrees
                val bitmap = BitmapFactory.decodeFile(profilePicFileTemp.absolutePath)
                val bitmapThumbnail = ThumbnailUtils.extractThumbnail(bitmap, 200, 200)
                val matrix = Matrix()
                matrix.postRotate(ei.toFloat())
                val rotatedImgBitmap = Bitmap.createBitmap(
                    bitmapThumbnail,
                    0,
                    0,
                    bitmapThumbnail.width,
                    bitmapThumbnail.height,
                    matrix,
                    true
                )

                val dataViewModel = ViewModelProvider(mainActivity).get(UserDataViewModel::class.java)
                dataViewModel.assignProfilePic(rotatedImgBitmap)

                profilePicFileTemp.delete()

                Log.i("TAGProfile","user profile pic downloaded : $rotatedImgBitmap")

            }
            .addOnFailureListener {
                Log.i("TAG", "profile pic error : ${it.message}")
                userData =
                    UserDataClass(userID, userName, mobileNumber, null)
            }
    }

    private fun launchMainActivity(context: Context){
        val intent = Intent(context, MainActivity::class.java)
        (context as Activity).finish()
        startActivity(context,intent,null)
    }

    private fun getOutputDirectory(context: Context): File {
        val mediaDir = context.externalMediaDirs.firstOrNull().let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }

    fun updateScrollState(state: Bundle){
        scrollState = state
    }

    fun getScrollState(): Bundle?{
        return if (this::scrollState.isInitialized){
            scrollState
        } else null
    }

}