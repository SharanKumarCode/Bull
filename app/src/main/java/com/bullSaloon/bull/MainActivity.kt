package com.bullSaloon.bull

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bullSaloon.bull.databinding.ActivityMainBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG: String = "TAG"

    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        storage = Firebase.storage

//        get basic user data
        val data = SingletonUserData.userData

        SingletonUserData.getProfilePic(this)
        val userDataModel = ViewModelProvider(this).get(UserDataViewModel::class.java)

        userDataModel.assignBasicUserData(data)

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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val navItemImage = binding.bottomNavigationView.menu.findItem(R.id.yourProfileFragment)
        binding.bottomNavigationView.itemIconTintList = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            navItemImage.iconTintList = null
            navItemImage.iconTintMode = null
        }

        val dataViewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        dataViewModel.getProfilePic().observe(LifecycleOwner { lifecycle },{
            var data = it

            if (it == null){
                data = BitmapFactory.decodeResource(this.resources, R.drawable.ic_baseline_person_black_40)
            }

            Log.i("TAGProfile", "user profile updated main activity : ${data}")

            GlideApp.with(this)
                .asBitmap()
                .load(data)
                .circleCrop()
                .placeholder(R.drawable.ic_baseline_person_black_40)
                .fallback(R.drawable.ic_baseline_person_black_40)
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

    fun updateProfilePicOutsideMain(){
        SingletonUserData.getProfilePic(this)
    }
}