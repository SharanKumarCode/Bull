package com.bullSaloon.bull.recyclerViewAdapters

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.BullMagicItemViewHolderBinding

class BullMagicListRecyclerViewAdapter(lists: MutableList<Map<String,Any>>): RecyclerView.Adapter<BullMagicListRecyclerViewAdapter.BullMagicListRecyclerViewHolder>() {

    private val bullMagicLists = lists

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BullMagicListRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bull_magic_item_view_holder,parent,false)
        return BullMagicListRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BullMagicListRecyclerViewHolder, position: Int) {
        val holderBinding = holder.binding

        val context: Context = holder.itemView.context
        val imageResource = context.resources.getIdentifier(bullMagicLists[position]["image"].toString(), "drawable",context.packageName)
        val bitmap = BitmapFactory.decodeResource(context.resources, imageResource)
        val displayMetrics = DisplayMetrics()

        var width = displayMetrics.widthPixels
        var height = displayMetrics.heightPixels
        val thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap,400,800)
        val bitmapDrawable = BitmapDrawable(context.resources,thumbBitmap)
        holderBinding.BullMagicImageView.background = bitmapDrawable

        holderBinding.BullMagicUserName.text = bullMagicLists[position]["User_Name"].toString()
        holderBinding.BullMagicShopName.text = bullMagicLists[position]["Shop_Name"].toString()
        holderBinding.NicesTextView.text = bullMagicLists[position]["Nices"].toString()

        if (bullMagicLists[position]["Nice_filled"] == true){
            holderBinding.BullMagicNiceImageView.setImageResource(R.drawable.ic_nice_filled_icon)
        } else {
            holderBinding.BullMagicNiceImageView.setImageResource(R.drawable.ic_nice_blank_icon)
        }

    }

    override fun getItemCount(): Int {
        return bullMagicLists.size
    }

    inner class BullMagicListRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: BullMagicItemViewHolderBinding = BullMagicItemViewHolderBinding.bind(itemView)

    }

}