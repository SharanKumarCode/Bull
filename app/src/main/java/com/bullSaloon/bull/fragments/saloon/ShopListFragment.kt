package com.bullSaloon.bull.fragments.saloon


import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bullSaloon.bull.R
import com.bullSaloon.bull.adapters.ShopRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentShopListBinding
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.ShopDataPreviewClass
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ShopListFragment : Fragment() {

    private var _binding: FragmentShopListBinding? = null
    private val binding get() = _binding!!

    private lateinit var animate: AnimatedVectorDrawable
    private lateinit var dataViewModel: MainActivityViewModel
    private var recyclerBundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        exitTransition = inflaterTrans.inflateTransition(R.transition.fade)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        postponeEnterTransition()

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

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

        //Get shop list data from firestore
        Log.i("TAGLifecycle","data being fetched: onViewCreated")
        generateDataFirestore()
    }

    override fun onPause() {
        super.onPause()

        val recyclerState = binding.recyclerView.layoutManager?.onSaveInstanceState()

        recyclerBundle?.putParcelable("recyclerState", recyclerState)

        Log.i("TAGLifecycle","Shop List Recycler View is Paused : ${recyclerState?.describeContents()}")
    }

    override fun onResume() {
        super.onResume()

        Log.i("TAGLifecycle","Shop List Recycler View is checked")

        if(view != null){
            Log.i("TAGLifecycle","Shop List Recycler View is resumed")
            dataViewModel.getShopDataList().observe(viewLifecycleOwner, { result ->
                binding.recyclerView.adapter = ShopRecyclerViewAdapter(result, dataViewModel,this)
                binding.recyclerView.layoutManager?.onRestoreInstanceState(recyclerBundle?.getParcelable("recyclerState"))

                (view?.parent as ViewGroup).doOnPreDraw {
                    startPostponedEnterTransition()
                }
            })

        }
    }

    private fun generateDataFirestore(){

        Log.i("TAGLifecycle","Firestore data is being fetched")

        val db = Firebase.firestore
        val shopLists = mutableListOf<ShopDataPreviewClass>()
        dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        db.collection("shops_data")
            .get()
            .addOnCompleteListener{
                for (document in it.result!!){

                    val shopID: Number? = document.getLong("shop_id")
                    val shopName: String? = document.getString("Shop_Name")
                    val areaName: String? = document.getString("area")
                    val rating: Long? = document.getLong("Rating")
                    val imageSource:String? = document.getString("Image")
                    val gender: String? = document.getString("Gender")
                    val openStatus: Boolean? = document.getBoolean("Open")
                    val contact: String? = document.getString("Contact")
                    val shopAddress: String? = document.getString("Address")

                    shopLists.add(ShopDataPreviewClass(shopID, shopName,areaName, rating,imageSource,gender,openStatus, contact, shopAddress))

                    animate.stop()
                    animate.clearAnimationCallbacks()
                    binding.loadingIconListFragmentImageView.visibility = View.GONE

                }

                dataViewModel.assignShopData(shopLists)
                if(view != null){
                    dataViewModel.getShopDataList().observe(viewLifecycleOwner, { result ->
                        binding.recyclerView.adapter = ShopRecyclerViewAdapter(result, dataViewModel, this)

                        (view?.parent as ViewGroup).doOnPreDraw {
                            startPostponedEnterTransition()
                        }
                    })
                }
            }
    }
}