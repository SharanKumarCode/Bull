package com.bullSaloon.bull.fragments.yourProfile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentSaloonBinding
import com.bullSaloon.bull.databinding.FragmentYourProfilePhotosContainerBinding

class YourProfilePhotosContainerFragment : Fragment() {

    private var _binding: FragmentYourProfilePhotosContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourProfilePhotosContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}