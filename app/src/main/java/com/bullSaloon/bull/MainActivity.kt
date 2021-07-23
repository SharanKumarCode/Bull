package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.core.view.marginBottom
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.bullSaloon.bull.databinding.ActivityMainBinding
import com.bullSaloon.bull.fragments.MainFragment
import com.bullSaloon.bull.fragments.bullMagic.BullMagicListFragment
import com.bullSaloon.bull.fragments.yourProfile.YourProfileFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG: String = "TAG"

    private lateinit var storage: FirebaseStorage
    private lateinit var userName: String
    private lateinit var userID: String

    private lateinit var controller: NavController


    @SuppressLint("WrongConstant", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        storage = Firebase.storage

//        get basic user data
        val data = SingletonUserData.userData
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

        navController?.addOnDestinationChangedListener { controller, destination, arguments ->
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

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val navItemImage = binding.bottomNavigationView.menu.findItem(R.id.yourProfileFragment)
        binding.bottomNavigationView.itemIconTintList = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            navItemImage.iconTintList = null
            navItemImage.iconTintMode = null
        }

        GlideApp.with(this)
            .asBitmap()
            .load(SingletonUserData.userData.profilePicBitmap)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(object : CustomTarget<Bitmap>(60, 60){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    navItemImage.icon = BitmapDrawable(resources, resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    navItemImage.icon = placeholder
                }
            })

        return super.onPrepareOptionsMenu(menu)
    }
}