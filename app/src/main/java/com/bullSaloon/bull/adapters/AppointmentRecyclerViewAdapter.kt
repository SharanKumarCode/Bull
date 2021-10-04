package com.bullSaloon.bull.adapters

import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.ViewHolderAppointmentItemBinding
import com.bullSaloon.bull.fragments.AppointmentsFragment
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.genericClasses.dataClasses.AppointmentDataClass

class AppointmentRecyclerViewAdapter(lists: MutableList<AppointmentDataClass>, _fragment: AppointmentsFragment): RecyclerView.Adapter<AppointmentRecyclerViewAdapter.AppointmentViewHolder>() {

    private val appointmentList = lists
    private val fragment = _fragment
    private val storageRef = SingletonInstances.getStorageReference()
    private val db = SingletonInstances.getFireStoreInstance()

    private lateinit var userID: String
    private lateinit var saloonID: String
    private lateinit var appointmentID: String


    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val binding : ViewHolderAppointmentItemBinding = ViewHolderAppointmentItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_appointment_item,parent,false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val holderBinding = holder.binding

        holderBinding.appointmentSaloonNameTextView.text = appointmentList[position].saloonName
        holderBinding.appointmentAreaNameTextView.text = appointmentList[position].areaName
        holderBinding.appointmentServiceNameText.text = appointmentList[position].service

        holderBinding.appointmentDateText.text = appointmentList[position].date
        holderBinding.appointmentTimeText.text = appointmentList[position].time

        setSaloonDisplayPic(holderBinding, appointmentList[position].saloonID)

        holderBinding.cancelAppointmentButton.setOnClickListener {
            startCancelAppointmentDialog(holderBinding)
        }

        userID = appointmentList[position].userID
        saloonID = appointmentList[position].saloonID
        appointmentID = appointmentList[position].appointmentID
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    private fun setSaloonDisplayPic(binding: ViewHolderAppointmentItemBinding, saloonID: String){

        db.collection("Saloons")
            .document(saloonID)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    val imageUrl = it.getString("display_pic_image_ref")!!
                    val imageRef = storageRef.storage.getReferenceFromUrl(imageUrl)

                    GlideApp.with(binding.root.context)
                        .asBitmap()
                        .load(imageRef)
                        .centerCrop()
                        .placeholder(R.drawable.ic_bull)
                        .into(binding.appointmentSaloonDisplayImageView)
                }
            }
            .addOnFailureListener { e->
                Log.i(TAG, "error in getting saloon display pic : ${e.message}")
            }
    }

    private fun startCancelAppointmentDialog(binding: ViewHolderAppointmentItemBinding){

        val builder = AlertDialog.Builder(binding.root.context)
        builder.setMessage("Are you sure to Cancel the Appointment?")
        builder.setPositiveButton("Yes"
        ) { _, _ -> cancelAppointment(binding) }
        builder.setNegativeButton("No", object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0?.dismiss()
            }
        })

        val dialog = builder.create()


        dialog.show()
    }

    private fun cancelAppointment(binding: ViewHolderAppointmentItemBinding){
        db.collection("Users")
            .document(userID)
            .collection("appointments")
            .document(appointmentID)
            .delete()
            .addOnSuccessListener {
                db.collection("Saloons")
                    .document(saloonID)
                    .collection("appointments")
                    .document(appointmentID)
                    .delete()
                    .addOnSuccessListener {
                        Log.i(TAG,"Appointment cancelled successfully")
                        fragment.returnGetAppointmentListFireStore()
                    }
                    .addOnFailureListener { e->
                        Log.i(TAG,"Error in cancelling appointment: ${e.message}")
                        Toast.makeText(binding.root.context, "Error in Cancelling Appointment. \n\n Please try again later", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e->
                Log.i(TAG,"Error in cancelling appointment: ${e.message}")
                Toast.makeText(binding.root.context, "Error in Cancelling Appointment. \n\n Please try again later", Toast.LENGTH_SHORT).show()
            }
    }

    companion object{
        private const val TAG = "TAGAppointmentRecyclerViewAdapter"
    }
}