package com.bullSaloon.bull.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Log
import android.util.Size
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.FragmentCameraBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.UploadImageServicePayload
import com.bullSaloon.bull.services.UploadImageToFirebaseService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var outputDirectory: File
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: Camera
    private lateinit var photoFileTemp: File
    private lateinit var userData: Map<String,String>
    private var cameraLens: Int = LENS_FRONT

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var loadingIconAnim: AnimatedVectorDrawable
    private lateinit var snackBar: Snackbar
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

        storageRef = SingletonInstances.getStorageReference()
        db = SingletonInstances.getFireStoreInstance()
        auth = SingletonInstances.getAuthInstance()

        userData = mapOf("user_name" to data.user_name.replace("\\s".toRegex(), "_"), "id" to data.user_id)

        snackBar = Snackbar
            .make(binding.takePhotoButton, "Image is being uploaded in the background", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(requireContext().getColor(R.color.teal_200))
            .setTextColor(requireContext().getColor(R.color.black))
            .setAnimationMode(ANIMATION_MODE_SLIDE)

        snackBar.setAction(R.string.buttonDismiss) {
            snackBar.dismiss()
        }

//        Request camera permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        loadingIconAnim = binding.loadingIconCamera.drawable as AnimatedVectorDrawable
        outputDirectory = getOutputDirectory()

        startCamera(cameraLens)

        binding.takePhotoButton.setOnClickListener{
            setButtonVisibility(ViewVisibility.INITIAL_INVISIBLE)

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

                launchUploadImageService()
            } else {

//                show popup to enter image data
                launchDialogCaption()
            }
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
            setButtonVisibility(ViewVisibility.INITIAL_INVISIBLE)
            getImageFromGallery()
        }
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

        _binding = null
        cameraExecutor.shutdown()
    }

    private fun launchDialogCaption(){

        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_camera_caption_saloon)

        val okButton = dialog.findViewById<MaterialButton>(R.id.OkButton)
        val skipButton = dialog.findViewById<MaterialButton>(R.id.SkipButton)
        val closeButton = dialog.findViewById<ImageButton>(R.id.closeDialogButton)
        val saloonNameTextField = dialog.findViewById<TextInputEditText>(R.id.saloonNameTextField)
        val captionTextField = dialog.findViewById<TextInputEditText>(R.id.captionTextField)
        val saloonNameTextInputLayout = dialog.findViewById<TextInputLayout>(R.id.saloonNameTextInputLayout)
        val captionTextInputLayout = dialog.findViewById<TextInputLayout>(R.id.captionTextInputLayout)

        saloonNameTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                when {
                    saloonNameTextField.text?.length!! < 5 -> {
                        saloonNameTextInputLayout.error = "minimum 5 characters required"
                    }
                    saloonNameTextField.text?.length!! > 20 -> {
                        saloonNameTextInputLayout.error = "restrict saloon name to 20 characters"
                    }
                    else -> {
                        saloonNameTextInputLayout.error = null
                    }
                }
            }
        })

        captionTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                when {
                    captionTextField.text?.length!! < 1 -> {
                        captionTextInputLayout.error = "minimum 1 character required"
                    }
                    captionTextField.text?.length!! > 50 -> {
                        captionTextInputLayout.error = "restrict caption name to 50 characters"
                    }
                    else -> {
                        captionTextInputLayout.error = null
                    }
                }
            }
        })

        okButton.setOnClickListener {

            val saloonLength = saloonNameTextField.text?.length!!
            val captionLength = captionTextField.text?.length!!

            if ((saloonLength in 6..19) || (captionLength in 2..49)){

                saloonName = saloonNameTextField.text.toString()
                val captionString = captionTextField.text.toString()
                captionText = URLEncoder.encode(captionString, "UTF-8")

                activity?.window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

                launchUploadImageService()
                snackBar.show()

                dialog.hide()
            }
        }

        skipButton.setOnClickListener {

            launchUploadImageService()

            snackBar.show()
            dialog.hide()
        }

        closeButton.setOnClickListener {
            saloonName = ""
            captionText = ""
            saloonNameTextField.setText("")
            captionTextField.setText("")

            dialog.hide()
        }

        dialog.show()

    }

    private fun startCamera(cameraLens: Int){
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener( {
            val cameraProvider = cameraProviderFuture.get()
            bindUseCases(cameraProvider, cameraLens)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindUseCases(cameraProvider : ProcessCameraProvider, cameraLens: Int){
        cameraProvider.unbindAll()

        val preview: Preview = Preview.Builder()
            .build()

        cameraSelector = CameraSelector.Builder().requireLensFacing(cameraLens).build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280,720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, {
            val rotationDegrees = it.imageInfo.rotationDegrees



            it.close()
        })

        camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector,imageCapture, imageAnalysis, preview)

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
                    setButtonVisibility(ViewVisibility.UPLOAD_COMPLETE_VISIBILITY)
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

    private fun launchUploadImageService(){
        try {
            val serviceIntent = Intent(requireContext(), UploadImageToFirebaseService::class.java)
            val serviceData = UploadImageServicePayload(
                userData["id"]!!,
                userData["user_name"]!!,
                photoFileTemp.absolutePath,
                saloonName,
                captionText,
                ACTIVITY_FLAG)
            val servicePayload = Bundle()
            servicePayload.putParcelable("service_data", serviceData)
            serviceIntent.putExtra("service_payload", servicePayload)
            requireActivity().startService(serviceIntent)

            setButtonVisibility(ViewVisibility.UPLOAD_COMPLETE_VISIBILITY)

        } catch (e: Exception){

            setButtonVisibility(ViewVisibility.UPLOAD_COMPLETE_VISIBILITY)
            Log.i("TAGUploadImageToFirebaseService", "error in service : ${e.message}")
            Toast.makeText(requireActivity(), "Error in uploading Image.. Please try again later", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteImageFile(){

        if (this::photoFileTemp.isInitialized){
            photoFileTemp.delete()
        }

        Log.i(TAG, "PhotoFile Deleted")

        setButtonVisibility(ViewVisibility.UPLOAD_COMPLETE_VISIBILITY)

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
        val mediaDir = requireContext().getExternalFilesDir(null).let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir.exists())
            mediaDir else requireContext().filesDir
    }

    private fun setButtonVisibility(flag: String){
        if (flag == ViewVisibility.INITIAL_INVISIBLE){
            binding.takePhotoButton.visibility = View.INVISIBLE
            binding.changeCameraButton.visibility = View.INVISIBLE
            binding.uploadFromGalleryButton.visibility = View.INVISIBLE
            binding.backCameraButton.visibility = View.INVISIBLE

        } else if (flag == ViewVisibility.UPLOAD_COMPLETE_VISIBILITY){
            binding.capturedImageViewLayout.visibility = View.GONE
            binding.takePhotoButton.visibility = View.VISIBLE
            binding.changeCameraButton.visibility = View.VISIBLE
            binding.uploadFromGalleryButton.visibility = View.VISIBLE
            binding.backCameraButton.visibility = View.VISIBLE

        }

    }

    companion object {
        private const val TAG = "CameraX"
        private var ACTIVITY_FLAG: String = "none"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val LENS_FRONT = CameraSelector.LENS_FACING_FRONT
        private const val LENS_BACK = CameraSelector.LENS_FACING_BACK
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private object ViewVisibility {
            const val INITIAL_INVISIBLE = "initial_invisible"
            const val UPLOAD_COMPLETE_VISIBILITY = "upload_complete_visibility"
        }
    }





}