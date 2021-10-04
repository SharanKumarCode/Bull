package com.bullSaloon.bull.fragments.saloon

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.HandlerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.adapters.SaloonItemViewPagerAdapter
import com.bullSaloon.bull.databinding.FragmentSaloonItemBinding
import com.bullSaloon.bull.fragments.CameraFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.RangeSlider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import java.time.Month
import kotlin.collections.HashMap


class SaloonItemFragment : Fragment() {

    private var _binding: FragmentSaloonItemBinding? = null
    private val binding get() = _binding!!
    private lateinit var saloonID: String
    private lateinit var saloonName: String
    private lateinit var saloonArea: String
    private lateinit var dataViewModel: MainActivityViewModel
    private lateinit var dataUserViewModel: UserDataViewModel

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var storageRef = SingletonInstances.getStorageReference()
    private lateinit var basicUserData: UserDataClass
    private var priceList = mutableListOf<String>()
    private var rating = 1

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

        dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataUserViewModel = ViewModelProvider(requireActivity()).get(UserDataViewModel::class.java)
        db = SingletonInstances.getFireStoreInstance()
        auth = SingletonInstances.getAuthInstance()

        var contact = ""
        var shopAddress = ""

        val saloonNavHostFragment = this.parentFragment?.childFragmentManager?.findFragmentById(R.id.saloonFragmentContainer)
        val navController = saloonNavHostFragment?.findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                navController?.navigate(R.id.action_shopItemFragment_to_shopListFragment)
            }
        })

        generatePricingFireStoreData()

//        Getting data from ViewModel - UserDataViewModel

        dataUserViewModel.getUserBasicData().observe(viewLifecycleOwner, { data ->
            basicUserData = data
        })

