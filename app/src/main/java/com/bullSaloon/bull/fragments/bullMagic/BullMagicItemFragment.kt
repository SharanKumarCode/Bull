package com.bullSaloon.bull.fragments.bullMagic

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentBullMagicItemBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class BullMagicItemFragment : Fragment() {

    private var _binding: FragmentBullMagicItemBinding? = null
    private val binding get() = _binding!!
    private lateinit var userImageData: HashMap<String,String>
    private val storage = Firebase.storage
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var dataUserID: String
    private lateinit var dataPhotoID: String
    private lateinit var dataImageRef: String
    private var isCommentClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        exitTransition = inflaterTrans.inflateTransition(R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBullMagicItemBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            onBackButtonPressed()
        }

        userImageData = (arguments?.getSerializable("userImageData") as HashMap<String, String>?)!!
        dataImageRef = userImageData["imageRef"].toString()
        dataPhotoID = userImageData["photo_id"].toString()
        dataUserID = userImageData["user_id"].toString()

        isCommentClicked = userImageData["clickedComments"].toString() == "true"

        binding.bullMagicPhotoItemDate.text = ""

        getImageDataFromFireStore()

        binding.bullMagicItemUserName.setOnClickListener {
            launchBullMagicTargetUserFragment()
        }

        //check nice status and number of nices
        db.collection("Users")
            .document(dataUserID)
            .addSnapshotListener { value, error ->
                if (error == null){
                    if (value?.exists() == true){
                        val photoID = dataPhotoID
                        val array = if (value.contains("photos.$photoID.nices_userid")) value.get("photos.$photoID.nices_userid") as List<String> else listOf()
                        if (array.contains(auth.currentUser?.uid)){
                            binding.bullMagicNiceImageView.setImageResource(R.drawable.ic_nice_filled_icon)
                        } else {
                            binding.bullMagicNiceImageView.setImageResource(R.drawable.ic_nice_blank_icon)
                        }
                        binding.bullMagicNicesTextView.text = array.size.toString()
                    }
                }
            }

        if (isCommentClicked){
            binding.bullMagicEditCommentTextView.requestFocus()
        }

    }

    private fun onBackButtonPressed(){
        val navHost = this.parentFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)
        val navController = navHost?.findNavController()

        if (navController?.previousBackStackEntry?.destination?.id == R.id.bullMagicTargetUserFragment){
            launchBullMagicTargetUserFragment()
        } else {
            navController?.navigate(R.id.action_bullMagicItemFragment_to_bullMagicListFragment)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getImageDataFromFireStore(){

        db.collection("Users")
            .document(dataUserID)
            .get()
            .addOnSuccessListener { document->
                if (document.exists()){

                    val timeStamp = document.getString("photos.${dataPhotoID}.timestamp").toString()
                    val saloon = document.getString("photos.${dataPhotoID}.saloon_name").toString()
                    val caption = document.getString("photos.${dataPhotoID}.caption").toString()

                    //set date
                    val date = timeStamp.substring(0,10)
                    val dateFormatted = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
                    val month = dateFormatted.month.toString()
                        .lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                    binding.bullMagicPhotoItemDate.text = resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,dateFormatted.year)

                    //set user name
                    val userName = document.get("user_name").toString()
                    binding.bullMagicItemUserName.text = userName

                    //set saloon name
                    binding.bullMagicPhotoItemSaloonName.text = if (saloon != "") "@$saloon" else ""

                    //set caption
                    if (caption == ""){
                        binding.bullMagicPhotoItemCaption.visibility = View.GONE
                    } else {
                        binding.bullMagicPhotoItemCaption.visibility = View.VISIBLE
                        binding.bullMagicPhotoItemCaption.text = URLDecoder.decode(caption, "UTF-8")
                    }


                    setImageFromFirebase(dataImageRef)

//                    set userProfilePic
                    val targetUserNameUnderScore = userName.replace("\\s".toRegex(), "_")
                    val targetUserProfilePicRef= "User_Images/${dataUserID}/${targetUserNameUnderScore}_profilePicture.jpg"
                    setTargetUserProfile(targetUserProfilePicRef)

                    //        update nice status
                    binding.bullMagicNiceImageView.setOnClickListener {
                        updateNiceStatus(dataUserID, dataPhotoID, userName, dataImageRef)
                    }
                }
            }
    }

    private fun setImageFromFirebase(imageUrl: String){

        val imageRef = storage.getReferenceFromUrl(imageUrl)
        val width = Resources.getSystem().displayMetrics.widthPixels

        GlideApp.with(this)
            .load(imageRef)
            .override(width, Target.SIZE_ORIGINAL)
            .placeholder(R.drawable.ic_bull)
            .into(binding.bullMagicPhotoItemImageView)
    }

    private fun setTargetUserProfile(profilePicRef: String){

        val imageRef = Firebase.storage.reference.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(binding.bullMagicItemUserProfilePic)
    }

    private fun updateNiceStatus(userId: String, photoID: String, userName: String, imageRef: String){

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
                                updateNiceStatusToSelf(userId, photoID, userName, imageRef)
                            }
                            .addOnFailureListener {e->
                                Log.i("TAG","failed to add nice status to firestore : ${e.message}")
                            }
                    } else if (it.contains("photos.$photoID.nices_userid") && !array.contains(auth.currentUser?.uid)){
                        db.collection("Users")
                            .document(userId)
                            .update("photos.$photoID.nices_userid", FieldValue.arrayUnion(auth.currentUser?.uid))
                            .addOnSuccessListener {
                                updateNiceStatusToSelf(userId, photoID, userName, imageRef)
                            }
                            .addOnFailureListener {e->
                                Log.i("TAG","failed to add nice status to firestore : ${e.message}")
                            }
                    } else {
                        db.collection("Users")
                            .document(userId)
                            .update("photos.$photoID.nices_userid", FieldValue.arrayRemove(auth.currentUser?.uid))
                            .addOnSuccessListener {
                                db.collection("Users")
                                    .document(auth.currentUser?.uid!!)
                                    .get()
                                    .addOnSuccessListener { document->
                                        if (document.exists()){
                                            val nicesMapData = document.get("nices") as Map<String,Map<String,String>>
                                            nicesMapData.forEach { (key, _) ->
                                                run {
                                                    if (nicesMapData[key]?.get("photo_id") == photoID) {
                                                        db.collection("Users")
                                                            .document(auth.currentUser?.uid!!)
                                                            .update("nices.$key", FieldValue.delete())
                                                            .addOnSuccessListener {
                                                                Log.i("TAG","deleted nice data from self firestore data")
                                                            }
                                                            .addOnFailureListener {e->
                                                                Log.i("TAG","failed to delete nice status from self firestore data : ${e.message}")
                                                            }
                                                    }
                                                }

                                            }
                                        }
                                    }
                            }
                            .addOnFailureListener {e->
                                Log.i("TAG","failed to add nice status to firestore : ${e.message}")
                            }
                    }

                }
            }
    }

    private fun updateNiceStatusToSelf(userId: String, photoID: String, userName: String, imageRef: String){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val niceMap = mapOf("user_id" to userId, "photo_id" to photoID, "timestamp" to dateFormat.toString(), "user_name" to userName, "image_ref" to imageRef)
        val niceUUID = UUID.randomUUID().toString()
        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .update("nices.$niceUUID", niceMap)
            .addOnSuccessListener {
                Log.i("TAG","nice data added to firestore")
            }
            .addOnFailureListener {e->
                Log.i("TAG","failed to add nice data to firestore : ${e.message}")
            }
    }

    private fun launchBullMagicTargetUserFragment(){
        val args = Bundle()
        args.putString("user_id", dataUserID)
        this.parentFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)
            ?.findNavController()
            ?.navigate(R.id.action_bullMagicItemFragment_to_bullMagicTargetUserFragment, args)
    }
}