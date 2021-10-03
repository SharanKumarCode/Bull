package com.bullSaloon.bull

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bullSaloon.bull.databinding.ActivityMainBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.login.LoginManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.GeoPoint


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationManager: LocationManager
    private lateinit var animate: AnimatedVectorDrawable
    private var userLatitude = 0.0
    private var userLongitude = 0.0
    private var resumeFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        createNotificationChannel()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            checkLocationEnabled()
        }

//        get basic user data
        val data = SingletonUserData.userData

        SingletonUserData.getProfilePic(this)
        val userDataModel = ViewModelProvider(this).get(UserDataViewModel::class.java)

        userDataModel.assignBasicUserData(data)

//        setting bottom navigation menu
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment)
        val navController = navHostFragment?.findNavController()

        if (navController != null) {
            binding.bottomNavigationView.setupWithNavController(navController)
        }

        binding.fab.setOnClickListener {
            navController?.navigate(R.id.cameraFragment)
        }

//        disable reselection of bottomNavigationView items
        binding.bottomNavigationView.setOnItemReselectedListener(object : NavigationBarView.OnItemReselectedListener{
            override fun onNavigationItemReselected(item: MenuItem) {

            }
        })

        binding.topAppBar.setOnMenuItemClickListener {

            when(it!!.itemId)
            {
                R.id.menuItemSettings -> {
                    navController?.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }

        navController?.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.cameraFragment){
                binding.bottomAppBar.performHide()
                binding.fab.hide()
                binding.topAppBar.visibility = View.GONE

                val param = binding.fragment.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(0,0,0,0)
                binding.fragment.layoutParams = param

            } else {
                binding.bottomAppBar.performShow()
                binding.fab.show()
                binding.topAppBar.visibility = View.VISIBLE

                val param = binding.fragment.layoutParams as ViewGroup.MarginLayoutParams
                val tv = TypedValue()
                if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                    param.setMargins(0,actionBarHeight,0,actionBarHeight)
                    binding.fragment.layoutParams = param
                }

            }
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
                Toast.makeText(this,
                    "Location permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            } else {
                checkLocationEnabled()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationEnabled(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (LocationManagerCompat.isLocationEnabled(locationManager)){
            getLocation()
        } else {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_turn_on_location)
            val enableButton = dialog.findViewById<MaterialButton>(R.id.dialogLocationEnableButton)
            val denyButton = dialog.findViewById<Button>(R.id.dialogLocationDenyButton)

            enableButton.setOnClickListener {
                dialog.hide()
                resumeFlag = true
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            denyButton.setOnClickListener {
                dialog.hide()
            }

            dialog.show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_fetching_location_data)
        val loadText = dialog.findViewById<TextView>(R.id.dialogLocationPleaseWaitText)
        val okButton = dialog.findViewById<MaterialButton>(R.id.dialogLocationPleaseWaitOKButton)
        val loadingIcon = dialog.findViewById<ImageView>(R.id.dialogGPSLoadingIconImage)

        dialog.show()

        //starting loading icon
        loadingIcon.visibility = View.VISIBLE
        animate = loadingIcon.drawable as AnimatedVectorDrawable

        AnimatedVectorDrawableCompat.registerAnimationCallback(animate, object: Animatable2Compat.AnimationCallback(){
            override fun onAnimationEnd(drawable: Drawable?) {
                super.onAnimationEnd(drawable)

                animate.start()
            }
        })

        animate.start()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val cancellation = androidx.core.os.CancellationSignal()

        val countDownTimer = object : CountDownTimer(5000, 1000){
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                okButton.visibility = View.VISIBLE
                loadingIcon.visibility = View.GONE
                loadText.text = resources.getString(R.string.textFetchingLocationDataFailed)
                cancellation.cancel()

                okButton.setOnClickListener {
                    animate.stop()
                    dialog.hide()
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }
        countDownTimer.start()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationConsumer = Consumer<Location> {
            Log.i(TAG, "latitude : ${it?.latitude}")
            Log.i(TAG, "longitude : ${it?.longitude}")
            Log.i(TAG, "current location provider : ${it?.provider}")

            val dataViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

            if (userLatitude != 0.0 || userLongitude != 0.0){
                dataViewModel.assignUserLocationData(GeoPoint(userLatitude, userLongitude))
            } else {
                userLatitude = it.latitude
                userLongitude = it.longitude
                dataViewModel.assignUserLocationData(GeoPoint(userLatitude, userLongitude))
            }

            countDownTimer.cancel()
            cancellation.cancel()
            animate.stop()
            dialog.hide()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        LocationManagerCompat.getCurrentLocation(locationManager, LocationManager.NETWORK_PROVIDER, cancellation, ContextCompat.getMainExecutor(this), locationConsumer)
        LocationManagerCompat.getCurrentLocation(locationManager, LocationManager.GPS_PROVIDER, cancellation, ContextCompat.getMainExecutor(this), locationConsumer)

    }

    override fun onResume() {
        super.onResume()

        if (resumeFlag){
            getLocation()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val navItemImage = binding.bottomNavigationView.menu.findItem(R.id.yourProfileFragment)
        binding.bottomNavigationView.itemIconTintList = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            navItemImage.iconTintList = null
            navItemImage.iconTintMode = null
        }

        val dataViewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        dataViewModel.getProfilePic().observe( { lifecycle },{
            var data = it

            if (it == null){
                data = BitmapFactory.decodeResource(this.resources, R.drawable.ic_baseline_person_black_40)
            }

            Log.i(TAG, "user profile updated main activity : $data")

            GlideApp.with(this)
                .asBitmap()
                .load(data)
                .circleCrop()
                .placeholder(R.drawable.ic_baseline_delete_24)
                .fallback(R.drawable.ic_baseline_delete_40)
                .into(object : CustomTarget<Bitmap>(60, 60){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        navItemImage.icon = BitmapDrawable(resources, resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        navItemImage.icon = placeholder
                    }
                })
        })

        return super.onPrepareOptionsMenu(menu)
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Bull App"
            val descriptionText = "Notification channel for Bull Saloon App"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name , importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateProfilePicOutsideMain(){
        SingletonUserData.getProfilePic(this)
    }

    companion object {
        private const val TAG = "TAGMainActivity"
        private const val NOTIFICATION_CHANNEL_ID = "100"
        private const val REQUEST_CODE_PERMISSIONS = 123
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    }
}