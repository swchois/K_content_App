package com.example.k_content_app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URL

class AnnounceActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announce)

        auth = FirebaseAuth.getInstance()
        announceWinner()
    }

    private fun announceWinner() {
        val winnerMessage = findViewById<TextView>(R.id.winnerMessage)
        val winnerImage = findViewById<ImageView>(R.id.winnerImage)
        val winnerDisplayName = findViewById<TextView>(R.id.winnerDisplayName)
        val giftIcon = findViewById<ImageView>(R.id.giftIcon)
        val failIcon = findViewById<ImageView>(R.id.failIcon)

        val currentUser = auth.currentUser

        db.collection("entries").get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val entries = snapshot.documents
                val randomIndex = (0 until entries.size).random()
                val winnerUid = entries[randomIndex].getString("uid")

                if (currentUser != null && winnerUid == currentUser.uid) {
                    // 당첨자 본인
                    winnerMessage.text = "축하합니다! 당첨 입니다!!"

                    // 사용자 정보 가져오기
                    db.collection("users").document(winnerUid!!).get().addOnSuccessListener { userDoc ->
                        val displayName = userDoc.getString("displayname")
                        val imgUrl = userDoc.getString("img")

                        winnerDisplayName.text = displayName

                        // 이미지 로드 및 둥글게 처리
                        LoadImageTask(winnerImage).execute(imgUrl)

                        winnerDisplayName.visibility = TextView.VISIBLE
                        winnerImage.visibility = ImageView.VISIBLE
                        failIcon.visibility = ImageView.GONE

                        // 랜덤으로 gifticon 설정
                        val giftIcons = listOf(R.drawable.gifticon1, R.drawable.gifticon2, R.drawable.gifticon3)
                        val randomGiftIcon = giftIcons.random()
                        giftIcon.setImageResource(randomGiftIcon)
                        giftIcon.visibility = ImageView.VISIBLE
                    }
                } else {
                    // 당첨자 본인이 아닌 경우
                    winnerMessage.text = ""
                    failIcon.setImageResource(R.drawable.fail)
                    failIcon.visibility = ImageView.VISIBLE
                }
            } else {
                winnerMessage.text = "응모자가 없습니다."
                failIcon.visibility = ImageView.GONE
            }
        }
    }

    private class LoadImageTask(val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        override fun doInBackground(vararg params: String?): Bitmap? {
            return try {
                val url = URL(params[0])
                BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            result?.let {
                val circularBitmap = BitmapUtils.getCircularBitmap(it)
                imageView.setImageBitmap(circularBitmap)
            }
        }
    }
}
