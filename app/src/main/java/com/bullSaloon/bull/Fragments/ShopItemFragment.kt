package com.bullSaloon.bull.Fragments

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bullSaloon.bull.R
import com.bullSaloon.bull.ViewModel.MainActivityViewModel


class ShopItemFragment : Fragment() {

    private lateinit var callButton: ImageView
    private lateinit var locationButton: ImageView
    private lateinit var shopFragmentImage: ImageView
    private lateinit var ratingFragmentImage: ImageView
    private lateinit var shopNameFragmentTextView: TextView
    private lateinit var openStatusTextView: TextView
    private lateinit var addressFragmentTextView: TextView

    private var originalHeight: Int? = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_shop_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resizeFragment()

        var contact: String? = "+917550349075"
        var shopName: String? = "Vaigai Saloon"
        var shopAddress: String? = "$shopName 918, Sathy Rd, Opp G P Hospital, Raju Naidu Layout, Gandhipuram, Tamil Nadu 641012"

        originalHeight= activity?.findViewById<FrameLayout>(R.id.shopFragmentContainer)?.height

        callButton = view.findViewById(R.id.callImageView)
        locationButton = view.findViewById(R.id.googleMapImageView)
        shopFragmentImage = view.findViewById(R.id.shopFragmentImage)
        shopNameFragmentTextView = view.findViewById(R.id.shopNameFragmentTextView)
        openStatusTextView = view.findViewById(R.id.openStatus_TextView)
        addressFragmentTextView = view.findViewById(R.id.addressFragmentTextView)
        ratingFragmentImage = view.findViewById(R.id.ratingFragmentImageView)


        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataViewModel.getShopData().observe(viewLifecycleOwner, androidx.lifecycle.Observer { data ->

                val imageResource = context?.resources?.getIdentifier(data.imageSource, "drawable",context?.packageName)
                shopFragmentImage.setBackgroundResource(imageResource!!)
                shopNameFragmentTextView.text = data.shopName
                addressFragmentTextView.text = data.shopAddress
                contact = data.mobile
                shopAddress = "${data.shopName} ${data.shopAddress}"


                if (data.openStatus == true){
                    openStatusTextView.text = "open"
                    openStatusTextView.setTextColor(context?.getColor(R.color.openStatusColor)!!)
                } else {
                    openStatusTextView.text = "closed"
                    openStatusTextView.setTextColor(context?.getColor(R.color.closeStatusColor)!!)
                }
        })

        callButton.setOnClickListener{
            callActivity(contact!!)
        }
        locationButton.setOnClickListener{
            locationActivity(shopAddress!!)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        val containerV = activity?.findViewById<FrameLayout>(R.id.shopFragmentContainer)
        val searchCard = activity?.findViewById<CardView>(R.id.cardViewSearch)

        containerV?.layoutParams?.height = originalHeight
        searchCard?.visibility = View.VISIBLE

    }

    private fun callActivity(contact: String){
        val callIntent: Intent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$contact")
        startActivity(callIntent)
    }

    private fun locationActivity(shopaddress:String){
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(shopaddress))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)

    }

    private fun resizeFragment(){
        val containerVar = activity?.findViewById<FrameLayout>(R.id.shopFragmentContainer)
        val searchCard = activity?.findViewById<CardView>(R.id.cardViewSearch)

        val height = containerVar?.rootView?.height?.plus(-300)

        containerVar?.layoutParams?.height = height!!
        searchCard?.visibility = View.GONE

    }

    override fun onStart() {
        super.onStart()
        val d = ratingFragmentImage.drawable as AnimatedVectorDrawable
        d.start()
    }

}