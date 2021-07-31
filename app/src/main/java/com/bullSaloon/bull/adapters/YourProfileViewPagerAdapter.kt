package com.bullSaloon.bull.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bullSaloon.bull.fragments.yourProfile.YourProfileCommentsFragment
import com.bullSaloon.bull.fragments.yourProfile.YourProfileNiceFragment
import com.bullSaloon.bull.fragments.yourProfile.YourProfilePhotosContainerFragment
import com.bullSaloon.bull.fragments.yourProfile.YourProfileReviewFragment

class YourProfileViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->{
                YourProfilePhotosContainerFragment()
            }
            1->{
                YourProfileCommentsFragment()
            }
            2->{
                YourProfileNiceFragment()
            }
            3->{
                YourProfileReviewFragment()
            }
            else -> Fragment()
        }
    }
}