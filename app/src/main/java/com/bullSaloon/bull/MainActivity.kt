package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bullSaloon.bull.fragments.ShopListFragment
import com.bullSaloon.bull.fragments.StyleListFragment
import com.bullSaloon.bull.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        launchShopListFragment()

        binding.buttonBullMagic.setOnClickListener{
            launchBullMagicActivity()
        }

        binding.topAppBarToolBar.setOnMenuItemClickListener {
            when(it.itemId){

                R.id.menuItemAbout -> {
                    val builder = AlertDialog.Builder(this)
                    val appOwner = "Sharan Kumar"
                    val appVersion = "1.0"
                    val title = "About"
                    builder.setMessage("Created by $appOwner \n\nApp version: $appVersion")
                        .setTitle(title)
                        .setPositiveButton("OK"
                        ) { p0, _ -> p0?.dismiss() }
                        .show()
                    true
                }

                R.id.menuItemSignOut ->{
                    Firebase.auth.signOut()
                    startActivity(Intent(this,SplashScreenActivity::class.java))
                    true
                }

                else -> false
            }

        }

        binding.tabInput.getTabAt(1)?.select()

        binding.tabInput.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0){
                    launchStyleListFragment()
                } else {
                    launchShopListFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

    }

    private fun launchBullMagicActivity(){
        val intent = Intent(this, BullMagicActivity::class.java)
        startActivity(intent)
    }

    private fun launchShopListFragment(){

        val shopListFragment = ShopListFragment()
        supportFragmentManager.beginTransaction().replace(R.id.shopFragmentContainer, shopListFragment).commit()

    }

    private fun launchStyleListFragment(){

        val styleListFragment = StyleListFragment()
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.shopFragmentContainer, styleListFragment)
            .addToBackStack(null)
            .commit()

    }


}