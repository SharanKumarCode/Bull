package com.bullSaloon.bull.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bullSaloon.bull.MainActivity
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentCameraBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var ACTIVITY_FLAG: String = "none"

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var outputDirectory: File
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraSelector: CameraSelector
    private lateinit var camera: Camera
    private lateinit var photoFileTemp: File
    private lateinit var userData: Map<String,String>
    private var cameraLens: Int = LENS_FRONT

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var loadingIconAnim: AnimatedVectorDrawable
    private var saloonName = ""
    private var captionText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        exitTransition = inflaterTrans.inflateTransition(R.transition.slide_left_to_right)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.actionBar?.hide()

        val data = SingletonUserData.userData
        ACTIVITY_FLAG = arguments?.getString("camera_purpose")!!

        storage = Firebase.storage
        storageRef = storage.reference
        db = Firebase.firestore
        auth = Firebase.auth

        userData = mapOf("user_name" to data.user_name.replace("\\s".toRegex(), "_"), "id" to data.user_id)
        binding.progressIndicatorCardView.visibility = View.GONE

//        Request camera permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        loadingIconAnim = binding.loadingIconCamera.drawable as AnimatedVectorDrawable
        outputDirectory = getOutputDirectory()

        startCamera(cameraLens)

        binding.saloonNameTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                when {
                    binding.saloonNameTextField.text?.length!! < 5 -> {
                        binding.saloonNameTextInputLayout.error = "minimum 5 characters required"
                    }
                    binding.saloonNameTextField.text?.length!! > 20 -> {
                        binding.saloonNameTextInputLayout.error = "restrict saloon name to 20 characters"
                    }
                    else -> {
                        binding.saloonNameTextInputLayout.error = null
                    }
                }
            }
        })

        binding.captionTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                when {
                    binding.captionTextField.text?.length!! < 1 -> {
                        binding.captionTextInputLayout.error = "minimum 1 character required"
                    }
                    binding.captionTextField.text?.length!! > 50 -> {
                        binding.captionTextInputLayout.error = "restrict caption name to 50 characters"
                    }
                    else -> {
                        binding.captionTextInputLayout.error = null
                    }
                }
            }
        })

        binding.takePhotoButton.setOnClickListener{
            setButtonVisibility(VIEW_VISIBILITY.INITIAL_INVISIBLE)

//            starting loading icon
            startLoadingIcon()
            takeImage()
        }

        binding.changeCameraButton.setOnClickListener {
            if (cameraLens == LENS_FRONT){
                cameraLens = LENS_BACK
                startCamera(cameraLens)
            } else {
                cameraLens = LENS_FRONT
                startCamera(cameraLens)
            }
        }

        binding.UploadImageFirebaseButton.setOnClickListener{

            saloonName = ""
            captionText = ""

            if (ACTIVITY_FLAG == "profilePicture"){

//                show progress
                binding.progressIndicatorCardView.visibility = View.VISIBLE
                uploadImage()
            } else {

//                show popup to enter image data
                binding.popUpImageDataLayout.visibility = View.VISIBLE
            }
        }

        binding.OkButton.setOnClickListener {

            val saloonLength = binding.saloonNameTextField.text?.length!!
            val captionLength = binding.captionTextField.text?.length!!

            if ((saloonLength in 6..19) || (captionLength in 2..49)){

                saloonName = binding.saloonNameTextField.text.toString()
                captionText = binding.captionTextField.text.toString()
                binding.popUpImageDataLayout.visibility = View.GONE

                val imm = getSystemService(this.requireContext(), INPUT_METHOD_SERVICE::class.java) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)

                binding.progressIndicatorCardView.visibility = View.VISIBLE
                uploadImage()
            }
        }

        binding.SkipButton.setOnClickListener {

            binding.popUpImageDataLayout.visibility = View.GONE
            binding.progressIndicatorCardView.visibility = View.VISIBLE
            uploadImage()
        }

        binding.CancelImageButton.setOnClickListener{
            deleteImageFile()
        }

        binding.viewFinder.setOnTouchListener { _, motionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    Log.i(TAG,"tap to focus is called")
                    tapToFocus()
                    return@setOnTouchListener true
                }
                else-> return@setOnTouchListener false
            }
        }

        binding.backCameraButton.setOnClickListener {
            Log.i("CameraX", "clicked back : ${SingletonUserData.userData}")
            deleteImageFile()
            val navHostFragment = this.parentFragmentManager.findFragmentById(R.id.fragment)

            navHostFragment?.findNavController()?.navigate(R.id.action_cameraFragment_to_bullMagicFragment)
        }

        binding.uploadFromGalleryButton.setOnClickListener {
            setButtonVisibility(VIEW_VISIBILITY.INITIAL_INVISIBLE)
            getImageFromGallery()
        }

        binding.closepopUpImageDataLayoutButton.setOnClickListener {
            saloonName = ""
            captionText = ""
            binding.saloonNameTextField.setText("")
            binding.captionTextField.setText("")

            binding.popUpImageDataLayout.visibility = View.GONE
        }
    }

    private fun startCamera(cameraLens: Int){
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider, cameraLens)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onPause() {
        super.onPause()

        Log.i(TAG, "backstack onPause :${activity?.supportFragmentManager?.findFragmentById(R.id.fragment)?.findNavController()?.backStack?.last?.destination}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::photoFileTemp.isInitialized){
            photoFileTemp.delete()
        }
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider, cameraLens: Int){
        cameraProvider.unbindAll()

        val preview: Preview = Preview.Builder()
            .build()

        cameraSelector = CameraSelector.Builder().requireLensFacing(cameraLens).build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector,imageCapture, preview)

        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

    }

    private fun takeImage(){

        photoFileTemp = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFileTemp).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    Log.d(TAG, "Image is stored in file")

                    val imgBitmap = BitmapFactory.decodeFile(photoFileTemp.absolutePath)

                    val ei = ExifInterface(photoFileTemp).rotationDegrees

                    val matrix = Matrix()
                    matrix.postRotate(ei.toFloat())

                    //rotating image to correct orientation
                    if (cameraLens == LENS_FRONT){
                        matrix.preScale(1.0F, -1.0F)
                    }

                    val rotatedImgBitmap = Bitmap.createBitmap(imgBitmap, 0, 0, imgBitmap.width, imgBitmap.height, matrix, true)

                    stopLoadingIcon()

                    val bg = BitmapDrawable(resources,rotatedImgBitmap)

                    val aspectRatio = rotatedImgBitmap.width.toFloat() / rotatedImgBitmap.height.toFloat()
                    val width = Resources.getSystem().displayMetrics.widthPixels
                    val height = (width / aspectRatio).toInt()

                    Glide.with(binding.root.context)
                        .load(bg)
                        .apply(RequestOptions.overrideOf(width, height))
                        .placeholder(R.drawable.ic_bull)
                        .into(binding.capturedImageView)
                }

                override fun onError(exception: ImageCaptureException) {
                    setButtonVisibility(VIEW_VISIBILITY.UPLOAD_COMPLETE_VISIBILITY)
                    Log.d(TAG, "error occurred: $exception")
                }
            }
        )
    }

    private fun tapToFocus(){
        val previewView = binding.viewFinder
        val x = (previewView.width/2).toFloat()
        val y = (previewView.height/2).toFloat()
        Log.i(TAG,"x: $x, y:$y")
        val meteringPoint = DisplayOrientedMeteringPointFactory(
            previewView.display,
            camera.cameraInfo,
            previewView.width.toFloat(),
            previewView.height.toFloat()).createPoint(x,y)

        val action = FocusMeteringAction.Builder(meteringPoint).build()
        camera.cameraControl.startFocusAndMetering(action)
    }

    private fun getImageFromGallery(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        resultLauncher.launch(Intent.createChooser(intent, "Select an image"))
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            val imageUri = it.data?.data

            uploadImageFromGallery(imageUri!!)

            Log.i("CameraX","Obtained image: $imageUri")
        }
    }

    private fun uploadImageFromGallery(imageUri: Uri){

        binding.capturedImageViewLayout.visibility = View.VISIBLE

        imageUri.let { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(activity?.contentResolver!!, imageUri)
            val bitmap = ImageDecoder.decodeBitmap(source)

            val bg = BitmapDrawable(resources, bitmap)

            photoFileTemp = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
            )

            val fileOut = FileOutputStream(photoFileTemp)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOut)
            fileOut.flush()
            fileOut.close()

            binding.capturedImageView.setImageDrawable(bg)
            }
        }
    }

    private fun uploadImage(){

        val dateFormat = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val imagePathFirestore: String = if (ACTIVITY_FLAG == "profilePicture"){
            "User_Images/${userData["id"]}/${userData["user_name"]}_profilePicture.jpg"
        } else {
            "User_Images/${userData["id"]}/${userData["user_name"]}_$dateFormat.jpg"
        }

        val imageRef = storageRef.child(imagePathFirestore)
        val uploadTask = imageRef.putFile(Uri.fromFile(photoFileTemp))

        uploadTask.addOnSuccessListener{

            binding.progressIndicatorText.text = resources.getString(R.string.textProgressIndicator, "100 %")
            binding.progressHorizontal.progress = 100

            Toast.makeText(requireContext(),"Image uploaded..", Toast.LENGTH_SHORT).show()

            setButtonVisibility(VIEW_VISIBILITY.UPLOAD_COMPLETE_VISIBILITY)

            Log.d(TAG, "Image is uploaded")

            if (ACTIVITY_FLAG == "profilePicture"){
                deleteImageFile()

                (activity as MainActivity).updateProfilePicOutsideMain()

                val navHostFragment = this.parentFragment?.childFragmentManager?.findFragmentById(R.id.fragment)
                val navController = navHostFragment?.findNavController()
                navController?.navigate(R.id.action_cameraFragment_to_yourProfileFragment)

            } else {
                updateFirestoreUserData(imagePathFirestore, dateFormat)
            }
        }

        uploadTask.addOnFailureListener{

            setButtonVisibility(VIEW_VISIBILITY.UPLOAD_COMPLETE_VISIBILITY)

            Log.d(TAG, "Image upload failed: ${it.message}")
            Toast.makeText(requireContext(),"Image upload failed..", Toast.LENGTH_SHORT).show()

            deleteImageFile()
        }

        uploadTask.addOnProgressListener {
            val progress = ((it.bytesTransferred.toFloat() / it.totalByteCount.toFloat()) * 100).toInt()
            binding.progressHorizontal.progress = progress
            binding.progressIndicatorText.text = resources.getString(R.string.textProgressIndicator, "$progress %")
        }
    }

    private fun updateFirestoreUserData(imagePath: String, dateFormat: String){

        val fireStoreUrl = "gs://bull-saloon.appspot.com/"

        try {

            db.collection("Users")
                .document(auth.currentUser?.uid.toString())
                .get()
                .addOnSuccessListener { it ->
                    if (it.exists()){
                        if (it.id == auth.currentUser?.uid.toString()){
                            val mapData = hashMapOf<String, Any>("timestamp" to dateFormat, "image_ref" to "$fireStoreUrl$imagePath", "caption" to captionText, "saloon_name" to saloonName)
                            val photoUUID = UUID.randomUUID().toString()

                            if (it.get("photos") == null){
                                val photoData = hashMapOf<String, Map<String,Any>>(photoUUID to mapData)
                                db.collection("Users")
                                    .document(it.id)
                                    .update("photos", photoData)
                                    .addOnSuccessListener {
                                        deleteImageFile()
                                        Log.i(TAG, "Data updated")
                                    }
                                    .addOnFailureListener {

                                        stopLoadingIcon()
                                        Log.i(TAG, "Data update Failed : ${it.message}")
                                    }
                            } else {
                                db.collection("Users")
                                    .document(it.id)
                                    .update("photos.${photoUUID}", mapData)
                                    .addOnSuccessListener {
                                        stopLoadingIcon()
                                        deleteImageFile()
                                        Log.i(TAG, "Data updated")
                                    }
                                    .addOnFailureListener {
                                        stopLoadingIcon()
                                        Log.i(TAG, "Data update Failed : ${it.message}")
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    stopLoadingIcon()
                    Log.i(TAG, "User Data Not Found")
                }
        }catch (e: Exception){
            stopLoadingIcon()
            Log.i(TAG, "error: e")
        }
    }

    private fun deleteImageFile(){

        if (this::photoFileTemp.isInitialized){
            photoFileTemp.delete()
        }
        Log.i(TAG, "PhotoFile Deleted")

        setButtonVisibility(VIEW_VISIBILITY.UPLOAD_COMPLETE_VISIBILITY)

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
        binding.capturedImageViewLayout.visibility = View.VISIBLE
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireContext().filesDir
    }

    private fun setButtonVisibility(flag: String){
        if (flag == VIEW_VISIBILITY.INITIAL_INVISIBLE){
            binding.takePhotoButton.visibility = View.INVISIBLE
            binding.changeCameraButton.visibility = View.INVISIBLE
            binding.uploadFromGalleryButton.visibility = View.INVISIBLE

        } else if (flag == VIEW_VISIBILITY.UPLOAD_COMPLETE_VISIBILITY){
            binding.progressIndicatorCardView.visibility = View.GONE
            binding.capturedImageViewLayout.visibility = View.GONE
            binding.takePhotoButton.visibility = View.VISIBLE
            binding.changeCameraButton.visibility = View.VISIBLE
            binding.uploadFromGalleryButton.visibility = View.VISIBLE

        }

    }

    companion object {
        private const val TAG = "CameraX"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val LENS_FRONT = CameraSelector.LENS_FACING_FRONT
        private const val LENS_BACK = CameraSelector.LENS_FACING_BACK
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private object VIEW_VISIBILITY {
            const val INITIAL_INVISIBLE = "initial_invisible"
            const val UPLOAD_COMPLETE_VISIBILITY = "upload_complete_visibility"
        }
    }





}