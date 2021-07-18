package com.bullSaloon.bull.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.text.style.IconMarginSpan
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import com.bullSaloon.bull.BullMagicActivity
import com.bullSaloon.bull.CameraActivity
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentYourProfileBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.UserBasicDataParcelable
import com.bullSaloon.bull.genericClasses.UserDataClass
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class YourProfileFragment : Fragment() {

    private var _binding: FragmentYourProfileBinding? = null
    private val binding get() = _binding!!

    private val EXTRA_INTENT: String = "userBasicData"
    private lateinit var dataParecelable: UserBasicDataParcelable
    private lateinit var userDatadata: UserDataClass

    private lateinit var storage: FirebaseStorage

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

        val act = activity as BullMagicActivity
        dataParecelable = act.fragmentGetIntentData()
        userDatadata = UserDataClass(dataParecelable.user_id, dataParecelable.user_name, dataParecelable.mobileNumber)

        binding.userNameYourProfileTextView.text = userDatadata.user_name
        binding.mobileNumberYourProfileTextView.text = userDatadata.mobileNumber

        binding.userImageEditButton.setOnClickListener {
            showPopUp(it)
        }

        setProfilePhoto()
    }

    override fun onResume() {
        super.onResume()
        setProfilePhoto()
        Log.i("TAG","Fragment resumed")
    }

    @SuppressLint("RestrictedApi")
    private fun showPopUp(v: View){
        val popUp = PopupMenu(requireContext(), v)
        popUp.menuInflater.inflate(R.menu.popup_user_picture_edit_menu, popUp.menu)

        popUp.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.popUpMenuItemTakePicture ->{
                    val intent = Intent(requireContext(), CameraActivity::class.java)
                    intent.putExtra(EXTRA_INTENT, dataParecelable)
                    intent.putExtra("activity_flag", "profilePicture")
                    startActivity(intent)
                    true
                }
                R.id.popUpMenuItemGallery ->{
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

        val firebaseCloudUrl = "gs://bull-saloon.appspot.com/"

        val userData = mapOf("user_name" to userDatadata.user_name.replace("\\s".toRegex(), "_"), "id" to userDatadata.user_id)
        val imageUrl = "User_Images/${userDatadata.user_id}/${userData["user_name"]}_profilePicture.jpg"
        val imageDownloadUrl = "$firebaseCloudUrl$imageUrl"

        storage.reference.child(imageUrl).downloadUrl
            .addOnSuccessListener {

                GlideApp.with(requireContext())
                    .load(storage.reference.child(imageUrl))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(binding.userPhotoYourProfileTextView)

            }
            .addOnFailureListener {

                GlideApp.with(requireContext())
                    .load(R.drawable.ic_baseline_person_24)
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(binding.userPhotoYourProfileTextView)

                Log.i("TAG","storage check: Image does not exists - url - ${imageDownloadUrl}")

            }
    }

    private fun removePhoto(){

        Log.i("TAG","Photo to be removed")

        val userData2 = mapOf("user_name" to userDatadata.user_name.replace("\\s".toRegex(), "_"), "id" to userDatadata.user_id)
        val imageUrl2 = "User_Images/${userDatadata.user_id}/${userData2["user_name"]}_profilePicture.jpg"

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