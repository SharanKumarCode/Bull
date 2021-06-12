package com.example.bull

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShopRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val shopName = itemView.findViewById<TextView>(R.id.shopNameTextView)
    val gender = itemView.findViewById<ImageView>(R.id.gender_ImageView)
    val openStatus = itemView.findViewById<ImageView>(R.id.openStatus_ImageView)
    val shopImage = itemView.findViewById<LinearLayout>(R.id.shopImageLayoutBg)
    val ratingsImageViewList = mutableListOf<ImageView>()
    init {
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_1))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_2))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_3))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_4))
        ratingsImageViewList.add(itemView.findViewById(R.id.rating_ImageView_5))
    }
}