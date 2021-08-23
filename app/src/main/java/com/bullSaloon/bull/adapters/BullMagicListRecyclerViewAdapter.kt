package com.bullSaloon.bull.adapters

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.ViewHolderBullMagicItemBinding
import com.bullSaloon.bull.fragments.bullMagic.BullMagicListFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.BullMagicListData
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FieldValue
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class BullMagicListRecyclerViewAdapter(lists: MutableList<BullMagicListData>, _fragment: BullMagicListFragment): RecyclerView.Adapter<BullMagicListRecyclerViewAdapter.BullMagicListRecyclerViewHolder>() {

    private val bullMagicLists = lists
    private val db = SingletonInstances.getFireStoreInstance()
    private val auth = SingletonInstances.getAuthInstance()
    private val storage = SingletonInstances.getStorageReference()
    private val fragment = _fragment
    private val currentUserID = auth.currentUser?.uid

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BullMagicListRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_bull_magic_item,parent,false)

        return BullMagicListRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BullMagicListRecyclerViewHolder, position: Int) {
        val holderBinding = holder.binding
        val context: Context = holder.itemView.context

        //set user name
        holderBinding.BullMagicUserName.text = bullMagicLists[position].userName

        //set saloon name
        holderBinding.BullMagicShopName.text = if (bullMagicLists[position].saloonName != "") "@" + bullMagicLists[position].saloonName else ""

        //set caption name

        if (bullMagicLists[position].caption == ""){
            holderBinding.BullMagicImageCaptionText.visibility = View.GONE
        } else {
            holderBinding.BullMagicImageCaptionText.visibility = View.VISIBLE
            holderBinding.BullMagicImageCaptionText.text = URLDecoder.decode(bullMagicLists[position].caption, "UTF-8")
        }

        holderBinding.bullMagicItemContainer.animation = AnimationUtils.loadAnimation(holderBinding.root.context, R.anim.animation_two)

        //set date
        val date = bullMagicLists[position].timeStamp.substring(0,10)
        val dateFormatted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val month = dateFormatted.month.toString()
            .lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        holderBinding.BullMagicImageDate.text = holderBinding.root.resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,dateFormatted.year)

        //set Image
        setImageFromFirebase(context, holderBinding, bullMagicLists[position].imageRef)

//        update nice status
        holderBinding.BullMagicNiceImageView.setOnClickListener {
            updateNiceStatus(bullMagicLists[position].userId, bullMagicLists[position].photoId, bullMagicLists[position].userName, bullMagicLists[position].imageRef)
        }

//        start BullMagicItem Fragment from image
        holderBinding.BullMagicImageView.setOnClickListener {
            startBullMagicItemFragment(fragment, bullMagicLists[position].userId, bullMagicLists[position].photoId, bullMagicLists[position].imageRef)
        }

//        start BullMagicItem Fragment from comment
        holderBinding.BullMagicCommentsImageView.setOnClickListener {
            startBullMagicItemFragment(fragment, bullMagicLists[position].userId, bullMagicLists[position].photoId, bullMagicLists[position].imageRef, true)
        }

