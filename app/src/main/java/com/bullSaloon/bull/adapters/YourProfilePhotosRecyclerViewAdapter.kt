package com.bullSaloon.bull.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.ViewHolderYourProfilePhotosItemBinding
import com.bullSaloon.bull.fragments.yourProfile.YourProfilePhotosFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.MyPhotosData
import com.bullSaloon.bull.viewModel.YourProfileViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class YourProfilePhotosRecyclerViewAdapter(lists: MutableList<MyPhotosData>, dataViewModel: YourProfileViewModel, childFragmentManager: YourProfilePhotosFragment): RecyclerView.Adapter<YourProfilePhotosRecyclerViewAdapter.YourProfilePhotosRecyclerViewHolder>(){

    val myPhotosList = lists
    val storage = Firebase.storage
    val dataModel = dataViewModel
    private val childFragManager = childFragmentManager


    inner class YourProfilePhotosRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: ViewHolderYourProfilePhotosItemBinding = ViewHolderYourProfilePhotosItemBinding.bind(itemView)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): YourProfilePhotosRecyclerViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_your_profile_photos_item,parent,false)
        return YourProfilePhotosRecyclerViewHolder(view)

    }

    override fun onBindViewHolder(holder: YourProfilePhotosRecyclerViewHolder, position: Int) {
        val holBinding = holder.binding

        setImageFromFirebase(holBinding.root.context, holBinding, myPhotosList[position].imageRef)

        holBinding.yourProfilePhotoItemImageView.setOnClickListener {

            dataModel.assignUserPhotoData(myPhotosList[position])
            startYourProfilePhotoItemFragment(childFragManager)
        }
    }

    override fun getItemCount(): Int {
        return myPhotosList.size
    }

    private fun setImageFromFirebase(context: Context, binding: ViewHolderYourProfilePhotosItemBinding, imageUrl: String){

        val imageRef = storage.getReferenceFromUrl(imageUrl)

        GlideApp.with(context)
            .load(imageRef)
            .centerCrop()
            .placeholder(R.drawable.ic_bull)
            .into(binding.yourProfilePhotoItemImageView)
    }

    private fun startYourProfilePhotoItemFragment(childFragManager: YourProfilePhotosFragment){

        Log.i("TAGNav", "photo is clicked")

        val yourProfilePhotoItemFragmentHost = childFragManager.parentFragmentManager.findFragmentById(R.id.YourProfilePhotoFragmentContainer)
        val navController = yourProfilePhotoItemFragmentHost?.findNavController()

        navController?.navigate(R.id.action_yourProfilePhotosFragment2_to_yourProfilePhotoItemFragment2)
    }
}