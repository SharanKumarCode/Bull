package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bullSaloon.bull.fragments.BullMagicListFragment
import com.bullSaloon.bull.databinding.ActivityBullMagicBinding
import com.bullSaloon.bull.genericClasses.UserBasicDataParcelable
import com.bullSaloon.bull.genericClasses.UserDataClass
import com.google.android.material.appbar.AppBarLayout

class BullMagicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBullMagicBinding
    private val EXTRA_INTENT: String = "userBasicData"
    private val TAG: String = "TAGO"
    private lateinit var getIntent: UserBasicDataParcelable

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBullMagicBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.hide()

        getIntent = intent.getParcelableExtra(EXTRA_INTENT)!!

        val bullMagicListFragment = BullMagicListFragment()
        supportFragmentManager.beginTransaction().replace(R.id.BullMagicFragmentContainer, bullMagicListFragment).commit()

        binding.floatingActionCameraButton.setOnClickListener {
            launchCameraActivity()
        }

    }

    private fun launchCameraActivity(){
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(EXTRA_INTENT, getIntent)
        startActivity(intent)
    }
}