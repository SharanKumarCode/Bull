package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bullSaloon.bull.fragments.BullMagicListFragment
import com.bullSaloon.bull.databinding.ActivityBullMagicBinding
import com.bullSaloon.bull.fragments.YourProfileFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.UserBasicDataParcelable
import com.bullSaloon.bull.genericClasses.UserDataClass
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class BullMagicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBullMagicBinding

    private val EXTRA_INTENT: String = "userBasicData"
    private val EXTRA_INTENT_FRAGMENT_FLAG: String = "fragment_flag"
    private val FRAGMENT_FLAG: String = "yourProfileFragment"
    private val TAG: String = "TAGO"
    private lateinit var getIntent: UserBasicDataParcelable

    private lateinit var storage: FirebaseStorage

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBullMagicBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        getIntent = intent.getParcelableExtra(EXTRA_INTENT)!!
        val data = UserDataClass(getIntent.user_id, getIntent.user_name, getIntent.mobileNumber)
        val getFragmentFlag = intent.getStringExtra(EXTRA_INTENT_FRAGMENT_FLAG)

        storage = Firebase.storage

        if (getFragmentFlag == FRAGMENT_FLAG){
            launchYourProfileFragment()
        }

        binding.topAppBarBullMagic.setNavigationOnClickListener {
            binding.BullMagicDrawerLayout.openDrawer(Gravity.START)
        }

        val header = binding.BullMagicNavigationView.getHeaderView(0)
        setProfilePhoto(data.user_name, data.user_id)
        header.findViewById<TextView>(R.id.userNameNavigationHeaderTextView).text = data.user_name
        header.findViewById<TextView>(R.id.userMobileNavigationHeaderTextView).text = data.mobileNumber

        binding.BullMagicNavigationView.setNavigationItemSelectedListener {
            when(it.itemId){

                R.id.menuItemUserProfile ->{

                    binding.BullMagicDrawerLayout.closeDrawers()
                    launchYourProfileFragment()
                    true
                }

                R.id.menuItemBullMagic ->{

                    binding.BullMagicDrawerLayout.closeDrawers()
                    launchBullMagicListFragment()
                    true
                }

                R.id.menuItemAbout -> {
                    val builder = AlertDialog.Builder(this)
                    val appOwner = "Sharan Kumar"
                    val appVersion = "1.0"
                    val title = "About"
                    builder.setMessage("Created by $appOwner \n\nApp version: $appVersion")
                        .setTitle(title)
                        .setPositiveButton("OK"
                        ) { p0, _ -> p0?.dismiss() }
                        .show()
                    true
                }

                R.id.menuItemSignOut ->{
                    Firebase.auth.signOut()
                    startActivity(Intent(this,SplashScreenActivity::class.java))
                    true
                }

                else -> false
            }

        }

        launchBullMagicListFragment()

        binding.floatingActionCameraButton.setOnClickListener {
            launchCameraActivity()
        }

    }

    private fun launchCameraActivity(){
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(EXTRA_INTENT, getIntent)
        startActivity(intent)
    }

    private fun launchBullMagicListFragment(){
        val bullMagicListFragment = BullMagicListFragment()
        supportFragmentManager.beginTransaction().replace(R.id.BullMagicFragmentContainer, bullMagicListFragment).addToBackStack(null).commit()
    }

    private fun launchYourProfileFragment(){
        val yourProfileFragment = YourProfileFragment()
        supportFragmentManager.beginTransaction().replace(R.id.BullMagicFragmentContainer, yourProfileFragment).addToBackStack(null).commit()
    }

    private fun setProfilePhoto(userName: String, userId: String) {

        val firebaseCloudUrl = "gs://bull-saloon.appspot.com/"

        val userData = userName.replace("\\s".toRegex(), "_")
        val imageUrl = "User_Images/${userId}/${userData}_profilePicture.jpg"
        val imageDownloadUrl = "$firebaseCloudUrl$imageUrl"
        val headerImage = binding.BullMagicNavigationView.getHeaderView(0)
            .findViewById<ImageView>(R.id.userPhotoNavigationHeaderTextView)

        storage.reference.child(imageUrl).downloadUrl
            .addOnSuccessListener {

                GlideApp.with(this)
                    .load(storage.reference.child(imageUrl))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(headerImage)

            }
            .addOnFailureListener {

                GlideApp.with(this)
                    .load(R.drawable.ic_baseline_person_24)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(headerImage)

                Log.i("TAG", "storage check: Image does not exists - url - ${imageDownloadUrl}")

            }
    }

    fun fragmentGetIntentData(): UserBasicDataParcelable{
        return getIntent
    }
}