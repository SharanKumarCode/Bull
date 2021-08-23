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
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.FragmentYourProfilePhotosBinding
import com.bullSaloon.bull.genericClasses.dataClasses.MyPhotosData
import com.bullSaloon.bull.adapters.YourProfilePhotosRecyclerViewAdapter
import com.bullSaloon.bull.viewModel.YourProfileViewModel

class YourProfilePhotosFragment : Fragment() {

    private var _binding: FragmentYourProfilePhotosBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataViewModel: YourProfileViewModel

    private val db = SingletonInstances.getFireStoreInstance()
    private val auth = SingletonInstances.getAuthInstance()

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
        _binding = FragmentYourProfilePhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.yourProfilePhotosRecycler.layoutManager = GridLayoutManager(activity, 2)
        getPhotosFirestoreData()
    }

    private fun getPhotosFirestoreData(){

        dataViewModel = ViewModelProvider(requireActivity()).get(YourProfileViewModel::class.java)

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .collection("photos")
            .addSnapshotListener { snapshot, error ->
                if (error == null){
                    val myPhotosList: MutableList<MyPhotosData>  = mutableListOf()

                    if (!snapshot?.isEmpty!!){

                        for (document in snapshot.documents){
                            val nicesCount = document.get("nices_userid")as ArrayList<String>
                            val saloonName = if (document.get("saloon_name").toString() != "null") document.get("saloon_name").toString() else ""
                            val caption = if (document.get("caption").toString() != "null") document.get("caption").toString() else ""
                            val data = MyPhotosData(
                                document.get("photoID").toString(),
                                document.get("image_ref").toString(),
                                document.get("user_name").toString(),
                                document.get("user_id").toString(),
                                nicesCount.size,
                                document.get("timestamp").toString(),
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

    companion object {
        private const val TAG = "TAG"
    }
}