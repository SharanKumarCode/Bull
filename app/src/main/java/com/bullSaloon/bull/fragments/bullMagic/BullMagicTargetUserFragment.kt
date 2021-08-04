package com.bullSaloon.bull.fragments.bullMagic

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.adapters.BullMagicTargetUserRecyclerViewAdapter
import com.bullSaloon.bull.adapters.DialogFollowersRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentBullMagicTargetUserBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.MyPhotosData
import com.bullSaloon.bull.viewModel.YourProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class BullMagicTargetUserFragment : Fragment() {

    private var _binding: FragmentBullMagicTargetUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataViewModel: YourProfileViewModel
    private lateinit var dataUserID :String
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        exitTransition = inflaterTrans.inflateTransition(R.transition.slide_left_to_right)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBullMagicTargetUserBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            onBackButtonPressed()
        }

        dataUserID = arguments?.getString("user_id").toString()

        binding.bullMagicTargetUserRecyclerView.layoutManager = GridLayoutManager(activity, 2)
        generateFirestoreData()

//        check and set Follow Status button
        checkAndSetFollowStatus()

//        set follow count
        setFollowCount()

//        set follow status
        binding.bullMagicTargetUserFollowButton.setOnClickListener {
            setFollowStatus()
        }

        binding.bullMagicTargetUserFollowersLayout.setOnClickListener {
            setUpDialogBoxFollow("Followers")
        }

        binding.bullMagicTargetUserFollowingLayout.setOnClickListener {
            setUpDialogBoxFollow("Following")
        }

    }

    @SuppressLint("RestrictedApi")
    private fun onBackButtonPressed(){

        val navController = this.parentFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)?.findNavController()
        navController?.navigate(R.id.action_bullMagicTargetUserFragment_to_bullMagicListFragment)

    }

    private fun generateFirestoreData(){

        dataViewModel = ViewModelProvider(requireActivity()).get(YourProfileViewModel::class.java)

        db.collection("Users")
            .document(dataUserID)
            .addSnapshotListener { value, error ->
                if (error == null){
                    val myPhotosList: MutableList<MyPhotosData>  = mutableListOf()
                    if (value?.exists()!! && value.contains("photos")){
                        val photos = value.get("photos") as Map<String, Map<String,Any>>
                        photos.forEach { (keys, values) ->
                            val nices = if (values["nices"] != null) values["nices"] as List<String> else mutableListOf()
                            val saloonName = if (values["saloon_name"].toString() != "null") values["saloon_name"].toString() else ""
                            val caption = if (values["caption"].toString() != "null") values["caption"].toString() else ""
                            val data = MyPhotosData(
                                keys,
                                values["image_ref"].toString(),
                                value["user_name"].toString(),
                                value["user_id"].toString(),
                                nices.size,
                                values["timestamp"].toString(),
                                saloonName,
                                caption
                            )

                            binding.bullMagicTargetUserNameTextView.text = value["user_name"].toString()
                            val userNameUnderscore = value["user_name"].toString().replace("\\s".toRegex(), "_")
                            val imageUrl = "User_Images/${dataUserID}/${userNameUnderscore}_profilePicture.jpg"
                            setTargetUserProfilePic(imageUrl)

                            myPhotosList.add(data)
                        }
                    }

                    if (myPhotosList.size != 0){
                        binding.bullMagicTargetUserPhotosStatusText.visibility = View.GONE
                    } else {
                        binding.bullMagicTargetUserPhotosStatusText.visibility = View.VISIBLE
                    }

                    myPhotosList.sortBy {
                        it.timestamp
                    }
                    myPhotosList.reverse()
                    binding.bullMagicTargetUserRecyclerView.adapter = BullMagicTargetUserRecyclerViewAdapter(myPhotosList, this)
                }
                else {
                    Log.i(TAG, "error occurred: $error")
                }
            }
    }

    private fun setTargetUserProfilePic(profilePicRef: String){

        val imageRef = Firebase.storage.reference.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .circleCrop()
            .placeholder(R.drawable.ic_baseline_person_black_40)
            .into(binding.bullMagicTargetUserProfilePic)
    }

    private fun checkAndSetFollowStatus(){

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .addSnapshotListener { value, error ->
                if (error == null){
                    if (value?.exists()!!){
                        val following = if (value.get("following") != null) value.get("following") as  ArrayList<String> else arrayListOf()
                        if (following.contains(dataUserID)){
                            binding.bullMagicTargetUserFollowButton.text = activity?.resources?.getString(R.string.buttonUnFollow)
                        } else {
                            binding.bullMagicTargetUserFollowButton.text = activity?.resources?.getString(R.string.buttonFollow)
                        }
                    }
                } else {
                    Log.i(TAG, "Error in getting follow status: $error")
                }
            }
    }

    private fun setFollowCount(){
        db.collection("Users")
            .document(dataUserID)
            .addSnapshotListener { value, error ->
                if (error == null){
                    if (value?.exists()!!){
                        val following = if (value.get("following") != null) value.get("following") as  ArrayList<String> else arrayListOf()
                        val followingCount = following.size

                        val followers = if (value.get("followers") != null) value.get("followers") as  ArrayList<String> else arrayListOf()
                        val followerCount = followers.size

                        binding.bullMagicTargetUserFollowersText.text = followerCount.toString()
                        binding.bullMagicTargetUserFollowingText.text = followingCount.toString()
                    }
                } else {
                    Log.i(TAG, "Error in getting follow count: $error")
                }
            }
    }

    private fun setFollowStatus(){

        db.collection("Users")
            .document(dataUserID)
            .get()
            .addOnSuccessListener { document->
                if (document.exists()){
                    val followers = if (document.get("followers") != null) document.get("followers") as  ArrayList<String> else arrayListOf()
                    if (followers.contains(auth.currentUser?.uid)){
                        db.collection("Users")
                            .document(dataUserID)
                            .update("followers", FieldValue.arrayRemove(auth.currentUser?.uid))
                            .addOnSuccessListener {
                                Log.i(TAG, "Removed follower data")
                            }
                            .addOnFailureListener {error->
                                Log.i(TAG, "Error in removing follow count: $error")
                            }

                        db.collection("Users")
                            .document(auth.currentUser?.uid!!)
                            .update("following", FieldValue.arrayRemove(dataUserID))
                            .addOnSuccessListener {
                                Log.i(TAG, "Removed following data")
                            }
                            .addOnFailureListener {error->
                                Log.i(TAG, "Error in removing following count: $error")
                            }
                    } else {
                        db.collection("Users")
                            .document(dataUserID)
                            .update("followers", FieldValue.arrayUnion(auth.currentUser?.uid))
                            .addOnSuccessListener {
                                Log.i(TAG, "Added follower data")
                            }
                            .addOnFailureListener {error->
                                Log.i(TAG, "Error in Adding follow count: $error")
                            }

                        db.collection("Users")
                            .document(auth.currentUser?.uid!!)
                            .update("following", FieldValue.arrayUnion(dataUserID))
                            .addOnSuccessListener {
                                Log.i(TAG, "Added following data")
                            }
                            .addOnFailureListener {error->
                                Log.i(TAG, "Error in Adding following count: $error")
                            }
                    }
                }
            }
    }

    private fun setUpDialogBoxFollow(_dataType: String){

        var userLists: MutableList<String>
        val db = Firebase.firestore

        val dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.dialog_box_followers_list)
        val closeButton = dialog.findViewById<ImageView>(R.id.dialogFollowPopCloseButton)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.dialogFollowPopUpRecycler)
        val title = dialog.findViewById<TextView>(R.id.dialogFollowPopUpText)

        title.text = _dataType

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        dialog.show()

        val dataType = if (_dataType == "Followers"){
            "followers"
        } else {
            "following"
        }

        db.collection("Users")
            .document(dataUserID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val followList = if (it.get(dataType) !=  null) it.get(dataType)  as ArrayList<String> else arrayListOf()
                    userLists = followList

                    recyclerView.adapter = DialogFollowersRecyclerViewAdapter(userLists, this)
                }
            }
            .addOnFailureListener { e->
                Log.i("TAG", "Error in retrieving follow data : ${e.message}")
            }
    }

    companion object {
        private const val TAG = "TAG"
    }
}