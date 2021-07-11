package com.bullSaloon.bull.fragments

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentShopItemBinding
import com.bullSaloon.bull.viewModel.MainActivityViewModel


class ShopItemFragment : Fragment() {

    private var _binding: FragmentShopItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.shared_image)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShopItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        hiding the search bar
        hideSearchBar()

        var contact: String? = "+917550349075"
        var shopName: String? = "Vaigai Saloon"
        var shopAddress: String? = "$shopName 918, Sathy Rd, Opp G P Hospital, Raju Naidu Layout, Gandhipuram, Tamil Nadu 641012"

        ViewCompat.setTransitionName(binding.shopFragmentImage, "targetShopImage")

//        Getting data from ViewModel - MainActivityViewModel
        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataViewModel.getShopData().observe(viewLifecycleOwner, androidx.lifecycle.Observer { data ->

//                Set Image, shop name, shop address and contact number
                val imageResource = context?.resources?.getIdentifier(data.imageSource, "drawable",context?.packageName)
                binding.shopFragmentImage.setBackgroundResource(imageResource!!)
                binding.shopNameFragmentTextView.text = data.shopName
                binding.addressFragmentTextView.text = data.shopAddress
                contact = data.mobile
                shopAddress = "${data.shopName} ${data.shopAddress}"

//                Set Open Status
                if (data.openStatus == true){
                    binding.openStatusTextView.text = "open"
                    binding.openStatusTextView.setTextColor(context?.getColor(R.color.openStatusColor)!!)
                } else {
                    binding.openStatusTextView.text = "closed"
                    binding.openStatusTextView.setTextColor(context?.getColor(R.color.closeStatusColor)!!)
                }
        })

//        Set Call Button and location Button Click Handler
        binding.callImageView.setOnClickListener{
            callActivity(contact!!)
        }
        binding.googleMapImageView.setOnClickListener{
            locationActivity(shopAddress!!)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        val searchCard = activity?.findViewById<LinearLayout>(R.id.searchBarLinearLayout)
        searchCard?.visibility = View.VISIBLE

    }

    private fun callActivity(contact: String){
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$contact")
        startActivity(callIntent)
    }

    private fun locationActivity(shopaddress:String){
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(shopaddress))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)

    }

    private fun hideSearchBar(){

        val searchBar = activity?.findViewById<LinearLayout>(R.id.searchBarLinearLayout)
        searchBar?.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()

//        Starting rating icon animation
        val d = binding.ratingFragmentImageView.drawable as AnimatedVectorDrawable
        d.start()
    }

}