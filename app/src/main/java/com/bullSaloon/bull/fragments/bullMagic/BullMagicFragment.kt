package com.bullSaloon.bull.fragments.bullMagic

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentBullMagicBinding

class BullMagicFragment : Fragment() {

    private var _binding: FragmentBullMagicBinding? = null
    private val binding get() = _binding!!

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

        _binding = FragmentBullMagicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.getSerializable("userImageData") as HashMap<String,String>?

        val navHost = this.childFragmentManager.findFragmentById(R.id.bullMagicfragmentContainer)
        val navController = navHost?.findNavController()

        if (args?.get("fragment_flag") == FragmentFlag.BullMagicItem){
            Log.i("TAGNav", "navigating to Bull magic Item : $args")
            val arg = Bundle()
            arg.putSerializable("userImageData",args)
            navController?.navigate(R.id.action_bullMagicListFragment_to_bullMagicItemFragment, arg)
        } else if (args?.get("fragment_flag") == FragmentFlag.BullMagicTargetUser) {
            Log.i("TAGNav", "navigating to Bull magic target user : $args")
            val arg = Bundle()
            arg.putString("user_id", args["user_id"])
            navController?.navigate(R.id.action_bullMagicListFragment_to_bullMagicTargetUserFragment, arg)
        }

    }

    companion object {

        private object FragmentFlag {
            const val BullMagicItem = "BullMagicItem"
            const val BullMagicTargetUser = "BullMagicTargetUser"
        }
    }
}