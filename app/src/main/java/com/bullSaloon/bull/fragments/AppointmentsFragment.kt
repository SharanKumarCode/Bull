package com.bullSaloon.bull.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.adapters.AppointmentRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentAppointmentsBinding
import com.bullSaloon.bull.genericClasses.dataClasses.AppointmentDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Month


class AppointmentsFragment : Fragment() {

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var userID: String

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = SingletonInstances.getAuthInstance()
        db = SingletonInstances.getFireStoreInstance()
        userID = auth.currentUser?.uid!!

        getAppointmentListFireStore()

        binding.recyclerAppointmentsFragment.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getAppointmentListFireStore(){

        val appointmentLists = mutableListOf<AppointmentDataClass>()

        db.collection("Users")
            .document(userID)
            .collection("appointments")
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    for (document in it.documents){
                        val appointmentID = document.getString("appointment_id")!!
                        val userID = document.getString("user_id")!!
                        val userName = document.getString("user_name")!!
                        val saloonID = document.getString("saloon_id")!!
                        val saloonName = document.getString("saloon_name")!!
                        val areaName = document.getString("area")!!
                        val service = document.getString("service")!!
                        val dateMap = document.get("date") as HashMap<String, Int>
                        val timeMap = document.get("time") as HashMap<String, String>

                        val date = "${dateMap["Day"]} , ${Month.of(dateMap["Month"].toString().toInt()+1).toString().substring(0,3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${dateMap["Year"]}"
                        val time = "${timeMap["Hour"]} : ${timeMap["Minute"]} ${timeMap["AmPm"]}"

                        val appointmentData = AppointmentDataClass(
                                        appointmentID,
                                        userID,
                                        userName,
                                        saloonID,
                                        saloonName,
                                        areaName,
                                        service,
                                        date,
                                        time
                        )

                        appointmentLists.add(appointmentData)
                    }
                }

                if (appointmentLists.size == 0){
                    binding.appointmentsLabel.visibility = View.VISIBLE
                } else {
                    binding.appointmentsLabel.visibility = View.GONE
                }

                binding.recyclerAppointmentsFragment.adapter = AppointmentRecyclerViewAdapter(appointmentLists, this)
            }
            .addOnFailureListener { e->
                Log.i(TAG, "error in getting appointment list : ${e.message}")
            }
    }

    fun returnGetAppointmentListFireStore(){
        getAppointmentListFireStore()
    }

    companion object {
        private const val TAG = "TAGAppointmentsFragment"
    }

}