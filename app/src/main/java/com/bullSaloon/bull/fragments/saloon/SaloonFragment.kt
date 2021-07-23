package com.bullSaloon.bull.fragments.saloon

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentSaloonBinding


class SaloonFragment : Fragment() {

    private var _binding: FragmentSaloonBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflaterTrans = TransitionInflater.from(requireContext())
        exitTransition = inflaterTrans.inflateTransition(R.transition.fade)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSaloonBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val saloonNavHostFragment = this.childFragmentManager.findFragmentById(R.id.saloonFragmentContainer)
        val navController = saloonNavHostFragment?.findNavController()

        Log.i("CameraX", "backstack onViewCreated Saloon - child Fragment :${navController?.backStack?.last?.destination}")
        Log.i("CameraX", "backstack onViewCreated Saloon - activity support Fragment :${activity?.supportFragmentManager?.findFragmentById(R.id.fragment)?.findNavController()?.backStack?.last?.destination}")

        if (navController?.currentDestination?.id == R.id.saloonFragment){
            navController.navigate(R.id.action_saloonFragment_to_saloon_navigation)
        }

    }
}