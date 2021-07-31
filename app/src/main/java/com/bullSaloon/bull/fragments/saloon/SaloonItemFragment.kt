package com.bullSaloon.bull.fragments.saloon

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bullSaloon.bull.R
import com.bullSaloon.bull.adapters.SaloonItemViewPagerAdapter
import com.bullSaloon.bull.databinding.FragmentSaloonItemBinding
import com.bullSaloon.bull.fragments.CameraFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.google.android.material.slider.RangeSlider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*


class SaloonItemFragment : Fragment() {

    private var _binding: FragmentSaloonItemBinding? = null
    private val binding get() = _binding!!
    private lateinit var saloonID: String
    private var rating = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.shared_image)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaloonItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var contact = ""
        var shopAddress = ""

        val saloonNavHostFragment = this.parentFragment?.childFragmentManager?.findFragmentById(R.id.saloonFragmentContainer)
        val navController = saloonNavHostFragment?.findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navController?.navigate(R.id.action_shopItemFragment_to_shopListFragment)
            }
        })

//        Getting data from ViewModel - MainActivityViewModel
        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataViewModel.getShopData().observe(viewLifecycleOwner, { data ->

//            Set Image, shop name, shop address and contact number
            binding.saloonItemNameTextView.text = data.saloonName
            binding.saloonItemAddressTextView.text = data.saloonAddress
            contact = data.contact.toString()
            shopAddress = "${data.saloonName} ${data.saloonAddress}"

//            Set Open Status
            if (data.openStatus == true){
                binding.openStatusTextView.text = requireActivity().resources.getString(R.string.placeHolderOpenStatus)
                binding.openStatusTextView.setTextColor(context?.getColor(R.color.openStatusColor)!!)
            } else {
                binding.openStatusTextView.text = requireActivity().resources.getString(R.string.placeHolderClosedStatus)
                binding.openStatusTextView.setTextColor(context?.getColor(R.color.closeStatusColor)!!)
            }

            saloonID = data.saloonID.toString()

//            set saloon display picture
            setSaloonDisplayPic(data.imageSource!!)

//            set saloon rating and review count
            setRatingAndReview()
        })

        binding.ratingSaloonItemImageView.setOnClickListener {
            binding.ViewPagerSaloonItem.setCurrentItem(2, true)
            startRatingDialog()
        }

//        Set Call Button and location Button Click Handler
        binding.callSaloonItemImageView.setOnClickListener{
            callActivity(contact)
        }
        binding.googleMapImageView.setOnClickListener{
            locationActivity(shopAddress)
        }

        binding.saloonItemReviewCountImage.setOnClickListener {
            Log.i("TAG","Clicked review")
        }

        val viewPagerAdapter = SaloonItemViewPagerAdapter(this)
        binding.ViewPagerSaloonItem.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabInputSaloonItem, binding.ViewPagerSaloonItem){tab, position->
            when(position){
                0->{
                    tab.text = "Pricing"
                    tab.setIcon(R.drawable.ic_rupee_icon)
                }
                1->{
                    tab.text = "Photos"
                    tab.setIcon(R.drawable.ic_baseline_photo_library_24)
                }
                2->{
                    tab.text = "Reviews"
                    tab.setIcon(R.drawable.ic_baseline_rate_review_24)
                }
            }
        }.attach()

        binding.tabInputSaloonItem.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        binding.ViewPagerSaloonItem.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                viewPagerAdapter.notifyItemChanged(position)

