package com.example.k_content_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyBroadcastReceiver : BroadcastReceiver() {

    private val db = Firebase.firestore

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "com.example.k_content_app.PICK_WINNER") {
            pickWinner()
        } else if (action == "com.example.k_content_app.CLEAR_ENTRIES") {
            clearEntries()
        }
    }

    private fun pickWinner() {
        db.collection("entries").get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val entries = snapshot.documents
                val randomIndex = (0 until entries.size).random()
                val winner = entries[randomIndex].getString("uid")
                Log.d("MyBroadcastReceiver", "Winner selected: $winner")
                // 당첨자 발표를 위해 로그에 남기거나 별도의 처리를 할 수 있습니다.
            } else {
                Log.d("MyBroadcastReceiver", "No entries found.")
            }
        }
    }

    private fun clearEntries() {
        db.collection("entries").get().addOnSuccessListener { snapshot ->
            snapshot.documents.forEach { it.reference.delete() }
            Log.d("MyBroadcastReceiver", "Entries cleared.")
        }
    }
}
