package com.bullSaloon.bull.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.ViewHolderSaloonReviewItemBinding
import com.bullSaloon.bull.fragments.saloon.SaloonReviewFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SaloonReviewRecyclerAdapter(_lists: MutableList<SaloonReviewFragment.RatingReviewData>): RecyclerView.Adapter<SaloonReviewRecyclerAdapter.SaloonReviewRecyclerViewHolder>() {

    private val lists = _lists
    private val db = SingletonInstances.getFireStoreInstance()
    private val storageRef = SingletonInstances.getStorageReference()

    inner class SaloonReviewRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: ViewHolderSaloonReviewItemBinding = ViewHolderSaloonReviewItemBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaloonReviewRecyclerAdapter.SaloonReviewRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_saloon_review_item,parent,false)
        return SaloonReviewRecyclerViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SaloonReviewRecyclerViewHolder, position: Int) {
        val holderBinding = holder.binding

//        get username and profile pic
        getFirestoreData(lists[position].userID, holderBinding)

//        set date
        val date = lists[position].timestamp.substring(0,10)
        val dateFormatted = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        val month = dateFormatted.month.toString()
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        holderBinding.saloonReviewDate.text = holderBinding.root.resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,dateFormatted.year.toString())

//        set review
        holderBinding.saloonReviewUserReviewText.text = lists[position].review
        if (lists[position].review == ""){
            val param = holderBinding.saloonReviewUserReviewText.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(10,30,10,40)
            holderBinding.saloonReviewUserReviewText.layoutParams = param
        }

//        set rating
        when(lists[position].rating.toInt()){
            1 -> setRatingPic(holderBinding, R.drawable.ic_rating_one_stars)
            2 -> setRatingPic(holderBinding, R.drawable.ic_rating_two_stars)
            3 -> setRatingPic(holderBinding, R.drawable.ic_rating_three_stars)
            4 -> setRatingPic(holderBinding, R.drawable.ic_rating_four_stars)
            5 -> setRatingPic(holderBinding, R.drawable.ic_rating_five_stars)
            else -> setRatingPic(holderBinding, R.drawable.ic_rating_one_stars)
        }
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    private fun getFirestoreData(userID : String, binding: ViewHolderSaloonReviewItemBinding){

        db.collection("Users")
            .document(userID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    binding.saloonReviewUserName.text = it.getString("user_name")

                    val userNameUnderscore = it.getString("user_name")!!.replace("\\s".toRegex(), "_")
                    val imageUrl = "User_Images/${userID}/${userNameUnderscore}_profilePicture.jpg"

                    setTargetUserProfilePic(binding, imageUrl)

                }
            }
            .addOnFailureListener {e->
                Log.i("TAG", "error in getting user data : ${e.message}")
            }
    }

    private fun setTargetUserProfilePic(binding: ViewHolderSaloonReviewItemBinding, profilePicRef: String){

        val imageRef = storageRef.storage.reference.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(binding.saloonReviewUserProfilePic)
    }

    private fun setRatingPic(binding: ViewHolderSaloonReviewItemBinding, drawableResource: Int){

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(drawableResource)
            .placeholder(R.drawable.ic_rating_one_stars)
            .into(binding.saloonReviewRatingImageView)

    }
}