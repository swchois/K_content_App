package com.example.k_content_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.example.k_content_app.databinding.ActivityMainBinding
import com.example.k_content_app.databinding.ActivityMainhomeBinding

class MainHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainhomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mainhome)

        // NavHostFragment 초기화
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
    }
}
