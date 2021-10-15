package com.bullSaloon.bull.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.ViewHolderShopItemBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.SaloonDataClass
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.lang.Error
import kotlin.math.roundToLong

class SaloonListRecyclerViewAdapter(lists: MutableList<SaloonDataClass>, dataViewModel: MainActivityViewModel, _fragment: Fragment): RecyclerView.Adapter<SaloonListRecyclerViewAdapter.ShopRecyclerViewHolder>() {

    private val saloonList = lists
    private val dataModel = dataViewModel
    private val fragment = _fragment
    private val storageRef = SingletonInstances.getStorageReference()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_shop_item,parent,false)
        return ShopRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopRecyclerViewHolder, position: Int) {
        val holderBinding = holder.binding

        val context:Context = holder.itemView.context
        setSaloonDisplayPic(holderBinding, saloonList[position].imageSource!!)

        holderBinding.saloonNameTextView.text = saloonList[position].saloonName
        holderBinding.areaNameTextView.text = saloonList[position].areaName

        if (saloonList[position].openStatus == true){
            holderBinding.openStatusTextView.text = holderBinding.root.context.resources.getString(R.string.placeHolderOpenStatus)
            holderBinding.openStatusTextView.setTextColor(context.getColor(R.color.openStatusColor))
        } else {
            holderBinding.openStatusTextView.text = holderBinding.root.context.resources.getString(R.string.placeHolderClosedStatus)
            holderBinding.openStatusTextView.setTextColor(context.getColor(R.color.closeStatusColor))
        }

        when(saloonList[position].rating){
            1 -> setRatingPic(holderBinding, R.drawable.ic_rating_one_stars)
            2 -> setRatingPic(holderBinding, R.drawable.ic_rating_two_stars)
            3 -> setRatingPic(holderBinding, R.drawable.ic_rating_three_stars)
            4 -> setRatingPic(holderBinding, R.drawable.ic_rating_four_stars)
            5 -> setRatingPic(holderBinding, R.drawable.ic_rating_five_stars)
            else -> setRatingPic(holderBinding, R.drawable.ic_rating_one_stars)
        }

        holderBinding.saloonHaircutPriceText.text = String.format(holderBinding.root.context.resources.getString(R.string.textHaircutPrice), saloonList[position].haircutPrice.toString())
        holderBinding.saloonShavingPriceText.text = String.format(holderBinding.root.context.resources.getString(R.string.textHaircutPrice), saloonList[position].shavingPrice.toString())

        if (saloonList[position].distance != null){
            holderBinding.textSaloonDistanceLayout.visibility = View.VISIBLE
            holderBinding.textSaloonDistance.text = holderBinding.root.resources.getString(
                                                    R.string.textSaloonDistance,
                                                    (saloonList[position].distance?.div(1000))?.roundToLong().toString()
                                                )
        }

        holder.binding.cardView.animation = AnimationUtils.loadAnimation(holder.binding.root.context, R.anim.animation)

        holder.itemView.setOnClickListener {

            try {
                startShopItemFragment(fragment, dataModel, saloonList[position])
            }catch (e: Error){
                Log.i("fragmentError", "error occurred: $e")
            }
        }
    }

    override fun getItemCount(): Int {
        return saloonList.size
    }

    private fun startShopItemFragment(holder: Fragment, dataModel: MainActivityViewModel, saloonList: SaloonDataClass){

        dataModel.putShopData(saloonList.saloonID)

        val saloonNavHostFragment = holder.parentFragment?.childFragmentManager?.findFragmentById(R.id.saloonFragmentContainer)
        val navController = saloonNavHostFragment?.findNavController()
        navController?.navigate(R.id.action_shopListFragment_to_shopItemFragment)
    }

    private fun setSaloonDisplayPic(binding: ViewHolderShopItemBinding, profilePicRef: String){

        val imageRef = storageRef.storage.getReferenceFromUrl(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .centerCrop()
            .placeholder(R.drawable.ic_bull)
            .into(binding.saloonDisplayImageView)
    }

    private fun setRatingPic(binding: ViewHolderShopItemBinding, drawableResource: Int){

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(drawableResource)
            .placeholder(R.drawable.ic_rating_one_stars)
            .into(binding.saloonRatingImage)

    }

    inner class ShopRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val binding: ViewHolderShopItemBinding = ViewHolderShopItemBinding.bind(itemView)

    }
}