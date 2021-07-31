package com.bullSaloon.bull.fragments.saloon


import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
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
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.ShopDataPreviewClass
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SaloonListFragment : Fragment() {

    private var _binding: FragmentSaloonListBinding? = null
    private val binding get() = _binding!!

    private lateinit var animate: AnimatedVectorDrawable
    private lateinit var dataViewModel: MainActivityViewModel
    private lateinit var recyclerState: Parcelable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_left_to_right)
        exitTransition = inflaterTrans.inflateTransition(R.transition.fade)

        Log.i("TAG", "onCreate")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.i("TAG", "onCreateView")
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

        Log.i("TAG", "onViewCreated")

        generateDataFirestore()
    }

    override fun onPause() {
        super.onPause()

        Log.i("TAG", "onPause")
        recyclerState = binding.recyclerView.layoutManager?.onSaveInstanceState()!!
        SingletonUserData.updateScrollState("SaloonListRecycler",recyclerState)
    }

    private fun generateDataFirestore(){

        Log.i("TAG", "generateDataFirestore")

        val db = Firebase.firestore
        val shopLists = mutableListOf<ShopDataPreviewClass>()
        dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        db.collection("Saloons")
            .get()
            .addOnSuccessListener{
                for (document in it.documents){

                    val saloonID: String? = document.getString("saloon_id")
                    val saloonName: String? = document.getString("saloon_name")
                    val areaName: String? = document.getString("area")
                    val openStatus: Boolean? = document.getBoolean("open_status")
                    val contact: String? = document.getString("contact")
                    val saloonAddress: String? = document.getString("address")
                    val haircutPrice: Number? = document.getLong("cutting_shaving_price")

                    val ratings = if (document.get("rating") != null) document.get("rating") as HashMap<String, HashMap<String,String>> else hashMapOf()
                    var averageRating = 5
                    var ratingSum = 0
                    var reviewCount = 0

                    ratings.forEach { (_, value) ->
                        ratingSum += value["rating_value"]?.toInt()!!

                        if (value["review"] != ""){
                            reviewCount += 1
                        }
                    }

                    averageRating = if (ratings.size == 0){
                        1
                    } else {
                        ratingSum / ratings.size
                    }

                    val saloonNameUnderScore = saloonName?.replace("\\s".toRegex(),"_")
                    val imageUrl = "Saloon_Images/$saloonID/${saloonNameUnderScore}_displayPicture.jpg"

                    shopLists.add(ShopDataPreviewClass(saloonID, saloonName,areaName, averageRating, imageUrl ,openStatus, contact, saloonAddress, haircutPrice, reviewCount))

                    animate.stop()
                    animate.clearAnimationCallbacks()
                    binding.loadingIconListFragmentImageView.visibility = View.GONE

                }

                dataViewModel.assignShopData(shopLists)
                if(view != null){
                    dataViewModel.getShopDataList().observe(viewLifecycleOwner, { result ->
                        binding.recyclerView.adapter = SaloonListRecyclerViewAdapter(result, dataViewModel, this)
                        val recyAdapter = binding.recyclerView.adapter
                        recyAdapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

                    })
                }
            }
    }
}