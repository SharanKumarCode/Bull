package com.bullSaloon.bull.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bullSaloon.bull.MainActivity
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.SplashScreenActivity
import com.bullSaloon.bull.databinding.FragmentSettingsBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.login.LoginManager
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var data: UserDataClass

    private val storageRef = SingletonInstances.getStorageReference()
    private val db = SingletonInstances.getFireStoreInstance()
    private val auth = SingletonInstances.getAuthInstance()

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

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = SingletonUserData.userData

        binding.UserNameTextField.text = data.user_name

        binding.changeDisplayPicture.setOnClickListener {
            showPopUp(it)
        }

        binding.signOutButton.setOnClickListener {
            SingletonInstances.getAuthInstance().signOut()
            LoginManager.getInstance().logOut()
            Toast.makeText(requireActivity(), "Signed-Out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), SplashScreenActivity::class.java))
            activity?.finish()
        }

        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }

        binding.cacheSizeText.text = "${initializeCache() / (1024 * 1024)} MB"

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
                    navController?.navigate(R.id.action_settingsFragment_to_cameraFragment)
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
                    item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx,0)
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

            Log.i("TAGProfile", "user profile updated fragment : $data")

            GlideApp.with(this)
                .load(data)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_baseline_person_24)
                .fallback(R.drawable.ic_baseline_person_24)
                .into(binding.profilePicImage)
        })
    }

    private fun removePhoto(){

        val userData2 = mapOf("user_name" to data.user_name.replace("\\s".toRegex(), "_"), "id" to data.user_id)
        val imageUrl2 = "User_Images/${data.user_id}/${userData2["user_name"]}_profilePicture.jpg"

        val imageRef = storageRef.child(imageUrl2)

        imageRef.delete()
            .addOnSuccessListener {
                Log.i("TAG","profile pic is deleted")
                Toast.makeText(context,"Profile Picture is deleted. Restart App to Update Profile Pic",
                    Toast.LENGTH_SHORT).show()
                (activity as MainActivity).updateProfilePicOutsideMain()
                setProfilePhoto()
            }
            .addOnFailureListener {
                Log.i("TAG","Error : ${it.message}")
                Toast.makeText(context,"Error occurred. Please try again after sometime or check your internet connection",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAboutDialog(){

        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_about_us)

        val closeButton = dialog.findViewById<ImageButton>(R.id.dialogAboutCloseButton)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun initializeCache(): Long {
        var size: Long = 0
        size += getDirSize(requireContext().cacheDir)
        size += getDirSize(requireContext().externalCacheDir!!)

        return size
    }

    private fun getDirSize(dir: File): Long {
        var size: Long = 0

        for (file in dir.listFiles()) {
            if (file != null && file.isDirectory) {
                size += getDirSize(file)
            } else if (file != null && file.isFile) {
                size += file.length()
            }
        }
        return size
    }
}