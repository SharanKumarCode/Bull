package com.bullSaloon.bull

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ShopRecyclerViewAdapter(lists: MutableList<shopDataPreviewClass>): RecyclerView.Adapter<ShopRecyclerViewHolder>() {

    private val shopList = lists

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shop_item_view_holder,parent,false)
        return ShopRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopRecyclerViewHolder, position: Int) {

        val context:Context = holder.itemView.context
        val imageResource = context.resources.getIdentifier(shopList[position].imageSource, "drawable",context.packageName)
        holder.shopImage.setBackgroundResource(imageResource)

        holder.shopName.text = shopList[position].shopName
        holder.areaName.text = shopList[position].areaName


        if (shopList[position].gender == "Male"){
            holder.gender.setImageResource(R.drawable.ic_male)
        } else if (shopList[position].gender == "Female") {
            holder.gender.setImageResource(R.drawable.ic_female)
        } else {
            holder.gender.setImageResource(R.drawable.ic_unisex)
        }

        if (shopList[position].openStatus == true){
            holder.openStatus.text = "open"
            holder.openStatus.setTextColor(context.getColor(R.color.openStatusColor))
        } else {
            holder.openStatus.text = "closed"
            holder.openStatus.setTextColor(context.getColor(R.color.closeStatusColor))
        }

        for (i in 0 until shopList[position].rating!!){
            holder.ratingsImageViewList[i.toInt()].visibility = View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return shopList.size
    }
}