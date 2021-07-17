package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bullSaloon.bull.fragments.ShopListFragment
import com.bullSaloon.bull.fragments.StyleListFragment
import com.bullSaloon.bull.databinding.ActivityMainBinding
import com.bullSaloon.bull.genericClasses.UserBasicDataParcelable
import com.bullSaloon.bull.genericClasses.UserDataClass
import com.bullSaloon.bull.viewModel.UserDataViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val EXTRA_INTENT: String = "userBasicData"
    private val TAG: String = "TAG"
    private lateinit var getIntent: UserBasicDataParcelable


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        launchShopListFragment()

        getIntent = intent.getParcelableExtra(EXTRA_INTENT)!!
        val data = UserDataClass(getIntent?.user_id!!, getIntent.user_name, getIntent.mobileNumber)

        binding.buttonBullMagic.setOnClickListener{
            launchBullMagicActivity()
        }

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        val userDataModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        val header = binding.navigationView.getHeaderView(0)

        userDataModel.assignBasicUserData(data)
        userDataModel.getUserBasicData().observe(this, Observer {

            if (it != null){
                header.findViewById<TextView>(R.id.userNameNavigationHeaderTextView).text = it.user_name
                header.findViewById<TextView>(R.id.userMobileNavigationHeaderTextView).text = it.mobileNumber
            }
        })

        binding.navigationView.setNavigationItemSelectedListener {
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

                R.id.menuItemBullMagic ->{
                    launchBullMagicActivity()
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
        intent.putExtra(EXTRA_INTENT, getIntent)
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