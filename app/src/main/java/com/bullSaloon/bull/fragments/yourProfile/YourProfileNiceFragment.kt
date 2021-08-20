package com.bullSaloon.bull.fragments.yourProfile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.adapters.YourProfileNicesRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentYourProfileNiceBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
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

        //restore scroll state
        val scrollState = SingletonUserData.getScrollState("YourProfileNiceRecycler")

        if (scrollState != null){
            Log.i("TAG", "onPause nice fragment scroll state not null")
            binding.yourProfileNiceRecyclerView.layoutManager?.onRestoreInstanceState(scrollState)
        }

    }

    override fun onPause() {
        super.onPause()

        Log.i("TAG", "onPause nice fragment")
        val recyclerState = binding.yourProfileNiceRecyclerView.layoutManager?.onSaveInstanceState()!!
        SingletonUserData.updateScrollState("YourProfileNiceRecycler",recyclerState)
    }

    private fun getNiceDataFromFireStore(){
        val db = Firebase.firestore
        val auth = Firebase.auth
        dataViewModel = ViewModelProvider(requireActivity()).get(YourProfileViewModel::class.java)

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .collection("nices")
            .addSnapshotListener { snapshot, error ->
                if (error == null){

                    if (!snapshot?.isEmpty!!){

                        for (document in snapshot.documents){
                            val targetUserID = document.get("user_id").toString()
                            val photoID = document.get("photo_id").toString()
                            val timeStamp = document.get("timestamp").toString()
                            val targetUserName = document.get("user_name").toString()
                            val targetImageRef = document.get("image_ref").toString()
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
                                binding.yourProfileNiceRecyclerView.adapter = YourProfileNicesRecyclerViewAdapter(it, this)
                                YourProfileNicesRecyclerViewAdapter(it, this).stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                            })
                        }
                    }
                } else {
                    Log.i(TAG,"Error in retrieving nice data from FireStore : $error")
                }
            }
    }

    companion object {
        private const val TAG = "TAG"
    }

}