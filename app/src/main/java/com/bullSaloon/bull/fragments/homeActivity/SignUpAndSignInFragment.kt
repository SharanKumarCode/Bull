package com.bullSaloon.bull.fragments.homeActivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bullSaloon.bull.MainActivity
import com.bullSaloon.bull.R
import com.bullSaloon.bull.SingletonInstances
import com.bullSaloon.bull.databinding.FragmentSignUpAndSignInBinding
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider


class SignUpAndSignInFragment : Fragment() {
    private var _binding: FragmentSignUpAndSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val auth = SingletonInstances.getAuthInstance()
    private val db = SingletonInstances.getFireStoreInstance()

    private lateinit var faceBookCredential: AuthCredential

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpAndSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val createAccountFragment = CreateAccountFragment()

        callbackManager = CallbackManager.Factory.create()

        binding.CreateAccountButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.setReorderingAllowed(true)
                ?.replace(R.id.SignInFragmentContainer, createAccountFragment)
                ?.commit()
        }

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.googleSignInButton.setOnClickListener {
            signIn()
        }

        binding.facebookSignInButton.setOnClickListener {
            binding.facebookLoginButton.performClick()
        }

        binding.facebookLoginButton.setPermissions("email", "public_profile")
//        binding.facebookLoginButton.fragment = this

        try {

            binding.facebookLoginButton.registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Log.i(TAG, "facebook:onSuccess : $result")

                    handleFacebookAccessToken(result?.accessToken!!)

                }

                override fun onCancel() {
                    Log.i(TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException?) {
                    Log.i(TAG, "facebook:onError", error)
                }

            })

        }catch (e:Exception){
            Log.i(TAG, "facebook login major error: ${e.message}")
        }


    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.i(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.i(TAG, "signInWithCredential:success")
                updateFireStoreData()
            }
            .addOnFailureListener { e->

                val request = GraphRequest.newMeRequest(token
                ) { resultObject, _ ->
                    val email = resultObject?.getString("email")
                    auth.fetchSignInMethodsForEmail(email!!)
                        .addOnSuccessListener {
                            faceBookCredential = credential
                            binding.googleSignInButton.performClick()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                }

                val parameters = Bundle()
                parameters.putString("fields","email")
                request.parameters = parameters
                request.executeAsync()

                Log.i(TAG, "signInWithCredential:failure - ${e.message}")

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

    }

    private val activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val accountID = task.result.idToken
                firebaseAuthWithGoogle(accountID!!)
            } catch (e: ApiException) {
                Log.i(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        activityResult.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.i(TAG, "firebaseAuthWithGoogle")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.i(TAG, "signInWithCredential:success")

                    if (this::faceBookCredential.isInitialized){
                        auth.currentUser?.linkWithCredential(faceBookCredential)
                            ?.addOnSuccessListener {
                                Log.i(TAG, "signInWithCredential:failure: linkWithCredential success ${it.user}")
                                checkFireStoreData()
                            }
                            ?.addOnFailureListener {
                                Log.i(TAG, "signInWithCredential:failure: linkWithCredential failed ${it.message}")
                            }
                    } else {
                        checkFireStoreData()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.i(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checkFireStoreData(){
        val userID = auth.currentUser?.uid

        db.collection("Users")
            .document(userID!!)
            .get()
            .addOnCompleteListener {
                if (it.isComplete){
                    if (!it.result.exists()){
                        updateFireStoreData()
                    } else {
//                        launchCreateAccountFragment()
                        launchMainActivity()
                    }
                }
            }
    }

    private fun updateFireStoreData(){

        db.collection("Users")
            .document(auth.currentUser?.uid!!)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    Log.i(TAG, "user exists in fireStore")
//                    launchCreateAccountFragment()
                    launchMainActivity()
                } else {
                    Log.i(TAG, "user does not exists in fireStore")

                    val user = hashMapOf(
                        "user_name" to "",
                        "user_id" to auth.currentUser?.uid!!,
                        "mobile_number" to ""
                    )

                    db.collection("Users")
                        .document(user["user_id"].toString())
                        .set(user)
                        .addOnSuccessListener {
                            Log.i(TAG, "user data created in fireStore")
//                            launchCreateAccountFragment()
                            launchMainActivity()
                        }
                        .addOnFailureListener {e->
                            Log.i(TAG, "error in updating user data fireStore : ${e.message}")
                        }
                }
            }
            .addOnFailureListener {
                Log.i(TAG, "error in fetching data : ${it.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchCreateAccountFragment(){
        val createAccountFragment = CreateAccountFragment()
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.SignInFragmentContainer, createAccountFragment)?.commit()
    }

    private fun launchMainActivity(){
        startActivity(Intent(requireContext(), MainActivity::class.java))
        activity?.finish()
    }

    companion object {
        private const val TAG = "TAGLoginActivity"
    }

}