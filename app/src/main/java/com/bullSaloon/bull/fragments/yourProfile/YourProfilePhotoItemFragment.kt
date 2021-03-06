package com.bullSaloon.bull.fragments.yourProfile

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.FragmentYourProfilePhotoItemBinding
import com.bullSaloon.bull.genericClasses.GlideApp
import com.bullSaloon.bull.viewModel.YourProfileViewModel
import com.bumptech.glide.request.target.Target
import com.google.firebase.firestore.FieldValue
import java.net.URLDecoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class YourProfilePhotoItemFragment : Fragment() {

    private var _binding: FragmentYourProfilePhotoItemBinding? = null
    private val binding get() = _binding!!

    private val storageRef = SingletonInstances.getStorageReference()
    private val db = SingletonInstances.getFireStoreInstance()
    private val auth = SingletonInstances.getAuthInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)
        exitTransition = inflaterTrans.inflateTransition(R.transition.slide_left_to_right)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourProfilePhotoItemBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dataViewModel = ViewModelProvider(requireActivity()).get(YourProfileViewModel::class.java)
        dataViewModel.getUserPhotoData().observe(viewLifecycleOwner, { data ->

//            set Saloon Name
            binding.yourProfilePhotoItemSaloonName.text = data.saloonName

//            set Caption
            if (data.caption == ""){
                binding.yourProfilePhotoItemCaption.visibility = View.GONE
            } else {
                binding.yourProfilePhotoItemCaption.visibility = View.VISIBLE
                binding.yourProfilePhotoItemCaption.text = URLDecoder.decode(data.caption, "UTF-8")
            }

//            set number of nices
            binding.NicesTextView.text = data.nices.toString()

//            set date
            val date = data.timestamp.substring(0,10)
            val dateFormatted = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
            val month = dateFormatted.month.toString()
                .lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            binding.yourProfilePhotoItemDate.text = resources.getString(R.string.textBullMagicImageDate,dateFormatted.dayOfMonth,month,dateFormatted.year.toString())

//            set image
            setImageFromFirebase(requireContext(), binding, data.imageRef)

//            delete image
            binding.deleteImageView.setOnClickListener {
                deleteImageFromFirebaseCloud(data.imageRef, data.photoID)
            }
        })

        binding.yourProfileBackButtonImageView.setOnClickListener {
            this.parentFragmentManager.findFragmentById(R.id.YourProfilePhotoFragmentContainer)
                ?.findNavController()
                ?.navigate(R.id.action_yourProfilePhotoItemFragment2_to_yourProfilePhotosFragment2)
        }
    }

    private fun setImageFromFirebase(context: Context, binding: FragmentYourProfilePhotoItemBinding, imageUrl: String){

        val imageRef = storageRef.storage.getReferenceFromUrl(imageUrl)
        val width = Resources.getSystem().displayMetrics.widthPixels

        GlideApp.with(context)
            .load(imageRef)
            .override(width,Target.SIZE_ORIGINAL)
            .placeholder(R.drawable.ic_bull)
            .into(binding.yourProfilePhotoItemImageView)
    }

    private fun deleteImageFromFirebaseCloud(imageUrl: String, photoID: String){

        val imageRef = storageRef.storage.reference.child(imageUrl.replace("gs://bull-saloon.appspot.com",""))

        imageRef.delete()
            .addOnSuccessListener {
                Log.i("TAG","pic is deleted")
                Toast.makeText(context,"Picture is deleted", Toast.LENGTH_SHORT).show()
                deleteImageFromFirestore(imageUrl, photoID)
            }
            .addOnFailureListener {
                Log.i("TAG","Error : ${it.message}")
                Toast.makeText(context,"Error occurred. Please try again after sometime or check your internet connection",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteImageFromFirestore(imageUrl: String, photoID: String){

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    if (it.get("photos.${photoID}.image_ref") == imageUrl){
                        db.collection("Users").document(auth.currentUser?.uid!!)
                            .update("photos.${photoID}", FieldValue.delete())
                            .addOnSuccessListener {
                                Log.i("TAG","pic deleted from fireStore")
                                this.parentFragmentManager.popBackStack()
                            }
                            .addOnFailureListener {e->
                                Log.i("TAG","error on pic deletion from fireStore: ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener {
                Log.i("TAG","error on getting data from fireStore: ${it.message}")
            }
    }
}