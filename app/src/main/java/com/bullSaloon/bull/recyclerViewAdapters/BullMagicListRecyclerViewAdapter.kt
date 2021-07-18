package com.bullSaloon.bull.recyclerViewAdapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.ViewHolderBullMagicItemBinding
import com.bullSaloon.bull.genericClasses.BullMagicListData
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class BullMagicListRecyclerViewAdapter(lists: MutableList<BullMagicListData>): RecyclerView.Adapter<BullMagicListRecyclerViewAdapter.BullMagicListRecyclerViewHolder>() {

    private val bullMagicLists = lists
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BullMagicListRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_bull_magic_item,parent,false)
        return BullMagicListRecyclerViewHolder(view)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: BullMagicListRecyclerViewHolder, position: Int) {
        val holderBinding = holder.binding
        val context: Context = holder.itemView.context

        //set user name
        holderBinding.BullMagicUserName.text = bullMagicLists[position].userName

        //set saloon name
        holderBinding.BullMagicShopName.text = if (bullMagicLists[position].saloonName != "") "@" + bullMagicLists[position].saloonName else ""

        //set caption name
        holderBinding.BullMagicImageCaptionText.text = bullMagicLists[position].caption

        //set date
        Log.i("TAG","date : ${bullMagicLists[position].timeStamp} ")
        val date = bullMagicLists[position].timeStamp.substring(0,10)
        val dateFormatted = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        val month = dateFormatted.month.toString()
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        holderBinding.BullMagicImageDate.text = holderBinding.root.resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,dateFormatted.year)

        //set Image
        setImageFromFirebase(context, holderBinding, bullMagicLists[position].imageRef)

        holderBinding.BullMagicNiceImageView.setOnClickListener {
            updateNiceStatus(bullMagicLists[position].userId, bullMagicLists[position].photoId)
        }

        //check nice status and number of nices
        db.collection("Users")
            .document(bullMagicLists[position].userId)
            .addSnapshotListener { value, error ->
                if (error == null){
                    if (value?.exists() == true){
                        val photoID = bullMagicLists[position].photoId
                        val array = if (value.contains("photos.$photoID.nices_userid")) value.get("photos.$photoID.nices_userid") as List<String> else listOf()
                        if (array.contains(auth.currentUser?.uid)){
                            holderBinding.BullMagicNiceImageView.setImageResource(R.drawable.ic_nice_filled_icon)
                        } else {
                            holderBinding.BullMagicNiceImageView.setImageResource(R.drawable.ic_nice_blank_icon)
                        }
                        holderBinding.NicesTextView.text = array.size.toString()
                    }
                }
            }

    }

    override fun getItemCount(): Int {
        return bullMagicLists.size
    }

    private fun setImageFromFirebase(context: Context, binding: ViewHolderBullMagicItemBinding, imageUrl: String){

        val imageRef = storage.getReferenceFromUrl(imageUrl)
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = (Resources.getSystem().displayMetrics.heightPixels * 0.40).toInt()

        GlideApp.with(context)
            .load(imageRef)
            .apply(RequestOptions.overrideOf(width,height))
            .centerCrop()
            .placeholder(R.drawable.ic_bull)
            .into(binding.BullMagicImageView)
    }

    private fun updateNiceStatus(userId: String, photoID: String){

        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val array = if (it.contains("photos.$photoID.nices_userid")) it.get("photos.$photoID.nices_userid") as List<String> else listOf()
                    if (!it.contains("photos.$photoID.nices_userid")){
                        db.collection("Users")
                            .document(userId)
                            .update("photos.$photoID.nices_userid", FieldValue.arrayUnion(auth.currentUser?.uid))
                            .addOnSuccessListener {
                            }
                    } else if (it.contains("photos.$photoID.nices_userid") && !array.contains(auth.currentUser?.uid)){
                        db.collection("Users")
                            .document(userId)
                            .update("photos.$photoID.nices_userid", FieldValue.arrayUnion(auth.currentUser?.uid))
                            .addOnSuccessListener {
                            }
                    } else {
                        db.collection("Users")
                            .document(userId)
                            .update("photos.$photoID.nices_userid", FieldValue.arrayRemove(auth.currentUser?.uid))
                            .addOnSuccessListener {
                            }
                    }

                }
            }
    }

    inner class BullMagicListRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: ViewHolderBullMagicItemBinding = ViewHolderBullMagicItemBinding.bind(itemView)

    }

}