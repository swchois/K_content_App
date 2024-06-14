package com.example.k_content_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AnnounceActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announce)

        announceWinner()
    }

    private fun announceWinner() {
        val winnerTextView = findViewById<TextView>(R.id.winnerText)
        db.collection("entries").get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val entries = snapshot.documents
                val randomIndex = (0 until entries.size).random()
                val winner = entries[randomIndex].getString("uid")
                winnerTextView.text = "당첨자: $winner"
            } else {
                winnerTextView.text = "응모자가 없습니다."
            }
        }
    }
}
