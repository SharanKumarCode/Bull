package com.bullSaloon.bull.fragments.saloon

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.adapters.SaloonPricingRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentSaloonPricingBinding
import com.bullSaloon.bull.viewModel.MainActivityViewModel

class SaloonPricingFragment : Fragment() {

    private var _binding: FragmentSaloonPricingBinding? = null
    private val binding get() = _binding!!
    private lateinit var saloonID: String

    private val db = SingletonInstances.getFireStoreInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaloonPricingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saloonPricingRecyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL )
        binding.saloonPricingRecyclerView.layoutAnimation

        generatePricingFireStoreData()
    }

    private fun generatePricingFireStoreData(){

        val priceList = mutableListOf<HashMap<String, Number>>()

        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataViewModel.getShopData().observe(viewLifecycleOwner, { data ->
            saloonID = data.saloonID.toString()

            db.collection("Saloons")
                .document(saloonID)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val pricingList = it.get("pricing_list") as HashMap<String, Number>

                        pricingList.forEach { (key, value) ->
                            priceList.add(hashMapOf(key to value))
                        }
                    }
                    binding.saloonPricingRecyclerView.adapter = SaloonPricingRecyclerViewAdapter(priceList)
                }
                .addOnFailureListener {e->
                    Log.i("TAG", "Error in fetching pricing data : ${e.message}")
                }
        })
    }
}