//                viewPagerAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setSaloonDisplayPic(profilePicRef: String){

        val imageRef = Firebase.storage.reference.child(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .centerCrop()
            .placeholder(R.drawable.ic_bull)
            .into(binding.saloonItemDisplayImage)
    }

    private fun setRatingAndReview(){
        val db = Firebase.firestore

        db.collection("Saloons")
            .document(saloonID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()){

                    val ratings =
                        if (document.get("rating") != null) document.get("rating") as HashMap<String, HashMap<String, String>> else hashMapOf()
                    var ratingSum = 0
                    var reviewCount = 0

                    ratings.forEach { (_, value) ->
                        ratingSum += value["rating_value"]?.toInt()!!

                        if (value["review"] != "") {
                            reviewCount += 1
                        }
                    }

                    val averageRating = if (ratings.size == 0) {
                        1
                    } else {
                        ratingSum / ratings.size
                    }

                    when(averageRating){
                        1 -> setRatingPic(R.drawable.ic_rating_one_stars)
                        2 -> setRatingPic(R.drawable.ic_rating_two_stars)
                        3 -> setRatingPic(R.drawable.ic_rating_three_stars)
                        4 -> setRatingPic(R.drawable.ic_rating_four_stars)
                        5 -> setRatingPic(R.drawable.ic_rating_five_stars)
                        else -> setRatingPic(R.drawable.ic_rating_one_stars)
                    }

                    binding.saloonItemReviewCountText.text = reviewCount.toString()
                }
            }
    }

    private fun setRatingPic(drawableResource: Int){

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(drawableResource)
            .placeholder(R.drawable.ic_rating_one_stars)
            .into(binding.ratingSaloonItemImageView)

    }

    fun startRatingDialog(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_saloon_rating)
        val rateButton = dialog.findViewById<Button>(R.id.saloonItemDialogRateButton)
        val cancelButton = dialog.findViewById<ImageView>(R.id.saloonItemDialogCloseButton)
        val slider = dialog.findViewById<RangeSlider>(R.id.saloonItemDialogRatingSlider)
        val ratingImage = dialog.findViewById<ImageView>(R.id.saloonItemAnimatedRating)

        ratingImage.setImageResource(R.drawable.avd_rating_one_stars)

        slider.addOnChangeListener { _, value, _ ->
            when(value.toInt()){
                1-> setRatingImageDialog(dialog, R.drawable.avd_rating_one_stars, "1")
                2-> setRatingImageDialog(dialog, R.drawable.avd_rating_two_stars, "2")
                3-> setRatingImageDialog(dialog, R.drawable.avd_rating_three_stars, "3")
                4-> setRatingImageDialog(dialog, R.drawable.avd_rating_four_stars, "4")
                5-> setRatingImageDialog(dialog, R.drawable.avd_rating_five_stars, "5")
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        rateButton.setOnClickListener {
            dialog.dismiss()
            startReviewDialog()
        }

        dialog.show()
    }

    private fun startReviewDialog(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_saloon_review)
        val okButton = dialog.findViewById<Button>(R.id.saloonItemDialogOkButton)
        val skipButton = dialog.findViewById<Button>(R.id.saloonItemDialogCancelButton)
        val reviewEditText = dialog.findViewById<EditText>(R.id.saloonItemReviewTextField)

        reviewEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                when {
                    reviewEditText.text?.length!! < 5 -> {
                        reviewEditText.error = "minimum 4 characters required"
                    }
                    reviewEditText.text?.length!! > 100 -> {
                        reviewEditText.error = "restrict review to 100 characters"
                    }
                    else -> {
                        reviewEditText.error = null
                    }
                }
            }
        })

        okButton.setOnClickListener {
            val reviewLength = reviewEditText.text.length
            if (reviewLength in 4..100){
                dialog.dismiss()
                uploadRatingToFireStore(reviewEditText.text.toString())
            }

        }

        skipButton.setOnClickListener {
            dialog.dismiss()
            uploadRatingToFireStore()
        }

        dialog.show()
    }

    private fun uploadRatingToFireStore(review: String = ""){

        val db = Firebase.firestore
        val auth = Firebase.auth

        val timeStamp = SimpleDateFormat(CameraFragment.FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val ratingMap = hashMapOf<String, String>(
            "rating_id" to UUID.randomUUID().toString(),
            "timestamp" to timeStamp,
            "user_id" to auth.currentUser?.uid.toString(),
            "rating_value" to rating,
            "review" to URLEncoder.encode(review, "UTF-8"))

        db.collection("Saloons")
            .document(saloonID)
            .update("rating.${ratingMap["rating_id"]}", ratingMap)
            .addOnSuccessListener {
                Log.i("TAG", "review is added")
                setRatingAndReview()
            }
            .addOnFailureListener {e->
                Log.i("TAG", "error in adding review : ${e.message}")
            }
    }

    private fun setRatingImageDialog(_dialog:Dialog, d: Int, _rating: String){
        rating = _rating
        val ratingImage = _dialog.findViewById<ImageView>(R.id.saloonItemAnimatedRating)
        ratingImage.setImageResource(d)
        val anim = ratingImage.drawable as AnimatedVectorDrawable
        anim.start()
    }

    private fun callActivity(contact: String){
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$contact")
        startActivity(callIntent)
    }

    private fun locationActivity(shopAddress:String){
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(shopAddress))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)

    }

}