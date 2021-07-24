package com.bullSaloon.bull.fragments.yourProfile

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentYourProfilePhotosBinding
import com.bullSaloon.bull.genericClasses.dataClasses.MyPhotosData
import com.bullSaloon.bull.adapters.YourProfilePhotosRecyclerViewAdapter
import com.bullSaloon.bull.viewModel.YourProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class YourProfilePhotosFragment : Fragment() {

    private var _binding: FragmentYourProfilePhotosBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataViewModel: YourProfileViewModel

    private val TAG = "TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        exitTransition = inflaterTrans.inflateTransition(R.transition.slide_left_to_right)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYourProfilePhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.yourProfilePhotosRecycler.layoutManager = GridLayoutManager(activity, 2)
        generateFirestoreData()
    }

    private fun generateFirestoreData(){

        val db = Firebase.firestore
        val auth = Firebase.auth
        dataViewModel = ViewModelProvider(requireActivity()).get(YourProfileViewModel::class.java)

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .addSnapshotListener { value, error ->
                if (error == null){
                    var myPhotosList: MutableList<MyPhotosData>  = mutableListOf()
                    if (value?.exists()!! && value.contains("photos")){
                        val photos = value.get("photos") as Map<String, Map<String,Any>>
                        photos.forEach { keys, values ->
                            val nices = if (values.get("nices") != null) values.get("nices") as List<String> else mutableListOf()
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
                            myPhotosList.add(data)
                        }
                    }

                    myPhotosList.sortBy {
                        it.timestamp
                    }
                    myPhotosList.reverse()
                    binding.yourProfilePhotosRecycler.adapter = YourProfilePhotosRecyclerViewAdapter(myPhotosList, dataViewModel, this)
                }
                else {
                    Log.i(TAG, "error occurred: $error")
                }
            }
    }
}