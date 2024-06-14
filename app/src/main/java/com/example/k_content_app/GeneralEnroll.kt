package com.example.k_content_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GeneralEnroll : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_enroll)

        // Firebase 인증 인스턴스 가져오기
        auth = FirebaseAuth.getInstance()

        val enrollButton = findViewById<Button>(R.id.button4)
        enrollButton.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.email)
            val passwordEditText = findViewById<EditText>(R.id.pwd)
            val checkPasswordEditText = findViewById<EditText>(R.id.checkpwd)
            val nameEditText = findViewById<EditText>(R.id.name)

            val email: String = emailEditText.text.toString()
            val password: String = passwordEditText.text.toString()
            val checkPassword: String = checkPasswordEditText.text.toString()
            val name: String = nameEditText.text.toString()

            // 비밀번호와 비밀번호 확인이 일치하는지 확인
            if (password != checkPassword) {
                Toast.makeText(
                    baseContext,
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                passwordEditText.requestFocus()
            } else {
                // 사용자 생성 및 Firebase에 이름 설정
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            // 사용자 이름을 Firebase에 설정
                            user?.let { updateUserProfile(it, name) }
                            updateUI(user)
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateUI(null)
                        }
                    }
            }
        }
    }

    private fun updateUserProfile(user: FirebaseUser, name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                } else {
                    Log.e(TAG, "Failed to update user profile.", task.exception)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        // Implement your reload logic here if needed
    }

    companion object {
        private const val TAG = "GeneralEnroll"
    }
}