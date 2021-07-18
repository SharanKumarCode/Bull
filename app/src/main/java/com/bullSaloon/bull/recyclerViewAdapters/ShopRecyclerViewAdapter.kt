package com.bullSaloon.bull.recyclerViewAdapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.ViewHolderShopItemBinding
import com.bullSaloon.bull.fragments.ShopItemFragment
import com.bullSaloon.bull.genericClasses.ShopDataPreviewClass
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import java.lang.Error

class ShopRecyclerViewAdapter(lists: MutableList<ShopDataPreviewClass>, dataViewModel: MainActivityViewModel): RecyclerView.Adapter<ShopRecyclerViewAdapter.ShopRecyclerViewHolder>() {

    private val shopList = lists
    private val dataModel = dataViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_shop_item,parent,false)
        return ShopRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopRecyclerViewHolder, position: Int) {
        val holderBinding = holder.binding

        val context:Context = holder.itemView.context
        val imageResource = context.resources.getIdentifier(shopList[position].imageSource, "drawable",context.packageName)
        holderBinding.shopImageLayoutBg.setBackgroundResource(imageResource)

        ViewCompat.setTransitionName(holderBinding.shopImageLayoutBg, shopList[position].shopID?.toString())

        holderBinding.shopNameTextView.text = shopList[position].shopName
        holderBinding.areaNameTextView.text = shopList[position].areaName


        when (shopList[position].gender) {
            "Male" -> {
                holderBinding.genderImageView.setImageResource(R.drawable.ic_male)
            }
            "Female" -> {
                holderBinding.genderImageView.setImageResource(R.drawable.ic_female)
            }
            else -> {
                holderBinding.genderImageView.setImageResource(R.drawable.ic_unisex)
            }
        }

        if (shopList[position].openStatus == true){
            holderBinding.openStatusTextView.text = "open"
            holderBinding.openStatusTextView.setTextColor(context.getColor(R.color.openStatusColor))
        } else {
            holderBinding.openStatusTextView.text = "closed"
            holderBinding.openStatusTextView.setTextColor(context.getColor(R.color.closeStatusColor))
        }

        for (i in 0 until shopList[position].rating!!){
            holder.ratingsImageViewList[i.toInt()].visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {

            try {
                startShopItemFragment(holder, dataModel, shopList[position])
            }catch (e: Error){
                Log.i("fragmentError", "error occurred: $e")
            }
        }

    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    private fun startShopItemFragment(holder: ShopRecyclerViewHolder, dataModel: MainActivityViewModel, shopList: ShopDataPreviewClass){
        val context = holder.itemView.context as FragmentActivity
        val shopItemFragment = ShopItemFragment()
        dataModel.putShopData(shopList.shopID)


        context.supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.shopFragmentContainer, shopItemFragment)
            .addToBackStack(null)
            .commit()
    }

    inner class ShopRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: ViewHolderShopItemBinding = ViewHolderShopItemBinding.bind(itemView)
        val ratingsImageViewList = mutableListOf<ImageView>()

        init {
            ratingsImageViewList.add(binding.ratingImageView1)
            ratingsImageViewList.add(binding.ratingImageView2)
            ratingsImageViewList.add(binding.ratingImageView3)
            ratingsImageViewList.add(binding.ratingImageView4)
            ratingsImageViewList.add(binding.ratingImageView5)
        }
    }
}