package com.bullSaloon.bull.fragments.yourProfile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.adapters.YourProfileNicesRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentYourProfileNiceBinding
import com.bullSaloon.bull.genericClasses.dataClasses.MyNicesData
import com.bullSaloon.bull.viewModel.YourProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class YourProfileNiceFragment : Fragment() {
    private var _binding: FragmentYourProfileNiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataViewModel: YourProfileViewModel
    private var nicesList = mutableListOf<MyNicesData>()

    private val TAG = "YourProfileNiceFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourProfileNiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getNiceDataFromFireStore()

        binding.yourProfileNiceRecyclerView.layoutManager = LinearLayoutManager(activity)

    }

    private fun getNiceDataFromFireStore(){
        val db = Firebase.firestore
        val auth = Firebase.auth
        dataViewModel = ViewModelProvider(requireActivity()).get(YourProfileViewModel::class.java)

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .addSnapshotListener { document, error ->
                if (error == null){

                    if (document?.exists()!! && document.contains("nices")){
                        val nicesData = document.get("nices") as Map<String, Map<String,String>>

                        nicesData.forEach { (_, value) ->

                            val targetUserID = value["user_id"].toString()
                            val photoID = value["photo_id"].toString()
                            val timeStamp = value["timestamp"].toString()
                            val targetUserName = value["user_name"].toString()
                            val targetImageRef = value["image_ref"].toString()
                            val targetUserNameUnderScore = targetUserName.replace("\\s".toRegex(), "_")
                            val targetUserProfilePicRef= "User_Images/${targetUserID}/${targetUserNameUnderScore}_profilePicture.jpg"

                            val niceData = MyNicesData(targetUserID, photoID, targetUserName,targetImageRef,targetUserProfilePicRef, timeStamp)

                            nicesList.add(niceData)
                        }

                        dataViewModel.assignNicesData(nicesList)

                        if (nicesList.size != 0){
                            binding.textView.visibility = View.GONE
                        }

                        if (view != null){
                            dataViewModel.getNicesDataList().observe(viewLifecycleOwner,{
                                binding.yourProfileNiceRecyclerView.adapter = YourProfileNicesRecyclerViewAdapter(it)
                            })
                        }
                    }
                } else {
                    Log.i(TAG,"Error in retrieving nice data from FireStore : $error")
                }
            }
    }

}