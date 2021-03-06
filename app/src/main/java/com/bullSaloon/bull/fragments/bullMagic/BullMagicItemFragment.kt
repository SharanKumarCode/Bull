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
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.adapters.CommentRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentBullMagicItemBinding
import com.bullSaloon.bull.fragments.CameraFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.CommentDataClass
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.bumptech.glide.request.target.Target
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
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
    private val storageRef = SingletonInstances.getStorageReference()
    private val auth = SingletonInstances.getAuthInstance()
    private val db = SingletonInstances.getFireStoreInstance()
    private lateinit var currentUserID: String

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

        currentUserID = auth.currentUser?.uid!!

        userImageData = (arguments?.getSerializable("userImageData") as HashMap<String, String>?)!!
        dataImageRef = userImageData["imageRef"].toString()
        dataPhotoID = userImageData["photo_id"].toString()
        dataUserID = userImageData["user_id"].toString()

        isCommentClicked = userImageData["clickedComments"].toString() == "true"

        binding.bullMagicPhotoItemDate.text = ""

        getUserNameDataFromFireStore()
        getCommentsFromFireStore()

        binding.bullMagicItemCommentRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.bullMagicItemUserName.setOnClickListener {
            launchBullMagicTargetUserFragment()
        }

        binding.bullMagicEnterComment.setOnClickListener {
            updateCommentToFireStore()
        }

        //check nice status and number of nice
        db.collection("Users")
            .document(dataUserID)
            .collection("photos")
            .document(dataPhotoID)
            .addSnapshotListener { value, error ->
                if (error == null){
                    if (value?.exists() == true){
                        val array = value.get("nices_userid") as List<String>
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
            binding.bullMagicEditCommentTextLayout.requestFocus()
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
    private fun getUserNameDataFromFireStore(){

        db.collection("Users")
            .document(dataUserID)
            .get()
            .addOnSuccessListener {

                val userName = it.get("user_name").toString()

//                set user name
                binding.bullMagicItemUserName.text = userName

//                set userProfilePic
                val targetUserNameUnderScore = userName.replace("\\s".toRegex(), "_")
                val targetUserProfilePicRef= "User_Images/${dataUserID}/${targetUserNameUnderScore}_profilePicture.jpg"
                setTargetUserProfile(targetUserProfilePicRef)

                getImageDataFromFireStore(userName)

            }
            .addOnFailureListener {e->
                Log.i(TAG,"Error in getting username : ${e.message}")
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getImageDataFromFireStore(userName : String){

        db.collection("Users")
            .document(dataUserID)
            .collection("photos")
            .document(dataPhotoID)
            .get()
            .addOnSuccessListener { document->
                if (document.exists()){

                    val timeStamp = document.getString("timestamp").toString()
                    val saloon = document.getString("saloon_name").toString()
                    val caption = document.getString("caption").toString()


                    //set date
                    val date = timeStamp.substring(0,10)
                    val dateFormatted = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
                    val month = dateFormatted.month.toString()
                        .lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                    binding.bullMagicPhotoItemDate.text = resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,dateFormatted.year.toString())

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

                    //        update nice status
                    binding.bullMagicNiceImageView.setOnClickListener {
                        updateNiceStatus(dataUserID, dataPhotoID, userName, dataImageRef)
                    }
                }
            }
            .addOnFailureListener {e->
                Log.i(TAG,"Error in getting image data : ${e.message}")
            }
    }

    private fun getCommentsFromFireStore(){

        val commentLists = mutableListOf<CommentDataClass>()
        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        db.collection("Users")
            .document(dataUserID)
            .collection("photos")
            .document(dataPhotoID)
            .collection("comments")
            .orderBy("timestamp" , Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    for (document in it.documents){
                        val commentID = document.get("comment_id").toString()
                        val commentUserID = document.get("user_id").toString()
                        val timestamp = document.get("timestamp").toString()
                        val comment = document.get("comment").toString()

                        val commentData = CommentDataClass(commentID, commentUserID, comment, timestamp, dataUserID)
                        commentLists.add(commentData)
                    }
                }

                dataViewModel.putCommentListData(commentLists)
                dataViewModel.getCommentListData().observe(viewLifecycleOwner , { data->
                    if (data.size > 0){
                        binding.bullMagicItemCommentStatus.visibility = View.GONE
                    } else {
                        binding.bullMagicItemCommentStatus.visibility = View.VISIBLE
                    }
                    binding.bullMagicItemCommentRecyclerView.adapter = CommentRecyclerViewAdapter(data, dataPhotoID, this)

                })
            }
            .addOnFailureListener { e->
                Log.i(TAG, "Error in fetching comments: ${e.message}")
            }
    }

    private fun setImageFromFirebase(imageUrl: String){

        val imageRef = storageRef.storage.getReferenceFromUrl(imageUrl)
        val width = Resources.getSystem().displayMetrics.widthPixels

        GlideApp.with(this)
            .load(imageRef)
            .override(width, Target.SIZE_ORIGINAL)
            .placeholder(R.drawable.ic_bull)
            .into(binding.bullMagicPhotoItemImageView)
    }

    private fun setTargetUserProfile(profilePicRef: String){

        val imageRef = storageRef.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(binding.bullMagicItemUserProfilePic)
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
            .document(currentUserID)
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

    private fun updateCommentToFireStore(){
        val commentID = UUID.randomUUID().toString()
        val timeStamp = SimpleDateFormat(CameraFragment.FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val comment = binding.bullMagicEditCommentTextField.text.toString()
        val userID = auth.currentUser?.uid.toString()

        val commentMapData = hashMapOf<String, String>(
            "comment_id" to commentID,
            "timestamp" to timeStamp,
            "comment" to comment,
            "user_id" to userID
        )

        db.collection("Users")
            .document(dataUserID)
            .collection("photos")
            .document(dataPhotoID)
            .collection("comments")
            .document(commentID)
            .set(commentMapData)
            .addOnSuccessListener {
                Log.i(TAG, "Comment is added")
                binding.bullMagicEditCommentTextField.text?.clear()
                getCommentsFromFireStore()
            }
            .addOnFailureListener {
                Toast.makeText(requireActivity(),"Error in adding comment. \n\nPlease try again later",Toast.LENGTH_SHORT).show()
                Log.i(TAG, "error in adding comment : ${it.message}")
            }
    }

    private fun launchBullMagicTargetUserFragment(){
        val args = Bundle()
        args.putString("user_id", dataUserID)
        this.parentFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)
            ?.findNavController()
            ?.navigate(R.id.action_bullMagicItemFragment_to_bullMagicTargetUserFragment, args)
    }

    fun getFirestoreFuncFromBullMagicItem(){
        getCommentsFromFireStore()
    }

    companion object {
        private const val TAG = "TAGBullMagicItemFragment"
    }
}