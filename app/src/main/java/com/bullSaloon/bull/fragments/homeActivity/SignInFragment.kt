package com.bullSaloon.bull.fragments.homeActivity

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.FragmentSignInBinding
import com.bullSaloon.bull.genericClasses.OtpTextWatcherGeneric
import com.bullSaloon.bull.genericClasses.OtpVerificationClass


class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private var timer: CountDownTimer? = null

    private var mobileNumber: String = ""
    private lateinit var otpVerification: OtpVerificationClass


    private val db = SingletonInstances.getFireStoreInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        enterTransition = inflaterTrans.inflateTransition(R.transition.slide_left_to_right)
        exitTransition = inflaterTrans.inflateTransition(R.transition.slide_right_to_left)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(TAG, "SignInFragment created")

        binding.mobileNumberTextField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {

                when {
                    binding.mobileNumberTextField.text?.length!! > 10 -> {
                        binding.mobileNumberTextInputLayout.error = "Not a Valid Mobile Number"
                    }
                    binding.mobileNumberTextField.text?.length!! == 10 -> {
                        val phoneNumberCheck = "+91${binding.mobileNumberTextField.text.toString()}"
                        val collectionRef = db.collection("Users")
                        val task = collectionRef.whereEqualTo("mobile_number", phoneNumberCheck).get()

                        collectionRef.document("dummy").set(hashMapOf("data" to "dummy"))
                            .addOnSuccessListener {
                                Log.i(TAG, "User success")
                            }
                            .addOnFailureListener {
                                Log.i(TAG, "User failure : ${it.message}")
                            }

                        task.addOnSuccessListener {
                            Log.i(TAG, "User : ${it.size()} , mobile : $phoneNumberCheck")

                            if (it.isEmpty){
                                binding.mobileNumberTextInputLayout.apply {
                                    error = "Account does not exist with this mobile number"
                                }
                            } else {
                                    binding.mobileNumberTextInputLayout.apply {
                                        error = null
                                        boxStrokeColor = ContextCompat.getColor(binding.root.context,R.color.openStatusColor)
                                        hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context,R.color.openStatusColor))
                                    }
                            }
                        }
                    }
                    else -> {
                        binding.mobileNumberTextInputLayout.apply {
                            error = null
                            boxStrokeColor = ContextCompat.getColor(binding.root.context,R.color.white)
                            hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context,R.color.white))
                        }
                    }
                }
            }
        })

        binding.generateOtpButton.setOnClickListener {
            val phoneNumberCheck = "+91${binding.mobileNumberTextField.text.toString()}"
            val collectionRef = db.collection("Users")
            val task = collectionRef.whereEqualTo("mobile_number", phoneNumberCheck).get()
            task
                .addOnSuccessListener {
                    Log.i(TAG, "User : ${it.size()}")
                    if (it.isEmpty) {
                        Log.i("TAG", "User does not exists")
                        val builder = AlertDialog.Builder(binding.root.context)
                        val message =
                            "User account does not exist with Mobile Number: $phoneNumberCheck "
                        val title = "Error"
                        builder.setMessage(message)
                            .setTitle(title)
                            .setPositiveButton(
                                "OK"
                            ) { p0, _ -> p0?.dismiss() }
                            .show()
                    } else {
                        Log.i(TAG, "Existing User")
                        generateOtp()
                        binding.otpBox1.requestFocus()
                        binding.otpBox1.background = AppCompatResources.getDrawable(binding.root.context, R.drawable.bg_otp_box_focused)
                    }
                }
                .addOnFailureListener {
                    Log.i(TAG, "Unable to connect with server: ${it.message}")
                    Toast.makeText(
                        context,
                        "Unable to connect with server. \nCheck your Internet connection or please try again later..",
                        Toast.LENGTH_SHORT
                        ).show()
                }
        }

        binding.otpResendButton.setOnClickListener {
            startTimer()
            createAccount()
        }

        binding.CreateAccountButton.setOnClickListener {
            val createAccountFragment = CreateAccountFragment()
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.SignInFragmentContainer, createAccountFragment)?.commit()
        }
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateOtp(){
        binding.generateOtpButton.visibility = View.GONE
        binding.otpBox.visibility = View.VISIBLE

        binding.verifyOtpButton.apply {
            visibility = View.VISIBLE
            isClickable = false
            alpha = 0.2F
        }

        val otpEditBoxes = mutableListOf<EditText>()
        otpEditBoxes.add(binding.otpBox1)
        otpEditBoxes.add(binding.otpBox2)
        otpEditBoxes.add(binding.otpBox3)
        otpEditBoxes.add(binding.otpBox4)
        otpEditBoxes.add(binding.otpBox5)
        otpEditBoxes.add(binding.otpBox6)

        binding.apply {
            otpBox1.addTextChangedListener(OtpTextWatcherGeneric(otpBox1, otpEditBoxes,this))
            otpBox2.addTextChangedListener(OtpTextWatcherGeneric(otpBox2, otpEditBoxes,this))
            otpBox3.addTextChangedListener(OtpTextWatcherGeneric(otpBox3, otpEditBoxes,this))
            otpBox4.addTextChangedListener(OtpTextWatcherGeneric(otpBox4, otpEditBoxes,this))
            otpBox5.addTextChangedListener(OtpTextWatcherGeneric(otpBox5, otpEditBoxes,this))
            otpBox6.addTextChangedListener(OtpTextWatcherGeneric(otpBox6, otpEditBoxes,this))
        }

        startTimer()
        createAccount()
    }

    private fun createAccount(resendCLicked: Boolean = false){
        val mobileNumberField = binding.mobileNumberTextField.text.toString()

        if (mobileNumberField.isNotEmpty()
            && mobileNumberField.length == 10)
        {
            mobileNumber = "+91${binding.mobileNumberTextField.text.toString()}"
            otpVerification = OtpVerificationClass(mobileNumber,binding)
            if (resendCLicked){
                otpVerification.reSendVerificationCode(this.requireActivity())
            } else otpVerification.sendVerificationCode(this.requireActivity())
        } else {
            Toast.makeText(binding.root.context, "Enter Valid Mobile number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer(){

        binding.otpResendButton.visibility = View.GONE

        timer = object : CountDownTimer(60000, 1000){
            override fun onTick(millisecs: Long) {
                binding.labelOtpResend.text = resources.getString(R.string.textResendOtp,(millisecs/1000).toInt())
            }

            override fun onFinish() {
                binding.otpResendButton.visibility = View.VISIBLE
            }
        }

        timer?.start()
    }

    companion object {
        private const val TAG = "TAGSignInFragment"
    }

}