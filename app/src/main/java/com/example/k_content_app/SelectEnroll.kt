package com.example.k_content_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SelectEnroll : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_enroll)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.enroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val generalBtn = findViewById<Button>(R.id.button2)
        generalBtn.setOnClickListener {

            val intent = Intent(this,GeneralEnroll::class.java)
            startActivity(intent)
        }
    }
}