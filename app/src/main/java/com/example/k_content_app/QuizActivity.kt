package com.example.k_content_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

data class QuizQuestion(val question: String, val answer: Boolean)

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var btnO: ImageButton
    private lateinit var btnX: ImageButton

    private var currentQuestionIndex = 0
    private var score = 0
    private val questions = listOf(
        QuizQuestion("드라마 '눈물의 여왕' 홍해인이 운영하는 백화점의 이름은 킹즈백화점 이다.", false),
        QuizQuestion("드라마 '오징어 게임' 성기훈은 한번 이기면 20만원을 주는 딱지치기에 응했습니다.", false),
        QuizQuestion("드라마 '도깨비' 김신의 나이는 939세이다.", true),
        QuizQuestion("드라마 '오징어 게임'의 총 상금은 456만원이다.", false),
        QuizQuestion("드라마 '도깨비' 4화 '사랑의 물리학'이라는 시에서 '쿵'이라는 단어가 총 3번 등장했다.", true)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.quiz)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        questionText = findViewById(R.id.questionText)
        btnO = findViewById(R.id.btnO)
        btnX = findViewById(R.id.btnX)

        updateQuestion()

        btnO.setOnClickListener { checkAnswer(true) }
        btnX.setOnClickListener { checkAnswer(false) }
    }

    private fun updateQuestion() {
        if (currentQuestionIndex < questions.size) {
            questionText.text = questions[currentQuestionIndex].question
        } else {
            showResult()
        }
    }
    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = questions[currentQuestionIndex].answer
        if (userAnswer == correctAnswer) {
            score += 20
            Toast.makeText(this, "정답입니다!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "틀렸습니다!", Toast.LENGTH_SHORT).show()
        }
        currentQuestionIndex++
        updateQuestion()
    }


    private fun showResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("score", score)
        intent.putExtra("totalQuestions", questions.size)
        startActivity(intent)
        finish()
    }
}