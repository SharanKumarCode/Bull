package com.bullSaloon.bull.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.ViewHolderBullmagicTargetuserItemBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.MyPhotosData

class BullMagicTargetUserRecyclerViewAdapter(_lists: MutableList<MyPhotosData>, _fragment: Fragment): RecyclerView.Adapter<BullMagicTargetUserRecyclerViewAdapter.BullMagicTargetUserRecyclerViewHolder>() {

    private val lists = _lists
    private val storage = SingletonInstances.getStorageReference()
    private val fragment = _fragment

    inner class BullMagicTargetUserRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val binding: ViewHolderBullmagicTargetuserItemBinding = ViewHolderBullmagicTargetuserItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BullMagicTargetUserRecyclerViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_bullmagic_targetuser_item,parent,false)
        return BullMagicTargetUserRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BullMagicTargetUserRecyclerViewHolder, position: Int) {
        val holdBinding = holder.binding

        setImageFromFirebase(holdBinding.root.context, holdBinding, lists[position].imageRef)

        holdBinding.bullMagicTargetUserItemImageView.setOnClickListener {
            launchBullMagicItemFragment(lists[position].userID, lists[position].photoID, lists[position].imageRef)
        }

    }

    override fun getItemCount(): Int {
        return lists.size
    }

    private fun setImageFromFirebase(context: Context, binding: ViewHolderBullmagicTargetuserItemBinding, imageUrl: String){

        val imageRef = storage.storage.getReferenceFromUrl(imageUrl)

        GlideApp.with(context)
            .load(imageRef)
            .centerCrop()
            .placeholder(R.drawable.ic_bull)
            .into(binding.bullMagicTargetUserItemImageView)
    }

    private fun launchBullMagicItemFragment(userID: String, photoID:String, imageRef:String){

        val navHost = fragment.parentFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)
        val navController = navHost?.findNavController()

        val args = Bundle()
        val mapData = hashMapOf("user_id" to userID, "photo_id" to photoID, "imageRef" to imageRef)
        args.putSerializable("userImageData",mapData)


        navController?.navigate(R.id.action_bullMagicTargetUserFragment_to_bullMagicItemFragment, args)

    }

}