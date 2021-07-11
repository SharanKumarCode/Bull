package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bullSaloon.bull.fragments.BullMagicListFragment
import com.bullSaloon.bull.databinding.ActivityBullMagicBinding

class BullMagicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBullMagicBinding

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBullMagicBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
//        supportActionBar?.setCustomView(R.layout.action_bar_layout)

        val bullMagicListFragment = BullMagicListFragment()
        supportFragmentManager.beginTransaction().replace(R.id.BullMagicFragmentContainer, bullMagicListFragment).commit()

        binding.floatingActionCameraButton.setOnClickListener {
            launchCameraActivity()
        }

    }

    private fun launchCameraActivity(){
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
}