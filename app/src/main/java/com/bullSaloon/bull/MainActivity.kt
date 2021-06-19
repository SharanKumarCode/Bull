package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var shopRecyclerView: RecyclerView
    private lateinit var searchButton: ImageView


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shopRecyclerView = findViewById(R.id.recyclerView)
        shopRecyclerView.layoutManager = LinearLayoutManager(this)
        generateDataFirestore()

        searchButton = findViewById(R.id.search_ImageView)

        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.action_bar_layout)

    }

    private fun generateDataFirestore(){
        val db = Firebase.firestore
        val shopLists = mutableListOf<shopDataPreviewClass>()

        db.collection("shops_data")
            .get()
            .addOnCompleteListener{
                for (document in it.result!!){

                    val shopName: String? = document.getString("Shop_Name")
                    val areaName: String? = document.getString("area")
                    val rating: Long? = document.getLong("Rating")
                    val imageSource:String? = document.getString("Image")
                    val gender: String? = document.getString("Gender")
                    val openStatus: Boolean? = document.getBoolean("Open")

                    shopLists.add(shopDataPreviewClass(shopName,areaName, rating,imageSource,gender,openStatus))
                }
                shopRecyclerView.adapter = ShopRecyclerViewAdapter(shopLists)
            }

    }
}