package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bullSaloon.bull.Fragments.ShopListFragment


class MainActivity : AppCompatActivity() {

    private lateinit var searchButton: ImageView


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchButton = findViewById(R.id.search_ImageView)

        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.action_bar_layout)

        val shopListFragment = ShopListFragment()
        supportFragmentManager.beginTransaction().replace(R.id.shopFragmentContainer, shopListFragment).commit()

    }


}