//        start BullMagicTargetUser Fragment
        holderBinding.BullMagicUserName.setOnClickListener {
            startBullMagicTargetUserFragment(fragment, bullMagicLists[position].userId)
        }

        //check nice status and number of nices
        db.collection("Users")
            .document(bullMagicLists[position].userId)
            .collection("photos")
            .document(bullMagicLists[position].photoId)
            .addSnapshotListener { value, error ->
                if (error == null){
                    if (value?.exists() == true){
                        val array = value.get("nices_userid") as List<String>
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

        val imageRef = storage.storage.getReferenceFromUrl(imageUrl)
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = (Resources.getSystem().displayMetrics.heightPixels * 0.40).toInt()

        GlideApp.with(context)
            .load(imageRef)
            .apply(RequestOptions.overrideOf(width,height))
            .centerCrop()
            .placeholder(R.drawable.ic_bull)
            .into(binding.BullMagicImageView)
    }

    private fun updateNiceStatus(targetUserId: String, photoID: String, userName: String, imageRef: String){

        val niceQuery = db.collection("Users")
            .document(targetUserId)
            .collection("photos")
            .document(photoID)

        niceQuery.get()
            .addOnSuccessListener {
                if (it.exists()){
                    val nicesUserIDList = it.get("nices_userid") as ArrayList<String>

                    if (nicesUserIDList.contains(currentUserID)){
                        niceQuery.update("nices_userid", FieldValue.arrayRemove(currentUserID))
                            .addOnSuccessListener {
                                updateNiceStatusToSelf(targetUserId, photoID, userName, imageRef, removeNiceData = true)
                            }
                            .addOnFailureListener{e->
                                Log.i(TAG,"failed to remove nice status from fireStore : ${e.message}")
                            }
                    } else {
                        niceQuery.update("nices_userid", FieldValue.arrayUnion(currentUserID))
                            .addOnSuccessListener {
                                updateNiceStatusToSelf(targetUserId, photoID, userName, imageRef)
                            }
                            .addOnFailureListener{e->
                                Log.i(TAG,"failed to add nice status to fireStore : ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e->
                Log.i(TAG,"failed to get nice status from fireStore : ${e.message}")
            }


    }

    private fun updateNiceStatusToSelf(targetUserId: String, photoID: String, userName: String, imageRef: String, removeNiceData: Boolean = false){

        val niceUUID = UUID.randomUUID().toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val niceMap = mapOf("user_id" to targetUserId,
                            "photo_id" to photoID,
                            "timestamp" to dateFormat.toString(),
                            "user_name" to userName,
                            "image_ref" to imageRef,
                            "nice_id" to niceUUID)

        val niceCollectionRef = db.collection("Users")
                                    .document(currentUserID!!)
                                    .collection("nices")

        if (!removeNiceData){

            niceCollectionRef
                .document(niceUUID)
                .set(niceMap)
                .addOnSuccessListener {
                    Log.i(TAG,"nice data added to self fireStore")
                }
                .addOnFailureListener {e->
                    Log.i(TAG,"failed to add nice data to self fireStore : ${e.message}")
                }

        } else {

            niceCollectionRef
                .whereEqualTo("photo_id", photoID)
                .get()
                .addOnSuccessListener {
                    niceCollectionRef
                        .document(it.documents.firstOrNull()?.id!!)
                        .delete()
                        .addOnSuccessListener {
                            Log.i(TAG,"nice data deleted from self fireStore")
                        }
                        .addOnFailureListener {e->
                            Log.i(TAG,"failed to remove nice data from self fireStore : ${e.message}")
                        }
                }
        }
    }

    inner class BullMagicListRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: ViewHolderBullMagicItemBinding = ViewHolderBullMagicItemBinding.bind(itemView)

    }

    private fun startBullMagicItemFragment(fragment: BullMagicListFragment, userID: String, photoID: String, imageRef: String, clickedComments: Boolean = false){

        val yourBullMagicFragmentHost = fragment.parentFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)
        val navController = yourBullMagicFragmentHost?.findNavController()

        val args = Bundle()

        val mapData: HashMap<String, String> = if (clickedComments){
            hashMapOf("user_id" to  userID, "photo_id" to photoID, "imageRef" to imageRef, "clickedComments" to "true")
        } else {
            hashMapOf("user_id" to  userID, "photo_id" to photoID, "imageRef" to imageRef, "clickedComments" to "false")
        }

        args.putSerializable("userImageData", mapData)

        navController?.navigate(R.id.action_bullMagicListFragment_to_bullMagicItemFragment, args)
    }

    private fun startBullMagicTargetUserFragment(fragment: BullMagicListFragment, userID: String){

        val yourBullMagicFragmentHost = fragment.parentFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)
        val navController = yourBullMagicFragmentHost?.findNavController()

        val args = Bundle()
        args.putString("user_id", userID)

        navController?.navigate(R.id.action_bullMagicListFragment_to_bullMagicTargetUserFragment, args)
    }

    companion object {
        private const val TAG = "TAGBullMagicListRecyclerViewAdapter"
    }

}