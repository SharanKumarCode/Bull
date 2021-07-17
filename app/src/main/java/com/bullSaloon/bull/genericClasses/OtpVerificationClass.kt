package com.bullSaloon.bull.genericClasses

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.bullSaloon.bull.MainActivity
import com.bullSaloon.bull.databinding.FragmentCreateAccountBinding
import com.bullSaloon.bull.databinding.FragmentSignInBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class OtpVerificationClass() {
    private var auth: FirebaseAuth = Firebase.auth
    private val EXTRA_INTENT: String = "userBasicData"
    private val TAG: String = "TAG"

    private lateinit var mobileNumber: String
    private lateinit var storedVerificationId:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var context: Context

    private var userName: String = ""

    private lateinit var mobileNumberTextField: TextInputEditText
    private lateinit var verifyOtpButton: Button
    private lateinit var otpBox1: EditText
    private lateinit var otpBox2: EditText
    private lateinit var otpBox3: EditText
    private lateinit var otpBox4: EditText
    private lateinit var otpBox5: EditText
    private lateinit var otpBox6: EditText

    constructor(_mobileNumber: String, _username: String, binding: FragmentCreateAccountBinding):this(){
        this.mobileNumber = _mobileNumber
        this.userName = _username
        this.mobileNumberTextField = binding.mobileNumberTextField
        this.verifyOtpButton = binding.verifyOtpButton
        this.otpBox1 = binding.otpBox1
        this.otpBox2 = binding.otpBox2
        this.otpBox3 = binding.otpBox3
        this.otpBox4 = binding.otpBox4
        this.otpBox5 = binding.otpBox5
        this.otpBox6 = binding.otpBox6
        this.context = mobileNumberTextField.context
    }

    constructor(_mobileNumber: String, binding: FragmentSignInBinding):this(){
        this.mobileNumber = _mobileNumber
        this.mobileNumberTextField = binding.mobileNumberTextField
        this.verifyOtpButton = binding.verifyOtpButton
        this.otpBox1 = binding.otpBox1
        this.otpBox2 = binding.otpBox2
        this.otpBox3 = binding.otpBox3
        this.otpBox4 = binding.otpBox4
        this.otpBox5 = binding.otpBox5
        this.otpBox6 = binding.otpBox6
        this.context = mobileNumberTextField.context
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.i(TAG, "Verification is completed")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException){
                Toast.makeText(mobileNumberTextField.context, "OTP is incorrect", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(mobileNumberTextField.context, "Something went wrong..", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "error: ${e.message}")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            Log.i(TAG, "error: Code is sent to $mobileNumber and $verificationId")
            storedVerificationId = verificationId
            resendToken = token

            verifyOtpButton.setOnClickListener {
                val otp = otpBox1.text.toString() + otpBox2.text.toString() + otpBox3.text.toString() + otpBox4.text.toString() + otpBox5.text.toString() + otpBox6.text.toString()
                if (otp.length == 6){
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(storedVerificationId,otp)
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Toast.makeText(mobileNumberTextField.context,"Enter OTP", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun sendVerificationCode(activity: Activity){

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobileNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun reSendVerificationCode(activity: Activity){

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobileNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setForceResendingToken(resendToken)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){

        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        Log.i(TAG, "Successfully signed in")
                        Toast.makeText(context, "Successfully signed in", Toast.LENGTH_SHORT).show()

                        // if username is blank start main activity. If username exists, update username in fireStore and start activity
                        if (userName != ""){
                            updateUserName()
                        }

                        //starting main activity
                        startMainActivity()
                    }
                    it.exception is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                    it.exception is FirebaseAuthInvalidUserException -> {
                        Toast.makeText(context, "Invalid User", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun updateUserName(){
        Log.i(TAG,"updating user to fireStore")
        val db = Firebase.firestore

        val userId = auth.currentUser?.uid.toString()

        val user = hashMapOf(
            "user_name" to userName,
            "user_id" to userId,
            "mobile_number" to mobileNumber
        )

        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { result ->
                if (result.exists()){
                    Log.i(TAG,"user already exists")
                } else {
                    db.collection("Users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Log.i(TAG,"user is added")
                            startMainActivity()
                        }
                        .addOnFailureListener {
                            Log.i(TAG,"user update failed, error: ${it.message}")
//                            auth.currentUser?.delete()
                        }
                }
            }
            .addOnFailureListener {
                Log.i(TAG,"task failed, error: ${it.message}")
//                auth.currentUser?.delete()
            }
    }

    private fun startMainActivity(){
        val context = otpBox1.context
        if (auth.currentUser == null){
            Log.i(TAG,"error: current user is null")
            Toast.makeText(context, "Error occurred. Please check your internet connection", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(context, MainActivity::class.java)
            val db = Firebase.firestore
            db.collection("Users")
                .document(auth.currentUser?.uid!!)
                .get()
                .addOnSuccessListener {
                    if (it?.exists()!!) {
                        val userBasicData = UserBasicDataParcelable(
                            it.getString("user_id")!!,
                            it.getString("user_name")!!,
                            it.getString("mobile_number")!!
                        )
                        intent.putExtra(EXTRA_INTENT, userBasicData)
                        context.startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Log.i(TAG,"error: ${it.message}")
                    Toast.makeText(context, "Error occurred. Please check your internet connection", Toast.LENGTH_SHORT).show()
                }
        }
    }
}