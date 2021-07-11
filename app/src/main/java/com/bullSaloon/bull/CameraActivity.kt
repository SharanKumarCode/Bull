package com.bullSaloon.bull


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bullSaloon.bull.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var outputDirectory: File
    private lateinit var imageCapture: ImageCapture
    private lateinit var photoFileTemp: File
    private lateinit var userData: Map<String,String>

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var loadingIconAnim: AnimatedVectorDrawable

    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        storage = Firebase.storage
        storageRef = storage.reference
        db = Firebase.firestore
        auth = Firebase.auth

        userData = mapOf("user_name" to "Sharan", "last_name" to "Kumar", "id" to "100124041995")

        loadingIconAnim = binding.loadingIconCamera.drawable as AnimatedVectorDrawable
        outputDirectory = getOutputDirectory()

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))

        binding.takePhotoButton.setOnClickListener{
            binding.takePhotoButton.visibility = View.INVISIBLE

            //starting loading icon
            startLoadingIcon()
            takeImage()
        }

        binding.UploadImageFirebaseButton.setOnClickListener{

            //starting loading icon
            startLoadingIcon()
            uploadImage()
        }

        binding.CancelImageButton.setOnClickListener{
            deleteImageFile()
        }
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider){
        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        val camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector,imageCapture, preview)

        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

    }

    private fun takeImage(){

        photoFileTemp = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFileTemp).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    Log.d("Firestore", "Image is stored in file")

                    val imgBitmap = BitmapFactory.decodeFile(photoFileTemp.absolutePath)
                    val matrix = Matrix()
                    matrix.postRotate(90F)
                    matrix.preScale(-1.0F, 1.0F)
                    val rotatedImgBitmap = Bitmap.createBitmap(imgBitmap, 0, 0, imgBitmap.width, imgBitmap.height, matrix, true)

                    stopLoadingIcon()

                    val bg = BitmapDrawable(resources,rotatedImgBitmap)
                    binding.capturedImageView.background = bg

                }

                override fun onError(exception: ImageCaptureException) {
                    binding.takePhotoButton.visibility = View.VISIBLE
                    Log.d("Firestore", "error occurred: $exception")
                }
            }
        )
    }

    private fun uploadImage(){


        val dateFormat = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val imagePathFirestore = "User_Images/${userData["id"]}/${userData["first_name"]}_${userData["last_name"]}_$dateFormat.jpg"
        val imageRef = storageRef.child(imagePathFirestore)
        val uploadTask = imageRef.putFile(Uri.fromFile(photoFileTemp))

        uploadTask.addOnCompleteListener{

            Log.d("Firestore", "Image is uploaded")

            binding.capturedImageView.visibility = View.GONE
            binding.takePhotoButton.visibility = View.VISIBLE

            updateFirestoreUserData(imagePathFirestore)
        }
        uploadTask.addOnFailureListener{

            binding.capturedImageView.visibility = View.GONE
            binding.takePhotoButton.visibility = View.VISIBLE
            Log.d("Firestore", "Image upload failed")
        }
    }

    private fun updateFirestoreUserData(imagePath: String){
        val firestoreUrl = "gs://bull-saloon.appspot.com/"

        try {

//            db.collection("Users")
//                .document(auth.currentUser?.uid.toString())
//                .get()
//                .addOnCompleteListener {
//                    if (it.result?.exists()!!){
//                        if (it.result!!.id == auth.currentUser?.uid.toString()){
//                            val mapData = hashMapOf<>("timestamp" to Timestamp.now(), )
//                            Log.i("Firestore","document name: ${it.result!!.id}")
//                            db.collection("Users")
//                                .document(it.result!!.id)
//                                .update()
//                        }
//                    }
//                }

            db.collection("User_data")
                .document()
                .get()
                .addOnCompleteListener{ document ->
                        if (document.result!!.get("id") == userData["id"]){
                            Log.i("Firestore","document name: ${document.result!!.id}")

                            db.collection("User_Data")
                                .document(document.result!!.id)
                                .update("images",FieldValue.arrayUnion("$firestoreUrl$imagePath"))
                                .addOnCompleteListener {

                                    stopLoadingIcon()
                                    deleteImageFile()
                                    Log.i("Firestore", "Data updated")
                                }
                                .addOnFailureListener {

                                    stopLoadingIcon()
                                    Log.i("Firestore", "Data update Failed")
                                }
                        }
                }
                .addOnFailureListener {
                    stopLoadingIcon()
                    Log.i("Firestore", "User Data Not Found")
                }
        }catch (e: Exception){
            stopLoadingIcon()
            Log.i("Firestore", "error: e")
        }
    }


    private fun deleteImageFile(){
        photoFileTemp.delete()
        Log.i("Firestore", "PhotoFile Deleted")

        binding.capturedImageView.visibility = View.GONE
        binding.takePhotoButton.visibility = View.VISIBLE

    }

    private fun startLoadingIcon(){

        binding.loadingIconCamera.visibility = View.VISIBLE
        loadingIconAnim = binding.loadingIconCamera.drawable as AnimatedVectorDrawable

        AnimatedVectorDrawableCompat.registerAnimationCallback(loadingIconAnim, object: Animatable2Compat.AnimationCallback(){
            override fun onAnimationEnd(drawable: Drawable?) {
                super.onAnimationEnd(drawable)

                loadingIconAnim.start()
            }
        })

        loadingIconAnim.start()
    }

    private fun stopLoadingIcon(){
        loadingIconAnim.stop()
        binding.loadingIconCamera.visibility = View.GONE
        binding.capturedImageView.visibility = View.VISIBLE
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

}