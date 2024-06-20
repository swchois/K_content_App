package com.example.k_content_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.k_content_app.databinding.ActivityNavMainBinding

class Nav_mainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNavMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        // btn1 (검색화면 이동)
        binding.btn1.setOnClickListener {
            navController.navigate(R.id.searchingFragment)
        }

        // btn2 (마이페이지 이동)
        binding.btn2.setOnClickListener {
            navController.navigate(R.id.userInfoFragment)
        }

        // btn3 (홈화면 이동)
        binding.btn3.setOnClickListener {
            navController.navigate(R.id.mainHomeFragment)
        }
    }
}
