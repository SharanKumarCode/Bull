package com.bullSaloon.bull

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bullSaloon.bull.databinding.ActivityHomeBinding
import com.bullSaloon.bull.fragments.homeActivity.SignUpAndSignInFragment

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val SignUpAndSignInFragment = SignUpAndSignInFragment()

        supportFragmentManager.beginTransaction().replace(binding.SignInFragmentContainer.id, SignUpAndSignInFragment).commit()
    }
}