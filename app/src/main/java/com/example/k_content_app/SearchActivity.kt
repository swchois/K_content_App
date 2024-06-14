package com.example.k_content_app

import RVAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchActivity : AppCompatActivity() {
    private val items = mutableListOf<SearchModel>()
    private lateinit var rvAdapter: RVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val db = Firebase.firestore
        val searchText = intent.getStringExtra("searchText")

        // Firestore에서 데이터 가져오기
        db.collection("drama")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val dramaTitle = document.getString("dramaTitle") ?: ""
                    val location = document.getString("location") ?: ""

                    // 데이터를 items 리스트에 추가
                    items.add(SearchModel(imageUrl, dramaTitle, location))
                }

                // 데이터를 가져온 후 어댑터 초기화 및 리사이클러뷰 설정
                initializeAdapter(searchText)
            }
            .addOnFailureListener { exception ->
                Log.d("inputdataError", "Error getting documents: ", exception)
            }

    }

    private fun initializeAdapter(searchText: String?) {
        // 어댑터 초기화
        rvAdapter = RVAdapter(baseContext, items, searchText)

        // 리사이클러뷰 설정
        val recyclerView = findViewById<RecyclerView>(R.id.rv)
        recyclerView.adapter = rvAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // SearchView 리스너 설정
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setQuery(searchText, false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                rvAdapter.filter(newText.orEmpty())
                return true
            }
        })

        // 아이템 클릭 리스너 설정
        rvAdapter.setOnItemClickListener { selectedItem ->
            val intent = Intent(this@SearchActivity, DramaDetailActivity::class.java).apply {
                putExtra("image", selectedItem.imageUrl)
                putExtra("title", selectedItem.dramaTitle)
                putExtra("location", selectedItem.location)
            }
            startActivity(intent)
        }
    }
}
