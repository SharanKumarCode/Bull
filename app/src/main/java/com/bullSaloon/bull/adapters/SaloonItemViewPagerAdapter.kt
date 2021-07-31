package com.bullSaloon.bull.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bullSaloon.bull.fragments.saloon.SaloonPhotosFragment
import com.bullSaloon.bull.fragments.saloon.SaloonPricingFragment
import com.bullSaloon.bull.fragments.saloon.SaloonReviewFragment

class SaloonItemViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    val fragmentList = mutableListOf<Fragment>()
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->{
                fragmentList.add(SaloonPricingFragment())
                SaloonPricingFragment()
            }
            1->{
                SaloonPhotosFragment()
            }
            2->{
                SaloonReviewFragment()
            }
            else -> Fragment()
        }
    }
}