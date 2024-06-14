package com.example.k_content_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ResultActivity : AppCompatActivity() {

    // Firebase Auth와 Firestore의 인스턴스를 선언합니다.
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        // 시스템 바 인셋을 설정하여 레이아웃이 전체 화면에 맞춰지도록 합니다.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.result)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase Auth와 Firestore를 초기화합니다.
        auth = Firebase.auth
        db = Firebase.firestore

        // 인텐트에서 score와 totalQuestions 값을 가져옵니다.
        val score = intent.getIntExtra("score", 0)  // 총점수 (캐시)
        val totalQuestions = intent.getIntExtra("totalQuestions", 0)  // 총 문제 수
        val correctAnswers = score / 20  // 맞춘 문제 수 (한 문제당 20캐시를 얻으므로)

        // 결과를 화면에 표시합니다.
        val resultText = findViewById<TextView>(R.id.resultText)
        resultText.text = "   $correctAnswers/$totalQuestions\n${score}c 획득"

        // Firestore의 cash 값을 업데이트합니다.
        updateCash(score)
    }

    // Firestore에서 현재 사용자의 cash 값을 업데이트하는 함수입니다.
    private fun updateCash(newCash: Int) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userDocRef = db.collection("users").document(currentUser.uid)
            // 새로운 cash 값을 덮어씁니다.
            userDocRef.update("cash", newCash)
                .addOnSuccessListener {
                    // 성공적으로 업데이트했을 때 처리할 내용
                    Log.d("ResultActivity", "Cash successfully updated to $newCash")
                }
                .addOnFailureListener { e ->
                    // 업데이트 실패 시 처리할 내용
                    Toast.makeText(this, "Failed to update cash: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
