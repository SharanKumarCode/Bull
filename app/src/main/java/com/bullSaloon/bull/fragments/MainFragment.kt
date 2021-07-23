package com.bullSaloon.bull.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentMainBinding
import com.bullSaloon.bull.fragments.bullMagic.BullMagicListFragment
import com.bullSaloon.bull.fragments.saloon.ShopListFragment
import com.bullSaloon.bull.fragments.styles.StyleListFragment
import com.google.android.material.tabs.TabLayout

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
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

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //default fragment
        launchShopListFragment()

        binding.tabInput.getTabAt(1)?.select()

        binding.tabInput.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0){
                    launchStyleListFragment()
                } else if (tab?.position == 1) {
                    launchShopListFragment()
                } else {
                    launchBullMagicListFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

    }

    private fun launchBullMagicListFragment(){
        val bullMagicListFragment = BullMagicListFragment()
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.mainFragmentContainer, bullMagicListFragment)?.addToBackStack(null)?.commit()
    }

    private fun launchShopListFragment(){
        val shopListFragment = ShopListFragment()
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.mainFragmentContainer, shopListFragment)?.addToBackStack(null)?.commit()
    }

    private fun launchStyleListFragment(){
        val styleListFragment = StyleListFragment()
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.mainFragmentContainer, styleListFragment)?.addToBackStack(null)?.commit()
    }
}