package com.bullSaloon.bull.fragments.yourProfile

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.BitmapFactory
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bullSaloon.bull.MainActivity
import com.bullSaloon.bull.R
import com.bullSaloon.bull.adapters.DialogFollowersRecyclerViewAdapter
import com.bullSaloon.bull.adapters.SaloonItemViewPagerAdapter
import com.bullSaloon.bull.adapters.YourProfileViewPagerAdapter
import com.bullSaloon.bull.databinding.FragmentYourProfileBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class YourProfileFragment : Fragment() {

    private var _binding: FragmentYourProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var data: UserDataClass

    private lateinit var storage: FirebaseStorage
    private val db = Firebase.firestore
    private val auth = Firebase.auth

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

        binding.YourProfileFollowersLayout.setOnClickListener {
            setUpDialogBoxFollow("Followers")
        }

        binding.YourProfileFollowingLayout.setOnClickListener {
            setUpDialogBoxFollow("Following")
        }

        setProfilePhoto()

        setFollowCount()

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

        val dataVieModel = ViewModelProvider(requireActivity()).get(UserDataViewModel::class.java)

        dataVieModel.getProfilePic().observe(viewLifecycleOwner,{

            var data = it

            if (it == null){
                data = BitmapFactory.decodeResource(this.resources, R.drawable.ic_baseline_person_24)
            }

            Log.i("TAGProfile", "user profile updated fragment : ${data}")

            GlideApp.with(this)
                .load(data)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_baseline_person_24)
                .fallback(R.drawable.ic_baseline_person_24)
                .into(binding.userPhotoYourProfileTextView)

        })
    }

    private fun removePhoto(){

        val userData2 = mapOf("user_name" to data.user_name.replace("\\s".toRegex(), "_"), "id" to data.user_id)
        val imageUrl2 = "User_Images/${data.user_id}/${userData2["user_name"]}_profilePicture.jpg"

        val imageRef = storage.reference.child(imageUrl2)

        imageRef.delete()
            .addOnSuccessListener {
                Log.i("TAG","profile pic is deleted")
                Toast.makeText(context,"Profile Picture is deleted. Restart App to Update Profile Pic",Toast.LENGTH_SHORT).show()
                (activity as MainActivity).updateProfilePicOutsideMain()
                setProfilePhoto()
            }
            .addOnFailureListener {
                Log.i("TAG","Error : ${it.message}")
                Toast.makeText(context,"Error occurred. Please try again after sometime or check your internet connection",Toast.LENGTH_SHORT).show()
            }
    }

    private fun setFollowCount(){
        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .addSnapshotListener { value, error ->
                if (error == null){
                    if (value?.exists()!!){
                        val following = if (value.get("following") != null) value.get("following") as  ArrayList<String> else arrayListOf()
                        val followingCount = following.size

                        val followers = if (value.get("followers") != null) value.get("followers") as  ArrayList<String> else arrayListOf()
                        val followerCount = followers.size

                        binding.YourProfileFollowersText.text = followerCount.toString()
                        binding.YourProfileFollowingText.text = followingCount.toString()
                    }
                } else {
                    Log.i("TAG", "Error in getting follow count: $error")
                }
            }
    }

    private fun setUpDialogBoxFollow(_dataType: String){

        var userLists: MutableList<String>
        val db = Firebase.firestore
        val auth = Firebase.auth
        var dataType = ""

        val dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.dialog_box_followers_list)
        val closeButton = dialog.findViewById<ImageView>(R.id.dialogFollowPopCloseButton)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.dialogFollowPopUpRecycler)
        val title = dialog.findViewById<TextView>(R.id.dialogFollowPopUpText)

        title.text = _dataType

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        dialog.show()

        dataType = if (_dataType == "Followers"){
            "followers"
        } else {
            "following"
        }

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val followList = if (it.get(dataType) !=  null) it.get(dataType)  as ArrayList<String> else arrayListOf()
                    userLists = followList

                    recyclerView.adapter = DialogFollowersRecyclerViewAdapter(userLists, this)
                }
            }
            .addOnFailureListener { e->
                Log.i("TAG", "Error in retrieving follow data : ${e.message}")
            }
    }
}