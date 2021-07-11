package com.bullSaloon.bull.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bullSaloon.bull.recyclerViewAdapters.BullMagicListRecyclerViewAdapter
import com.bullSaloon.bull.databinding.FragmentBullMagicListBinding


class BullMagicListFragment : Fragment() {

    private var _binding: FragmentBullMagicListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBullMagicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lists = mutableListOf<Map<String,Any>>()

        val list1 = mapOf<String,Any>("User_Name" to "Sharan Kumar", "Shop_Name" to "Vaigai Saloon", "Nices" to 20, "Nice_filled" to true, "image" to "photo_1")
        val list2 = mapOf<String,Any>("User_Name" to "Santhosh", "Shop_Name" to "Style Saloon", "Nices" to 0, "Nice_filled" to false, "image" to "photo_3")
        val list3 = mapOf<String,Any>("User_Name" to "Shanthi", "Shop_Name" to "RK Men's Saloon", "Nices" to 100, "Nice_filled" to false, "image" to "photo_2")
        val list4 = mapOf<String,Any>("User_Name" to "Tamil Selvan", "Shop_Name" to "Ashiyana Saloon", "Nices" to 4, "Nice_filled" to true, "image" to "photo_4")
        val list5 = mapOf<String,Any>("User_Name" to "Jhonny", "Shop_Name" to "Marvel Saloon", "Nices" to 1289, "Nice_filled" to true, "image" to "photo_5")

        lists.add(list1)
        lists.add(list2)
        lists.add(list3)
        lists.add(list4)
        lists.add(list5)

        binding.recyclerViewBullMagicList.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewBullMagicList.adapter = BullMagicListRecyclerViewAdapter(lists)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}