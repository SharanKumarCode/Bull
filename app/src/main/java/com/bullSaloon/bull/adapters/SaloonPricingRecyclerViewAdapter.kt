package com.bullSaloon.bull.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.ViewHolderSaloonPricingItemBinding

class SaloonPricingRecyclerViewAdapter(_lists: MutableList<HashMap<String, Number>>): RecyclerView.Adapter<SaloonPricingRecyclerViewAdapter.SaloonPricingRecyclerViewHolder>() {

    private val lists = _lists

    inner class SaloonPricingRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding : ViewHolderSaloonPricingItemBinding = ViewHolderSaloonPricingItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SaloonPricingRecyclerViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_saloon_pricing_item,parent,false)
        return SaloonPricingRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaloonPricingRecyclerViewHolder, position: Int) {

        lists[position].forEach { (key, value) ->
            holder.binding.saloonPricingLabelText.text = key
            holder.binding.saloonPricingPriceText.text = holder.binding.root.resources.getString(R.string.textHaircutPrice, value.toString())
        }

        val width = holder.binding.saloonPricingLayout.layoutParams.width
        val height = holder.binding.saloonPricingLayout.layoutParams.height + (-100..100).random()

        val colors = listOf(
            R.color.random_color_1,
            R.color.random_color_2,
            R.color.random_color_3,
            R.color.black,
            R.color.teal_700)

        val sample = listOf<Int>(1,3,6)

        holder.binding.saloonPricingLayout.layoutParams = ConstraintLayout.LayoutParams(width,height)

        holder.binding.saloonPricingLabelText.setBackgroundColor(holder.binding.root.resources.getColor(colors.random()))


    }

    override fun getItemCount(): Int {
        return lists.size
    }
}