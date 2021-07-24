package com.bullSaloon.bull.fragments.yourProfile

import android.annotation.SuppressLint
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bullSaloon.bull.R
import com.bullSaloon.bull.adapters.YourProfileViewPagerAdapter
import com.bullSaloon.bull.databinding.FragmentYourProfileBinding
import com.bullSaloon.bull.fragments.CameraFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class YourProfileFragment : Fragment() {

    private var _binding: FragmentYourProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var data: UserDataClass

    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        exitTransition = inflaterTrans.inflateTransition(R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYourProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = Firebase.storage

        data = SingletonUserData.userData

        binding.userNameYourProfileTextView.text = data.user_name
        binding.mobileNumberYourProfileTextView.text = data.mobileNumber

        binding.userImageEditButton.setOnClickListener {
            showPopUp(it)
        }

        setProfilePhoto()

        val viewPagerAdapter = YourProfileViewPagerAdapter(this)
        binding.ViewPagerYourProfile.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabInputYourProfile, binding.ViewPagerYourProfile){tab, position->
            when(position){
                0->{
                    tab.text = "Photos"
                    tab.setIcon(R.drawable.ic_baseline_photo_library_24)
                }
                1->{
                    tab.text = "Comments"
                    tab.setIcon(R.drawable.ic_baseline_comment_24)
                }
                2->{
                    tab.text = "Nices"
                    tab.setIcon(R.drawable.ic_nice_blank_icon)
                }
                3->{
                    tab.text = "Reviews"
                    tab.setIcon(R.drawable.ic_baseline_rate_review_24)
                }
            }
        }.attach()

        binding.tabInputYourProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        binding.ViewPagerYourProfile.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewPagerAdapter.notifyDataSetChanged()
            }
        })



    }

    override fun onResume() {
        super.onResume()
        setProfilePhoto()
    }

    @SuppressLint("RestrictedApi")
    private fun showPopUp(v: View){
        val popUp = PopupMenu(requireContext(), v)
        popUp.menuInflater.inflate(R.menu.popup_user_picture_edit_menu, popUp.menu)

        popUp.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.popUpMenuItemTakePicture ->{
                    val navHostFragment = this.parentFragment?.childFragmentManager?.findFragmentById(R.id.fragment)
                    val navController = navHostFragment?.findNavController()
                    navController?.navigate(R.id.action_yourProfileFragment_to_cameraFragment)
                    true
                }
                R.id.popUpMenuItemRemovePhoto ->{
                    removePhoto()
                    true
                }
                else -> false
            }
        }

        if (popUp.menu is MenuBuilder) {
            val menuBuilder = popUp.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 10F ,resources.displayMetrics)
                        .toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx,0)
                    } else {
                        item.icon =
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }
        popUp.show()
    }

    private fun setProfilePhoto(){

        if (SingletonUserData.userData.profilePicBitmap != null){

            GlideApp.with(this)
                .load(SingletonUserData.userData.profilePicBitmap)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.userPhotoYourProfileTextView)

            Log.i("TAGProfile","User profile updated: ${SingletonUserData.userData.profilePicBitmap.toString()}")

        } else {

            GlideApp.with(this)
                .load(R.drawable.ic_baseline_person_24)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(binding.userPhotoYourProfileTextView)
        }
    }

    private fun removePhoto(){

        val userData2 = mapOf("user_name" to data.user_name.replace("\\s".toRegex(), "_"), "id" to data.user_id)
        val imageUrl2 = "User_Images/${data.user_id}/${userData2["user_name"]}_profilePicture.jpg"

        val imageRef = storage.reference.child(imageUrl2)

        imageRef.delete()
            .addOnSuccessListener {
                Log.i("TAG","profile pic is deleted")
                Toast.makeText(context,"Profile Picture is deleted",Toast.LENGTH_SHORT).show()
                setProfilePhoto()
            }
            .addOnFailureListener {
                Log.i("TAG","Error : ${it.message}")
                Toast.makeText(context,"Error occurred. Please try again after sometime or check your internet connection",Toast.LENGTH_SHORT).show()
            }
    }
}