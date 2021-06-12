package com.example.bull

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var shopRecyclerView: RecyclerView
    private lateinit var searchButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        shopRecyclerView = findViewById(R.id.recyclerView)
        shopRecyclerView.layoutManager = LinearLayoutManager(this)
        shopRecyclerView.adapter = ShopRecyclerViewAdapter(generateList())
        searchButton = findViewById(R.id.search_ImageView)

    }

    private fun generateList():MutableList<shopDataPreviewClass>{
        val shopPreviewDataList = this.assets.open("shopData.json").bufferedReader().use { it.readText() }
        val shopJsonObject = JSONObject(shopPreviewDataList)
        val datas = shopJsonObject.getJSONArray("Shops")
        val shopLists = mutableListOf<shopDataPreviewClass>()
        for (data in 0 until datas.length()){
            val d = datas.getJSONObject(data)
            val shopName = d.getString("Shop_Name")
            val rating = d.getLong("Rating")
            val imageSource = d.getString("Image")
            val gender = d.getString("Gender")
            val openStatus = d.getBoolean("Open")

            shopLists.add(shopDataPreviewClass(shopName,rating,imageSource,gender,openStatus))
        }
        return shopLists
    }
}