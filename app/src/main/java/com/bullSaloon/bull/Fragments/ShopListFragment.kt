package com.bullSaloon.bull.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.ShopRecyclerViewAdapter
import com.bullSaloon.bull.ViewModel.MainActivityViewModel
import com.bullSaloon.bull.shopDataPreviewClass
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ShopListFragment : Fragment() {

    private lateinit var shopRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shop_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopRecyclerView = view.findViewById(R.id.recyclerView)
        shopRecyclerView.layoutManager = LinearLayoutManager(activity)
        generateDataFirestore()

    }

    private fun generateDataFirestore(){
        val db = Firebase.firestore
        val shopLists = mutableListOf<shopDataPreviewClass>()
        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        db.collection("shops_data")
            .get()
            .addOnCompleteListener{
                for (document in it.result!!){

                    val shopID: Number? = document.getLong("shop_id")
                    val shopName: String? = document.getString("Shop_Name")
                    val areaName: String? = document.getString("area")
                    val rating: Long? = document.getLong("Rating")
                    val imageSource:String? = document.getString("Image")
                    val gender: String? = document.getString("Gender")
                    val openStatus: Boolean? = document.getBoolean("Open")
                    val contact: String? = document.getString("Contact")
                    val shopAddress: String? = document.getString("Address")

                    shopLists.add(shopDataPreviewClass(shopID, shopName,areaName, rating,imageSource,gender,openStatus, contact, shopAddress))
                }

                dataViewModel.assignShopData(shopLists)
                dataViewModel.getShopDataList().observe(viewLifecycleOwner, Observer { result ->
                    shopRecyclerView.adapter = ShopRecyclerViewAdapter(result, dataViewModel)
                })
            }

    }
}