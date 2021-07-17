package com.bullSaloon.bull.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.recyclerViewAdapters.BullMagicListRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentBullMagicListBinding
import com.bullSaloon.bull.genericClasses.BullMagicListData
import com.bullSaloon.bull.genericClasses.UserDataClass
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase


class BullMagicListFragment : Fragment() {

    private var _binding: FragmentBullMagicListBinding? = null
    private val binding get() = _binding!!

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
                                    val shopName = if (values["shop_name"] != null) values["shop_name"].toString() else ""
                                    val caption = if (values["caption"] != null) values["shop_name"].toString() else ""
                                    val data = BullMagicListData(
                                        document.getString("user_id")!!,
                                        document.getString("user_name")!!,
                                        keys,
                                        values["image_ref"].toString(),
                                        values["timestamp"].toString(),
                                        niceStatus ?: false,
                                        nicesUserId?.size ?: 0,
                                        shopName,
                                        caption
                                    )
                                    bullMagicList.add(data)
                                }
                            }
                        }
                        binding.recyclerViewBullMagicList.adapter = BullMagicListRecyclerViewAdapter(bullMagicList)
                        }
                }

    }

}