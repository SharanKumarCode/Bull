package com.bullSaloon.bull.fragments.saloon

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.adapters.SaloonReviewRecyclerAdapter
import com.bullSaloon.bull.databinding.FragmentSaloonReviewBinding
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLDecoder


class SaloonReviewFragment : Fragment() {

    private var _binding: FragmentSaloonReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var saloonID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaloonReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saloonReviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.saloonReviewNoReviewTextLabel.visibility = View.VISIBLE
        binding.saloonReviewRecyclerView.visibility = View.GONE
        getReviewDataFromFireStore()

        binding.saloonReviewAddReviewRatingButton.setOnClickListener {
            val parentFragment = this.parentFragment as SaloonItemFragment
            parentFragment.startRatingDialog()
        }

    }

    private fun getReviewDataFromFireStore(){
        val db = Firebase.firestore

        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataViewModel.getShopData().observe(viewLifecycleOwner, { data ->
            saloonID = data.saloonID.toString()

            db.collection("Saloons")
                .document(saloonID)
                .addSnapshotListener { value, error ->
                    if (error == null){
                        if (value?.exists()!!){
                            val ratings = if (value.get("rating") != null)  value.get("rating") as HashMap<String, HashMap<String,String>> else hashMapOf()
                            val ratingsList = mutableListOf<RatingReviewData>()

                            ratings.forEach{ (_, values) ->
                                val ratingID = values["rating_id"].toString()
                                val rating = values["rating_value"].toString()
                                val timeStamp = values["timestamp"].toString()
                                val userID = values["user_id"].toString()
                                val review = URLDecoder.decode(values["review"].toString(), "UTF-8")

                                val ratingData = RatingReviewData(ratingID, rating, timeStamp, userID, review)
                                ratingsList.add(ratingData)

                                Log.i("TAG","review data : $ratings")
                            }

                            ratingsList.sortBy {
                                it.timeStamp
                            }

                            ratingsList.reverse()

                            if (ratingsList.size == 0){
                                binding.saloonReviewNoReviewTextLabel.visibility = View.VISIBLE
                                binding.saloonReviewRecyclerView.visibility = View.GONE
                            } else {
                                binding.saloonReviewNoReviewTextLabel.visibility = View.GONE
                                binding.saloonReviewRecyclerView.visibility = View.VISIBLE
                            }

                            binding.saloonReviewRecyclerView.adapter =  SaloonReviewRecyclerAdapter(ratingsList)
                        }
                    } else {
                        Log.i("TAG", "Error in fetching saloon review data : $error")
                    }
                }
        })


    }

    data class RatingReviewData(
        val ratingID: String,
        val rating: String,
        val timeStamp: String,
        val userID: String,
        val review: String = ""
    )
}