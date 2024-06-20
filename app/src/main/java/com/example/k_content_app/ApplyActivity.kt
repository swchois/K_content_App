package com.example.k_content_app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ApplyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply)

        // Firebase 초기화
        auth = Firebase.auth

        // 응모 버튼과 당첨자 발표 버튼 설정
        val applyButton = findViewById<Button>(R.id.applyButton)
        val announceButton = findViewById<Button>(R.id.announceButton)

        // 특정 날짜와 시간을 설정
        val pickWinnerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val clearEntriesTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
        }

        // 현재 시간과 비교하여 버튼 활성화 상태 설정
        updateButtonStates(applyButton, announceButton, pickWinnerTime, clearEntriesTime)
        setAlarms(pickWinnerTime, clearEntriesTime)

        // 응모 버튼 클릭 리스너 설정
        applyButton.setOnClickListener {
            applyForLottery()
        }

        // 당첨자 발표 버튼 클릭 리스너 설정
        announceButton.setOnClickListener {
            startActivity(Intent(this, AnnounceActivity::class.java))
        }
    }

    private fun updateButtonStates(applyButton: Button, announceButton: Button, pickWinnerTime: Calendar, clearEntriesTime: Calendar) {
        val currentTime = Calendar.getInstance()
        when {
            currentTime.time.before(pickWinnerTime.time) -> {
                announceButton.isEnabled = false
                announceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabled_button))
                applyButton.isEnabled = true
                applyButton.setBackgroundColor(ContextCompat.getColor(this, R.color.active_button))
            }
            currentTime.time.after(pickWinnerTime.time) && currentTime.time.before(clearEntriesTime.time) -> {
                announceButton.isEnabled = true
                announceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.active_button))
                applyButton.isEnabled = false
                applyButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabled_button))
            }
            else -> {
                announceButton.isEnabled = false
                announceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabled_button))
                applyButton.isEnabled = true
                applyButton.setBackgroundColor(ContextCompat.getColor(this, R.color.active_button))
            }
        }
    }

    private fun setAlarms(pickWinnerTime: Calendar, clearEntriesTime: Calendar) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pickWinnerIntent = Intent(this, MyBroadcastReceiver::class.java).apply {
            action = "com.example.k_content_app.PICK_WINNER"
        }
        val pickWinnerPendingIntent = PendingIntent.getBroadcast(
            this, 0, pickWinnerIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // 플래그 추가
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            pickWinnerTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pickWinnerPendingIntent
        )

        val clearEntriesIntent = Intent(this, MyBroadcastReceiver::class.java).apply {
            action = "com.example.k_content_app.CLEAR_ENTRIES"
        }
        val clearEntriesPendingIntent = PendingIntent.getBroadcast(
            this, 1, clearEntriesIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // 플래그 추가
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            clearEntriesTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            clearEntriesPendingIntent
        )
    }

    private fun applyForLottery() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userDocRef = db.collection("users").document(currentUser.uid)
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val currentCash = document.getLong("cash") ?: 0
                    if (currentCash >= 50) {
                        val updatedCash = currentCash - 50
                        userDocRef.update("cash", updatedCash)
                            .addOnSuccessListener {
                                saveEntry(currentUser.uid)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "캐시 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "캐시가 부족합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "사용자 데이터 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveEntry(uid: String) {
        val entry = hashMapOf(
            "uid" to uid,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("entries").add(entry)
            .addOnSuccessListener {
                Toast.makeText(this, "응모 완료", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "응모 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
