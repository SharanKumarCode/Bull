package com.bullSaloon.bull.fragments.saloon

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.adapters.SaloonReviewRecyclerAdapter
import com.bullSaloon.bull.databinding.FragmentSaloonReviewBinding
import com.bullSaloon.bull.viewModel.MainActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLDecoder


class SaloonReviewFragment : Fragment() {

    private var _binding: FragmentSaloonReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var saloonID: String
    private lateinit var dataViewModel: MainActivityViewModel

    val db = SingletonInstances.getFireStoreInstance()

    private val ratingsList = mutableListOf<RatingReviewData>()
    private var lastVisible: DocumentSnapshot? = null
    private var prevRatingsListSize = 0
    private var paginationSize = 5
    private var paginateFlag = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaloonReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        binding.saloonReviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.saloonReviewNoReviewTextLabel.visibility = View.VISIBLE
        binding.saloonReviewRecyclerView.visibility = View.GONE
        getReviewDataFromFireStore()

        binding.saloonReviewAddReviewRatingButton.setOnClickListener {
            val parentFragment = this.parentFragment as SaloonItemFragment
            parentFragment.startRatingDialog()
        }

        dataViewModel.getSaloonRefreshState().observe(viewLifecycleOwner, {
            if (it.saloonReview){
                binding.saloonReviewRecyclerView.adapter?.notifyItemRangeRemoved(0,ratingsList.size)
                ratingsList.clear()
                lastVisible = null
                getReviewDataFromFireStore()
            }
        })

        binding.loadMoreReviewsButton.setOnClickListener {
            getReviewDataFromFireStore()
        }
    }

    private fun getReviewDataFromFireStore(){

        val dataViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        dataViewModel.getShopData().observe(viewLifecycleOwner, { data ->
            saloonID = data.saloonID.toString()

            val query: Task<QuerySnapshot> = if (lastVisible == null){

                db.collection("Saloons")
                    .document(saloonID)
                    .collection("rating")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()

            } else {

                db.collection("Saloons")
                    .document(saloonID)
                    .collection("rating")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible!!.get("timestamp"))
                    .limit(5)
                    .get()
            }

            paginateFlag = lastVisible != null

            query.addOnSuccessListener {

                    if (it.isEmpty){
                        binding.loadMoreReviewsButton.visibility = View.GONE
                    } else {
                        binding.loadMoreReviewsButton.visibility = View.VISIBLE
                    }

                    if (!it.isEmpty){
                        lastVisible = it.documents.lastOrNull()
                        prevRatingsListSize = ratingsList.size

                        for (document in it.documents){
                            val ratingID = document["rating_id"].toString()
                            val rating = document["rating_value"].toString()
                            val timestamp = document["timestamp"].toString()
                            val userID = document["user_id"].toString()
                            val review = URLDecoder.decode(document["review"].toString(), "UTF-8")

                            val ratingData = RatingReviewData(ratingID, rating, timestamp, userID, review)
                            ratingsList.add(ratingData)
                        }

                        if (ratingsList.size == 0){
                            binding.saloonReviewNoReviewTextLabel.visibility = View.VISIBLE
                            binding.saloonReviewRecyclerView.visibility = View.INVISIBLE
                            binding.loadMoreReviewsButton.visibility = View.INVISIBLE
                        } else {
                            binding.saloonReviewNoReviewTextLabel.visibility = View.GONE
                            binding.saloonReviewRecyclerView.visibility = View.VISIBLE
                            binding.loadMoreReviewsButton.visibility = View.VISIBLE
                        }

                        Log.i(TAG, "Firestore data is fetched")

                        if (!paginateFlag){

                            binding.saloonReviewRecyclerView.adapter =  SaloonReviewRecyclerAdapter(ratingsList)
                        } else {

                            binding.saloonReviewRecyclerView.adapter?.notifyItemInserted(prevRatingsListSize)
                        }
                    }
                }
                .addOnFailureListener {e->
                    Log.i(TAG, "Error in fetching saloon review data : ${e.message}")
                }
        })
    }

    data class RatingReviewData(
        val ratingID: String,
        val rating: String,
        val timestamp: String,
        val userID: String,
        val review: String = ""
    )

    companion object {
        private const val TAG = "TAGSaloonReviewFragment"
    }
}