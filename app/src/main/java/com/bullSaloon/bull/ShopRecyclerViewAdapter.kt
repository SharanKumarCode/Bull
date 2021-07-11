package com.bullSaloon.bull

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.Fragments.ShopItemFragment
import com.bullSaloon.bull.ViewModel.MainActivityViewModel
import java.lang.Error

class ShopRecyclerViewAdapter(lists: MutableList<shopDataPreviewClass>, dataViewModel: MainActivityViewModel): RecyclerView.Adapter<ShopRecyclerViewHolder>() {

    private val shopList = lists
    private val dataModel = dataViewModel

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

        holder.itemView.setOnClickListener {

            Log.i("fragmenterror", "recycler view item: ${holder.areaName.id} clicked")
            try {
                Log.i("fragmenterror", "recycler view item: ${it.id} clicked")
                startShopItemFragment(holder, dataModel, shopList[position])
            }catch (e: Error){
                Log.i("fragmenterror", "error occured: $e")
            }

        }

    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    private fun startShopItemFragment(holder: ShopRecyclerViewHolder, dataModel: MainActivityViewModel, shopList: shopDataPreviewClass){
        val context = holder.itemView.context as FragmentActivity
        val shopItemFragment = ShopItemFragment()
        dataModel.putShopData(shopList.shopID)
        context.supportFragmentManager.beginTransaction().replace(R.id.shopFragmentContainer, shopItemFragment).addToBackStack(null).commit()
    }
}