//        Getting data from ViewModel - MainActivityViewModel

        dataViewModel.getShopData().observe(viewLifecycleOwner, { data ->

//            Set Image, shop name, shop address and contact number
            binding.saloonItemNameTextView.text = data.saloonName
            binding.saloonItemAddressTextView.text = data.saloonAddress
            contact = data.contact.toString()
            shopAddress = "${data.saloonName} ${data.saloonAddress}"
            saloonArea = data.areaName!!

//            Set Open Status
            if (data.openStatus == true){
                binding.openStatusTextView.text = requireActivity().resources.getString(R.string.placeHolderOpenStatus)
                binding.openStatusTextView.setTextColor(context?.getColor(R.color.openStatusColor)!!)
            } else {
                binding.openStatusTextView.text = requireActivity().resources.getString(R.string.placeHolderClosedStatus)
                binding.openStatusTextView.setTextColor(context?.getColor(R.color.closeStatusColor)!!)
            }

            saloonID = data.saloonID.toString()
            saloonName = data.saloonName.toString()

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

        binding.appointmentSaloonItemButton.setOnClickListener {
            startAppointmentDialog()
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

        binding.saloonRefresher.setOnRefreshListener {

            if (binding.ViewPagerSaloonItem.currentItem == 1){
                Log.i(TAG, "Refreshed : ${binding.ViewPagerSaloonItem.currentItem}")
            }else if (binding.ViewPagerSaloonItem.currentItem == 2){
                dataViewModel.setSaloonRefreshState(MainActivityViewModel.SaloonRefreshData(saloonPhotosState = false,saloonReview = true))
                setRatingAndReview()
                viewPagerAdapter.notifyItemChanged(binding.ViewPagerSaloonItem.currentItem)
            }

            HandlerCompat.postDelayed(Handler(Looper.getMainLooper()),
                {
                    binding.saloonRefresher.isRefreshing = false
                    dataViewModel.setSaloonRefreshState(MainActivityViewModel.SaloonRefreshData(saloonPhotosState = false,saloonReview = false))
                }
                ,null,2000)
        }

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

        val imageRef = storageRef.storage.getReferenceFromUrl(profilePicRef)

        GlideApp.with(binding.root.context)
            .asBitmap()
            .load(imageRef)
            .centerCrop()
            .placeholder(R.drawable.ic_bull)
            .into(binding.saloonItemDisplayImage)
    }

    private fun setRatingAndReview(){

        db.collection("Saloons")
            .document(saloonID)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()){

                    val averageRating = document.getDouble("average_rating.current_average_rating")?.roundToInt()
                    val reviewCount = document.getLong("average_rating.number_of_reviews")?.toInt()

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
                1-> setRatingImageDialog(dialog, R.drawable.avd_rating_one_stars, 1)
                2-> setRatingImageDialog(dialog, R.drawable.avd_rating_two_stars, 2)
                3-> setRatingImageDialog(dialog, R.drawable.avd_rating_three_stars, 3)
                4-> setRatingImageDialog(dialog, R.drawable.avd_rating_four_stars, 4)
                5-> setRatingImageDialog(dialog, R.drawable.avd_rating_five_stars, 5)
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

    private fun startAppointmentDialog(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_appointment)

        var serviceInputValidityFlag = false
        var dateInputValidityFlag = false
        var timeInputValidityFlag = false

        val selectedStyle = dialog.findViewById<TextInputLayout>(R.id.selectServiceTextInputLayoutAppointmentDialog)
        val selectedDate = dialog.findViewById<TextInputLayout>(R.id.selectDateTextInputLayoutAppointmentDialog)
        val selectedTime = dialog.findViewById<TextInputLayout>(R.id.selectTimeTextInputLayoutAppointmentDialog)
        val okButton = dialog.findViewById<MaterialButton>(R.id.okButtonAppointmentDialog)
        val closeButton = dialog.findViewById<AppCompatImageButton>(R.id.closeButtonAppointmentDialog)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, priceList)
        (selectedStyle.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        var selectedServiceText = ""
        val dateMap = hashMapOf<String, Int>()
        val timeMap = hashMapOf<String, String>()

        okButton.setOnClickListener {
            if (serviceInputValidityFlag && dateInputValidityFlag && timeInputValidityFlag){
                uploadAppointmentToFireStore(selectedServiceText, dateMap, timeMap)
                dialog.dismiss()
            } else {
                Toast.makeText(requireActivity(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        (selectedStyle.editText as? AutoCompleteTextView)?.onItemClickListener = object : AdapterView.OnItemClickListener {

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                serviceInputValidityFlag = true
                selectedServiceText = p0?.adapter?.getItem(p2).toString()
            }

        }

        val selectedDateEditText = selectedDate.findViewById<TextInputEditText>(R.id.selectDateTextInputEditAppointmentDialog)

        selectedDateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext())
            datePickerDialog.setOnDateSetListener { _, i, i2, i3 ->
                val month = Month.of(i2+1).toString().lowercase().replaceFirstChar { c -> c.uppercase() }
                selectedDateEditText.setText(requireContext().resources.getString(R.string.placeHolderDate, i3.toString(), month, i.toString()))
                dateInputValidityFlag = true

                dateMap["Day"] = i3
                dateMap["Month"] = i2+1
                dateMap["Year"] = i
            }

            val c = Calendar.getInstance()
            datePickerDialog.datePicker.minDate = c.timeInMillis
            c.add(Calendar.DATE, 7)
            datePickerDialog.datePicker.maxDate = c.timeInMillis
            datePickerDialog.show()
        }

        val selectedTimeEditText = selectedTime.findViewById<TextInputEditText>(R.id.selectTimeTextInputEditAppointmentDialog)

        selectedTimeEditText.setOnClickListener {
            val timePickerListener = object : TimePickerDialog.OnTimeSetListener{
                override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {

                    var hour = ""
                    val minute = p2.toString().padStart(2, '0')
                    var amPm = ""

                    when (p1){
                        0 -> {
                            hour = "12"
                            amPm = "AM"
                        }
                        12 -> {
                            hour = "12"
                            amPm = "PM"
                        }
                        in 1..12 -> {
                            hour = p1.toString().padStart(2, '0')
                            amPm = "AM"
                        }
                        in 12..24 -> {
                            hour = (p1-12).toString().padStart(2, '0')
                            amPm = "AM"
                        }
                    }

                    selectedTimeEditText.setText(requireContext().resources.getString(R.string.placeHolderTime, hour, minute, amPm))

                    timeInputValidityFlag = true

                    timeMap["Hour"] = hour
                    timeMap["Minute"] = minute
                    timeMap["AmPm"] = amPm
                }
            }

            val timePickerDialog = TimePickerDialog(requireContext(), timePickerListener,8,0,false)
            timePickerDialog.show()
        }

        val width = resources.displayMetrics.widthPixels

        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()

    }

    private fun generatePricingFireStoreData(){

        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataViewModel.getShopData().observe(viewLifecycleOwner, { data ->
            saloonID = data.saloonID.toString()

            db.collection("Saloons")
                .document(saloonID)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val pricingList = it.get("pricing_list") as HashMap<String, Number>

                        pricingList.forEach { (key, _) ->
                            priceList.add(key)
                        }
                        priceList.add("Other")
                    }
                }
                .addOnFailureListener {e->
                    Log.i("TAG", "Error in fetching pricing data : ${e.message}")
                }
        })
    }

    private fun uploadRatingToFireStore(review: String = ""){

        val timeStamp = SimpleDateFormat(CameraFragment.FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val ratingMap = hashMapOf<String, Any>(
            "rating_id" to UUID.randomUUID().toString(),
            "timestamp" to timeStamp,
            "user_id" to auth.currentUser?.uid.toString(),
            "rating_value" to rating,
            "review" to URLEncoder.encode(review, "UTF-8"))

        db.collection("Saloons")
            .document(saloonID)
            .collection("rating")
            .document(ratingMap["rating_id"].toString())
            .set(ratingMap)
            .addOnSuccessListener {
                Log.i(TAG, "review is added")
                updateAverageRatingAndReviewCount(review)
            }
            .addOnFailureListener {e->
                Log.i(TAG, "error in adding review : ${e.message}")
            }
    }

    private fun updateAverageRatingAndReviewCount(review: String){
        db.collection("Saloons")
            .document(saloonID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val oldAverageRating = it.getDouble("average_rating.current_average_rating")!!
                    val ratingCount = it.getDouble("average_rating.number_of_ratings")!!
                    val oldReviewCount = it.getLong("average_rating.number_of_reviews")?.toInt()!!

                    val newReviewCount = if (review == "") oldReviewCount.toLong() else (oldReviewCount + 1).toLong()
                    val newAverageRating = ((oldAverageRating * ratingCount) + rating.toDouble()) / (ratingCount + 1 )

                    val updateRatingReviewCountMap = mutableMapOf(
                        "current_average_rating" to newAverageRating,
                        "number_of_ratings" to (ratingCount + 1),
                        "number_of_reviews" to newReviewCount)

                    db.collection("Saloons")
                        .document(saloonID)
                        .update("average_rating", updateRatingReviewCountMap)
                        .addOnSuccessListener {

                            Toast.makeText(
                                requireContext(),
                                "Your Rating and Review is Updated. \n\n Refresh Screen to make it visible",
                                Toast.LENGTH_SHORT)
                                .show()

                            setRatingAndReview()
                            Log.i(TAG, "average rating is updated")
                        }
                        .addOnFailureListener {e->
                            Log.i(TAG, "error in updating average rating : ${e.message}")
                        }
                }
            }
            .addOnFailureListener {e->
                Log.i(TAG, "error in getting average Rating : ${e.message}")
            }
    }

    private fun uploadAppointmentToFireStore(service: String, date: HashMap<String, Int>, time: HashMap<String, String>){

        val loadingDialogBuilder = AlertDialog.Builder(requireContext())
        loadingDialogBuilder.setView(R.layout.dialog_loading)
        loadingDialogBuilder.setCancelable(false)

        val loadingDialog = loadingDialogBuilder.create()

        Log.i(TAG, "loading text : ${loadingDialog.findViewById<TextView>(R.id.loadingTextLoadingDialog)?.text}")
        loadingDialog.show()

        val appointmentID = UUID.randomUUID().toString()
        val appointmentMap = hashMapOf("appointment_id" to appointmentID,
                                                        "user_id" to basicUserData.user_id,
                                                        "user_name" to basicUserData.user_name,
                                                        "saloon_id" to saloonID,
                                                        "saloon_name" to saloonName,
                                                        "area" to saloonArea,
                                                        "service" to service,
                                                        "date" to date,
                                                        "time" to time)

        db.collection("Users")
            .document(basicUserData.user_id)
            .collection("appointments")
            .document(appointmentID)
            .set(appointmentMap)
            .addOnSuccessListener {
                db.collection("Saloons")
                    .document(saloonID)
                    .collection("appointments")
                    .document(appointmentID)
                    .set(appointmentMap)
                    .addOnSuccessListener {
                        Log.i(TAG, "Appointment made")
                        Snackbar.make(requireActivity(),binding.tabInputSaloonItem,"Appointment made Successfully", Snackbar.LENGTH_SHORT).show()
                        loadingDialog.dismiss()
                    }
                    .addOnFailureListener { e->
                        Log.i(TAG, "error in uploading Appointment : ${e.message}")
                        Snackbar.make(requireActivity(),binding.tabInputSaloonItem,"Error in making appointment. \n\n Please try again later", Snackbar.LENGTH_SHORT).show()
                        loadingDialog.dismiss()
                    }
            }
            .addOnFailureListener {e->
                Log.i(TAG, "error in uploading Appointment : ${e.message}")
                Snackbar.make(requireActivity(),binding.tabInputSaloonItem,"Error in making appointment. \n\n Please try again later", Snackbar.LENGTH_SHORT).show()
                loadingDialog.dismiss()
            }
    }

    private fun setRatingImageDialog(_dialog:Dialog, d: Int, _rating: Int){
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

    companion object {
        private const val TAG = "TAGSaloonItemFragment"
    }

}