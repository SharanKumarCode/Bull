package com.bullSaloon.bull.adapters

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.ViewHolderYourProfileNicesItemBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.MyNicesData
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class YourProfileNicesRecyclerViewAdapter(list: MutableList<MyNicesData>, _fragment: Fragment):
    RecyclerView.Adapter<YourProfileNicesRecyclerViewAdapter.YourProfileNicesRecyclerViewHolder>() {

    private var nicesList = list
    private val fragment = _fragment

    inner class YourProfileNicesRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding: ViewHolderYourProfileNicesItemBinding = ViewHolderYourProfileNicesItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): YourProfileNicesRecyclerViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_your_profile_nices_item,parent,false)
        return YourProfileNicesRecyclerViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: YourProfileNicesRecyclerViewHolder, position: Int) {

        val holBinding = holder.binding

//        set Target UserName
        holBinding.targetUserNameTextView.text = nicesList[position].targetUserName

//         set date
        val date = nicesList[position].timeStamp.substring(0,10)
        val dateFormatted = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        val month = dateFormatted.month.toString()
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        holBinding.niceTimeStampTextView.text = holBinding.root.resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,dateFormatted.year)

//         set Target User Profile Pic
        setTargetUserProfilePic(holBinding, nicesList[position].targetUserProfilePicRef)

//         set Target Image
        setTargetImage(holBinding, nicesList[position].targetImageRef)

//        launch bullMagicItem
        holBinding.targetNiceImage.setOnClickListener {
            launchBullMagicItemFragment(
                nicesList[position].targetUserID,
                nicesList[position].targetPhotoID,
                nicesList[position].targetImageRef,
                FRAGMENT_FLAG.BullMagicItem)
        }

//        launch bullMagicTargetUser
        holBinding.targetUserNameTextView.setOnClickListener {
            launchBullMagicItemFragment(
                nicesList[position].targetUserID,
                nicesList[position].targetPhotoID,
                nicesList[position].targetImageRef,
                FRAGMENT_FLAG.BullMagicTargetUser)
        }

    }

    override fun getItemCount(): Int {
        return nicesList.size
    }

    private fun setTargetUserProfilePic(binding: ViewHolderYourProfileNicesItemBinding, profilePicRef: String){

        val imageRef = Firebase.storage.reference.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(binding.targetNiceUserImage)
    }

    private fun setTargetImage(binding: ViewHolderYourProfileNicesItemBinding, _imageRef: String){

        val imageRef = Firebase.storage.getReferenceFromUrl(_imageRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .apply(RequestOptions.centerCropTransform())
            .placeholder(R.drawable.ic_bull)
            .into(binding.targetNiceImage)
    }

    private fun launchBullMagicItemFragment(userID:String, photoID: String, imageRef:String, fragmentFlag: String){

        val navHost = fragment.parentFragment?.activity?.supportFragmentManager?.findFragmentById(R.id.fragment)
        val navController = navHost?.findNavController()

        val args = Bundle()

        val mapData = hashMapOf(
            "user_id" to userID,
            "photo_id" to photoID,
            "imageRef" to imageRef,
            "clickedComments" to "false",
            "fragment_flag" to fragmentFlag
        )

        args.putSerializable("userImageData", mapData)

        navController?.navigate(R.id.action_yourProfileFragment_to_bullMagicFragment, args)
    }

    companion object {

        private object FRAGMENT_FLAG {
            const val BullMagicItem = "BullMagicItem"
            const val BullMagicTargetUser = "BullMagicTargetUser"
        }
    }
}