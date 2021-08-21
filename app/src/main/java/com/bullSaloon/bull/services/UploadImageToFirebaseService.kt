package com.bullSaloon.bull.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bullSaloon.bull.R
import com.bullSaloon.bull.fragments.CameraFragment
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.UploadImageServicePayload
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class UploadImageToFirebaseService : Service() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var photoFileTempPath: String
    private lateinit var photoFileTemp: File

    private lateinit var userName: String
    private lateinit var userID: String
    private lateinit var activityFlag: String

    private var captionText = ""
    private var saloonName = ""

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            try {
                uploadImage()
            } catch (e: InterruptedException) {
                // Restore interrupt status.

                Log.i(TAG, "service thread interrupted error: ${e.message}")
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
        super.onCreate()

        auth = Firebase.auth
        db = Firebase.firestore
        storageRef = Firebase.storage.reference

        HandlerThread("UploadImageServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }

        Log.i(TAG,"service created")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG,"service started")

        val dataBundle = intent?.extras?.getBundle("service_payload")
        val data = dataBundle?.getParcelable<UploadImageServicePayload>("service_data")

        if (data != null) {
            userName = data.getUserName()
            userID = data.getUserID()
            photoFileTempPath = data.getPhotoFileTempPath()
            activityFlag = data.getActivityFlag()
            saloonName = data.getSaloonName()
            captionText = data.getCaptionText()
        }

        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        deleteImageFile()

        Log.i(TAG,"service stopped")
    }

    private fun uploadImage(){

        photoFileTemp = File(photoFileTempPath)

        val dateFormat = SimpleDateFormat(CameraFragment.FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val imagePathFirestore: String = if (activityFlag == "profilePicture"){
            "User_Images/${userID}/${userName}_profilePicture.jpg"
        } else {
            "User_Images/${userID}/${userName}_$dateFormat.jpg"
        }

        val imageRef = storageRef.child(imagePathFirestore)
        val uploadTask = imageRef.putFile(Uri.fromFile(photoFileTemp))

        uploadTask.addOnSuccessListener{

            Toast.makeText(this,"Image uploaded successfully..", Toast.LENGTH_SHORT).show()

            Log.d(TAG, "Image is uploaded")

            if (activityFlag == "profilePicture"){
                deleteImageFile()

                Toast.makeText(this, "Profile Picture is updated. \n\n Restart app for changes to be visible.", Toast.LENGTH_SHORT).show()

            } else {
                updateFirestoreUserData(imagePathFirestore, dateFormat)
            }
        }

        uploadTask.addOnFailureListener{

            Log.d(TAG, "Image upload failed: ${it.message}")
            Toast.makeText(this,"Image upload failed..", Toast.LENGTH_SHORT).show()

            deleteImageFile()
        }

//        show progress in notification
        val notificationID = 123
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle("Image upload")
            setContentText("Upload is in progress")
            setSmallIcon(R.drawable.ic_bull)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

        NotificationManagerCompat.from(this).apply{
            builder.setProgress(100, 0, false)
            notify(notificationID, builder.build())

            uploadTask.addOnProgressListener {
                val progress = ((it.bytesTransferred.toFloat() / it.totalByteCount.toFloat()) * 100).toInt()
                builder.setProgress(100, progress, false)
                builder.priority = NotificationCompat.PRIORITY_LOW
                notify(notificationID, builder.build())
                Log.i(TAG, "upload in progress :$progress %")
            }

            uploadTask.addOnCompleteListener{

                if (it.isSuccessful){
                    builder.setContentText("Upload Complete")
                        .setProgress(0,0, false)
                        .priority = NotificationCompat.PRIORITY_DEFAULT
                    notify(notificationID, builder.build())
                } else {
                    builder.setContentText("Upload Failed")
                        .setProgress(0,0, false)
                        .priority = NotificationCompat.PRIORITY_DEFAULT
                    notify(notificationID, builder.build())
                }
            }
        }
    }

    private fun updateFirestoreUserData(imagePath: String, dateFormat: String){

        val fireStoreUrl = "gs://bull-saloon.appspot.com/"

        try {

            val photoUUID = UUID.randomUUID().toString()
            val mapData = hashMapOf<String, Any>("timestamp" to dateFormat,
                "image_ref" to "$fireStoreUrl$imagePath",
                "caption" to captionText,
                "nices_userid" to arrayListOf<String>(),
                "photoID" to photoUUID,
                "saloon_name" to saloonName,
                "user_id" to SingletonUserData.userData.user_id,
                "user_name" to SingletonUserData.userData.user_name)

            db.collection("Users")
                .document(auth.currentUser?.uid.toString())
                .collection("photos")
                .document(photoUUID)
                .set(mapData)
                .addOnSuccessListener {

                    Log.i(TAG, "Data updated")
                }
                .addOnFailureListener {

                    Log.i(TAG, "Data update Failed : ${it.message}")
                }
                .addOnCompleteListener {
                    deleteImageFile()
                }

        }catch (e: Exception){
            deleteImageFile()
            Log.i(TAG, "error: $e")
        }
    }

    private fun deleteImageFile(){

        if (this::photoFileTemp.isInitialized){
            photoFileTemp.delete()
        }
        Log.i(TAG, "PhotoFile Deleted")

    }

    companion object {
        private const val TAG = "TAGUploadImageToFirebaseService"
        private const val NOTIFICATION_CHANNEL_ID = "100"
    }
}