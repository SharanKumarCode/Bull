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
import com.bullSaloon.bull.genericClasses.SingletonUserData
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

        generateDataFirestore()
    }

    override fun onPause() {
        super.onPause()

        val recyclerState = binding.recyclerView.layoutManager?.onSaveInstanceState()

        recyclerBundle?.putParcelable("recyclerState", recyclerState)
    }

    override fun onResume() {
        super.onResume()

        if(view != null){
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

        val db = Firebase.firestore
        val shopLists = mutableListOf<ShopDataPreviewClass>()
        dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        db.collection("Saloons")
            .get()
            .addOnCompleteListener{
                for (document in it.result!!){

                    val saloonID: String? = document.getString("saloon_id")
                    val saloonName: String? = document.getString("saloon_name")
                    val areaName: String? = document.getString("area")
                    val rating: Long? = document.getLong("rating")
                    val openStatus: Boolean? = document.getBoolean("open_status")
                    val contact: String? = document.getString("contact")
                    val saloonAddress: String? = document.getString("address")
                    val haircutPrice: Number? = document.getLong("cutting_shaving_price")

                    val saloonNameUnderScore = saloonName?.replace("\\s".toRegex(),"_")
                    val imageUrl = "Saloon_Images/$saloonID/${saloonNameUnderScore}_displayPicture.jpg"

                    shopLists.add(ShopDataPreviewClass(saloonID, saloonName,areaName, rating,imageUrl ,openStatus, contact, saloonAddress, haircutPrice))

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