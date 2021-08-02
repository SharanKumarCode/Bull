package com.bullSaloon.bull.fragments.bullMagic


import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.R
import com.bullSaloon.bull.adapters.BullMagicListRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentBullMagicListBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.BullMagicListData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class BullMagicListFragment : Fragment() {

    private var _binding: FragmentBullMagicListBinding? = null
    private val binding get() = _binding!!

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
        val db = Firebase.firestore
        val auth = Firebase.auth

        val bullMagicList = mutableListOf<BullMagicListData>()

        db.collection("Users")
            .get()
            .addOnSuccessListener { it ->
                if (!it.isEmpty) {
                        for (document in it.documents) {
                            if (document.id != auth.currentUser?.uid && document.get("photos") != null) {
                                val photos = document.get("photos") as Map<String,Map<String, *>>
                                photos.forEach{(keys, values) ->
                                    val nicesUserId = if (values["nices_userid"] != null) values["nices_userid"] as List<String> else null
                                    val niceStatus = nicesUserId?.contains(auth.currentUser?.uid)
                                    val saloonName = if (values["saloon_name"] != null) values["saloon_name"].toString() else ""
                                    val caption = if (values["caption"] != null) values["caption"].toString() else ""
                                    val data = BullMagicListData(
                                        document.getString("user_id")!!,
                                        document.getString("user_name")!!,
                                        keys,
                                        values["image_ref"].toString(),
                                        values["timestamp"].toString(),
                                        niceStatus ?: false,
                                        nicesUserId?.size ?: 0,
                                        saloonName,
                                        caption
                                    )
                                    bullMagicList.add(data)
                                }
                            }
                        }
                    binding.recyclerViewBullMagicList.adapter = BullMagicListRecyclerViewAdapter(bullMagicList, this)
                        }
                }

    }

}