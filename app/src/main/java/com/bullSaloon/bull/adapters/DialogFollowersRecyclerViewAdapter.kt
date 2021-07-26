package com.bullSaloon.bull.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.ViewHolderDialogBoxFollowersBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DialogFollowersRecyclerViewAdapter(list: MutableList<String>, _fragment: Fragment): RecyclerView.Adapter<DialogFollowersRecyclerViewAdapter.DialogFollowersViewHolder>() {

    private val lists = list
    private val fragment = _fragment

    inner class DialogFollowersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding: ViewHolderDialogBoxFollowersBinding = ViewHolderDialogBoxFollowersBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogFollowersViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_dialog_box_followers,parent,false)
        return DialogFollowersViewHolder(view)
    }

    override fun onBindViewHolder(holder: DialogFollowersViewHolder, position: Int) {
        val holderBinding = holder.binding

        getUserDataFromFireStore(holderBinding, lists[position])

    }

    override fun getItemCount(): Int {

        return lists.size
    }

    private fun getUserDataFromFireStore(binding: ViewHolderDialogBoxFollowersBinding, userID: String){
        val db = Firebase.firestore

        db.collection("Users")
            .document(userID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val userName = it.get("user_name").toString()
                    binding.dialogFollowUserName.text = userName

                    val userNameUnderscore = userName.replace("\\s".toRegex(), "_")
                    val imageUrl = "User_Images/${userID}/${userNameUnderscore}_profilePicture.jpg"

                    setTargetUserProfilePic(binding, imageUrl)
                }
            }
            .addOnFailureListener {e->
                Log.i("TAG","Error in retrieving follower profile pic : ${e.message}")
            }
    }

    private fun setTargetUserProfilePic(binding: ViewHolderDialogBoxFollowersBinding, profilePicRef: String){

        val imageRef = Firebase.storage.reference.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(binding.dialogFollowUserProfilePic)
    }
}