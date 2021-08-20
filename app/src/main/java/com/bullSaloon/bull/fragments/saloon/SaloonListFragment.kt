package com.bullSaloon.bull.fragments.saloon


import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Parcelable
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bullSaloon.bull.R
import com.bullSaloon.bull.adapters.SaloonListRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentSaloonListBinding
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.SaloonDataClass
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.roundToInt

class SaloonListFragment : Fragment() {

    private var _binding: FragmentSaloonListBinding? = null
    private val binding get() = _binding!!

    private lateinit var animate: AnimatedVectorDrawable
    private lateinit var dataViewModel: MainActivityViewModel
    private lateinit var recyclerState: Parcelable
    private val saloonLists = mutableListOf<SaloonDataClass>()
    private var lastVisible: DocumentSnapshot? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_left_to_right)
        exitTransition = inflaterTrans.inflateTransition(R.transition.fade)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSaloonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

        //restore scroll state
        val scrollState = SingletonUserData.getScrollState("SaloonListRecycler")

        if (scrollState != null){
            binding.recyclerView.layoutManager?.onRestoreInstanceState(scrollState)
        }

        //starting loading icon
        binding.loadingIconListFragmentImageView.visibility = View.VISIBLE
        animate = binding.loadingIconListFragmentImageView.drawable as AnimatedVectorDrawable

        AnimatedVectorDrawableCompat.registerAnimationCallback(animate, object: Animatable2Compat.AnimationCallback(){
            override fun onAnimationEnd(drawable: Drawable?) {
                super.onAnimationEnd(drawable)

                animate.start()
            }
        })

        animate.start()

        generateDataFirestore()

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!binding.recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    Log.i("TAGRecycle", "Scrolled to Bottom")
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()

        recyclerState = binding.recyclerView.layoutManager?.onSaveInstanceState()!!
        SingletonUserData.updateScrollState("SaloonListRecycler",recyclerState)
    }

    private fun generateDataFirestore(){

        val db = Firebase.firestore
        dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        val query: Task<QuerySnapshot> = if (lastVisible == null){
            db.collection("Saloons")
                .get()
        } else {
            db.collection("Saloons")
                .orderBy("saloon_name")
                .startAfter(lastVisible)
                .get()
        }

            query.addOnSuccessListener{
                for (document in it.documents){

                    val saloonID: String? = document.getString("saloon_id")
                    val saloonName: String? = document.getString("saloon_name")
                    val areaName: String? = document.getString("area")
                    val openStatus: Boolean? = document.getBoolean("open_status")
                    val contact: String? = document.getString("contact")
                    val saloonAddress: String? = document.getString("address")
                    val haircutPrice: Number? = document.getLong("cutting_shaving_price")
                    val locationData: GeoPoint = document.getGeoPoint("location_data")!!

                    lastVisible = it.documents[it.size() - 1]

                    val averageRating = document.getDouble("average_rating.current_average_rating")?.roundToInt()
                    val reviewCount = document.getLong("average_rating.number_of_reviews")?.toInt()

                    val saloonNameUnderScore = saloonName?.replace("\\s".toRegex(),"_")
                    val imageUrl = "Saloon_Images/$saloonID/${saloonNameUnderScore}_displayPicture.jpg"
                    val distance = 0F

                    val saloonData = SaloonDataClass(
                        saloonID,
                        saloonName,
                        areaName,
                        averageRating,
                        imageUrl,
                        openStatus,
                        contact,
                        saloonAddress,
                        haircutPrice,
                        reviewCount,
                        locationData,
                        distance)

                    saloonLists.add(saloonData)

                    animate.stop()
                    animate.clearAnimationCallbacks()
                    binding.loadingIconListFragmentImageView.visibility = View.GONE

                }

                dataViewModel.getUserLocationData().observe(viewLifecycleOwner, {data ->
                    for (saloon in saloonLists){
                        if (data != null && data.latitude != 0.0 && data.longitude != 0.0){
                            val userData = Location(LocationManager.GPS_PROVIDER)
                            userData.latitude = data.latitude
                            userData.longitude = data.longitude

                            val saloonData = Location(LocationManager.GPS_PROVIDER)
                            saloonData.latitude = saloon.locationData?.latitude!!
                            saloonData.longitude = saloon.locationData.longitude

                            saloon.distance = userData.distanceTo(saloonData)
                        } else {
                            saloon.distance = null
                        }
                    }

                    saloonLists.sortBy { sortData ->
                        sortData.distance
                    }

                    dataViewModel.assignShopData(saloonLists)
                    if(view != null){
                        dataViewModel.getShopDataList().observe(viewLifecycleOwner, { result ->
                            binding.recyclerView.adapter = SaloonListRecyclerViewAdapter(result, dataViewModel, this)
                            val recyclerAdapter = binding.recyclerView.adapter
                            recyclerAdapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        })
                    }
                })
            }
                .addOnFailureListener {e->
                    Log.i(TAG, "error in fetching saloon list : ${e.message}")
                }
    }

    companion object {
        private const val TAG = "TAGSaloonListFragment"
    }
}