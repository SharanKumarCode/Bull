package com.bullSaloon.bull.adapters

import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.ViewHolderCommentItemBinding
import com.bullSaloon.bull.fragments.bullMagic.BullMagicItemFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.CommentDataClass
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CommentRecyclerViewAdapter(_lists: MutableList<CommentDataClass>, _photoID: String, _fragment: BullMagicItemFragment): RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentRecyclerViewHolder>() {

    private val lists = _lists
    private val db = SingletonInstances.getFireStoreInstance()
    private val storageRef = SingletonInstances.getStorageReference()
    private val auth = SingletonInstances.getAuthInstance()
    private val photoID = _photoID
    private val fragment = _fragment

    private val currentUserID = auth.currentUser?.uid

    private lateinit var photoUserID: String
    private lateinit var commentUserID: String

    inner class CommentRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding: ViewHolderCommentItemBinding = ViewHolderCommentItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_comment_item,parent,false)
        return CommentRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentRecyclerViewHolder, position: Int) {

        val holderBinding = holder.binding

        photoUserID = lists[position].photoUserID
        commentUserID = lists[position].commentUserID

//        get username and profile pic
        getFirestoreData(commentUserID, holderBinding)

//        set date
        val date = lists[position].timestamp.substring(0,10)
        val dateFormatted = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        val year = if (dateFormatted.year == Calendar.getInstance().get(Calendar.YEAR)) "" else dateFormatted.year.toString()
        val month = dateFormatted.month.toString()
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            .substring(0, 3)
        holderBinding.commentDateText.text = holderBinding.root.resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,year)

        val hour24 = lists[position].timestamp.substring(11, 13).toInt()
        val min = lists[position].timestamp.substring(14, 16).toInt()
        val timeFlag = if (hour24 > 12) "pm" else "am"
        val hour12 = if (hour24 > 12) hour24 - 12 else hour24
        holderBinding.commentTimeText.text = holderBinding.root.resources.getString(R.string.placeHolderTime,hour12,min,timeFlag)

//        set comment
        holderBinding.commentText.text = lists[position].comment

//        set delete button visibility
        if (commentUserID == currentUserID){
            holderBinding.commentDeleteButton.visibility = View.VISIBLE
        } else {
            holderBinding.commentDeleteButton.visibility = View.INVISIBLE
        }

//        delete comment
        holderBinding.commentDeleteButton.setOnClickListener {
            openDeleteCommentDialog(holderBinding, photoUserID, photoID, lists[position].commentID)
        }

//        set like button
        holderBinding.commentLikeButton.setOnClickListener {

            Log.i(TAG,"Like is clicked")
            updateLikeStatus(holderBinding, photoUserID, photoID, lists[position].commentID)
        }

        getCommentLikes(holderBinding, photoUserID, photoID, lists[position].commentID)
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    private fun getFirestoreData(commentUserID : String, binding: ViewHolderCommentItemBinding){

        db.collection("Users")
            .document(commentUserID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    binding.commentUserName.text = it.getString("user_name")

                    val userNameUnderscore = it.getString("user_name")!!.replace("\\s".toRegex(), "_")
                    val imageUrl = "User_Images/${commentUserID}/${userNameUnderscore}_profilePicture.jpg"

                    setTargetUserProfilePic(binding, imageUrl)

                }
            }
            .addOnFailureListener {e->
                Log.i(TAG, "error in getting user data : ${e.message}")
            }
    }

    private fun setTargetUserProfilePic(binding: ViewHolderCommentItemBinding, profilePicRef: String){

        val imageRef = storageRef.storage.reference.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(binding.commentUserProfPicImage)
    }

    private fun openDeleteCommentDialog(binding: ViewHolderCommentItemBinding, photoUserID: String, photoID: String, commentID: String){
        val dialog = Dialog(binding.root.context)
        dialog.setContentView(R.layout.dialog_delete_comment)

        val deleteButton = dialog.findViewById<MaterialButton>(R.id.deleteCommentDialogButton)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancelDeleteCommentDialogButton)

        deleteButton.setOnClickListener {

            db.collection("Users")
                .document(photoUserID)
                .collection("photos")
                .document(photoID)
                .collection("comments")
                .document(commentID)
                .delete()
                .addOnSuccessListener {
                    fragment.getFirestoreFuncFromBullMagicItem()
                    dialog.dismiss()
                    Log.i(TAG, "Comment is deleted")
                }
                .addOnFailureListener { e->
                    dialog.dismiss()
                    Log.i(TAG, "Error in deleting comment : ${e.message}")
                }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun getCommentLikes(binding: ViewHolderCommentItemBinding, photoUserID: String, photoID: String, commentID: String){
        val likeQuery = db.collection("Users")
            .document(photoUserID)
            .collection("photos")
            .document(photoID)
            .collection("comments")
            .document(commentID)

        likeQuery.get()
            .addOnSuccessListener {
                if (it.exists()){
                    val commentLikeUserIDList = if (it.get("comment_likes_userid") != null) it.get("comment_likes_userid") as ArrayList<String> else arrayListOf()
                    binding.commentLikeText.text = commentLikeUserIDList.size.toString()

                    if (commentLikeUserIDList.contains(currentUserID)){
                        binding.commentLikeButton.setImageResource(R.drawable.ic_baseline_like_filled_24)
                    } else {
                        binding.commentLikeButton.setImageResource(R.drawable.ic_baseline_like_blank_24)
                    }
                }
            }
            .addOnFailureListener { e->
                Log.i(TAG,"failed to get comment like status from fireStore : ${e.message}")
            }

    }

    private fun updateLikeStatus(binding: ViewHolderCommentItemBinding, photoUserID: String, photoID: String, commentID: String){

        val likeQuery = db.collection("Users")
            .document(photoUserID)
            .collection("photos")
            .document(photoID)
            .collection("comments")
            .document(commentID)

        Log.i(TAG, "called updateLikeStatus targetUserID - $photoUserID , photoID - $photoID , commentID - $commentID")

        likeQuery.get()
            .addOnSuccessListener {
                if (it.exists()){

                    val commentLikeUserIDList = if (it.get("comment_likes_userid") != null) it.get("comment_likes_userid") as ArrayList<String> else arrayListOf()

                    if (commentLikeUserIDList.contains(currentUserID)){
                        Log.i(TAG, "commentLikeUserIDList contains")
                        likeQuery.update("comment_likes_userid", FieldValue.arrayRemove(currentUserID))
                            .addOnSuccessListener {
                                getCommentLikes(binding, photoUserID, photoID, commentID)
                                Log.i(TAG,"removed comment like status")
                            }
                            .addOnFailureListener{e->
                                Log.i(TAG,"failed to remove comment like status from fireStore : ${e.message}")
                            }
                    } else {
                        Log.i(TAG, "commentLikeUserIDList NOT contains")
                        likeQuery.update("comment_likes_userid", FieldValue.arrayUnion(currentUserID))
                            .addOnSuccessListener {
                                getCommentLikes(binding, photoUserID, photoID, commentID)
                                Log.i(TAG,"added comment like status")
                            }
                            .addOnFailureListener{e->
                                Log.i(TAG,"failed to add comment like status to fireStore : ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e->
                Log.i(TAG,"failed to get comment like status from fireStore : ${e.message}")
            }
    }

    companion object {
        private const val TAG = "TAGCommentRecyclerViewAdapter"
    }

}