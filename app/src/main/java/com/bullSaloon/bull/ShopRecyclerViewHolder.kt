package com.bullSaloon.bull

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShopRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    val shopName: TextView = itemView.findViewById<TextView>(R.id.shopNameTextView)
    val areaName: TextView = itemView.findViewById<TextView>(R.id.areaName_TextView)
    val gender: ImageView = itemView.findViewById<ImageView>(R.id.gender_ImageView)
    val openStatus: TextView = itemView.findViewById<TextView>(R.id.openStatus_TextView)
    val shopImage: LinearLayout = itemView.findViewById<LinearLayout>(R.id.shopImageLayoutBg)
    val ratingsImageViewList = mutableListOf<ImageView>()
    init {
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_1))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_2))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_3))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_4))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_5))
    }
}