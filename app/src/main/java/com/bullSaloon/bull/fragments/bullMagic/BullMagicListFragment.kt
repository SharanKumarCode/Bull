package com.bullSaloon.bull.fragments.bullMagic


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.HandlerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.adapters.BullMagicListRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentBullMagicListBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.BullMagicListData
import com.bullSaloon.bull.viewModel.MainActivityViewModel


class BullMagicListFragment : Fragment() {

    private var _binding: FragmentBullMagicListBinding? = null
    private val binding get() = _binding!!
    private var bullMagicList = mutableListOf<BullMagicListData>()

    private val db = SingletonInstances.getFireStoreInstance()
    private val auth = SingletonInstances.getAuthInstance()

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
        _binding = FragmentBullMagicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getFirebaseData()

        binding.recyclerViewBullMagicList.layoutManager = LinearLayoutManager(activity)

        //restore scroll state
        val scrollState = SingletonUserData.getScrollState("BullMagicListRecycler")

        if (scrollState != null){
            binding.recyclerViewBullMagicList.layoutManager?.onRestoreInstanceState(scrollState)
        }

        binding.bullMagicListRefresher.setOnRefreshListener {
            getFirebaseData()

            HandlerCompat.postDelayed(
                Handler(Looper.getMainLooper()),
                {
                    binding.bullMagicListRefresher.isRefreshing = false
                }, null, 1000)
        }

    }

    override fun onPause() {
        super.onPause()

        val recyclerState = binding.recyclerViewBullMagicList.layoutManager?.onSaveInstanceState()!!
        SingletonUserData.updateScrollState("BullMagicListRecycler",recyclerState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getFirebaseData() {

        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        bullMagicList.clear()
        dataViewModel.temp.clear()

        db.collection("Users")
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (userDocument in it.documents) {

                        db.collection("Users")
                            .document(userDocument.id)
                            .collection("photos")
                            .get()
                            .addOnSuccessListener { photoSnapshots ->

                                for (photoDoc in photoSnapshots){

                                    val nicesUserId = if (photoDoc.get("nices_userid") != null) photoDoc.get("nices_userid") as List<String> else null
                                    val niceStatus = nicesUserId?.contains(auth.currentUser?.uid)
                                    val saloonName = if (photoDoc.get("saloon_name") != null) photoDoc.get("saloon_name").toString() else ""
                                    val caption = if (photoDoc.get("caption") != null) photoDoc.get("caption").toString() else ""
                                    val data = BullMagicListData(
                                        userDocument.getString("user_id").toString(),
                                        userDocument.getString("user_name").toString(),
                                        photoDoc.getString("photoID").toString(),
                                        photoDoc.getString("image_ref").toString(),
                                        photoDoc.getString("timestamp").toString(),
                                        niceStatus ?: false,
                                        nicesUserId?.size ?: 0,
                                        saloonName,
                                        caption
                                    )

                                    bullMagicList.add(data)
                                    dataViewModel.putBullMagicData(data)
                                }
                            }
                            .addOnFailureListener { e->
                                Log.i(TAG,"Error fetching photo data : ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener {e->
                Log.i(TAG,"Error fetching user data : ${e.message}")
            }
            .addOnCompleteListener {
                if(view != null){
                    dataViewModel.getBullMagicDataList().observe(viewLifecycleOwner, {data->
                        if (data != null){
                            binding.recyclerViewBullMagicList.adapter = BullMagicListRecyclerViewAdapter(data, this)
                        }
                    })
                }
            }
    }

    companion object {
        private const val TAG = "TAGBullMagicListFragment"
    }
}