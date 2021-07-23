package com.bullSaloon.bull.fragments.homeActivity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentSignUpAndSignInBinding


class SignUpAndSignInFragment : Fragment() {

    private var _binding: FragmentSignUpAndSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpAndSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val createAccountFragment = CreateAccountFragment()
        val signInFragment = SignInFragment()

        binding.CreateAccountButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.setReorderingAllowed(true)
                ?.replace(R.id.SignInFragmentContainer, createAccountFragment)
                ?.commit()
        }

        binding.SignInButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.setReorderingAllowed(true)
                ?.replace(R.id.SignInFragmentContainer, signInFragment)
                ?.commit()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}