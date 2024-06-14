package com.example.k_content_app

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DramaDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_drama_detail)

        val dramaDetailView = findViewById<RelativeLayout>(R.id.dramadetail)
        ViewCompat.setOnApplyWindowInsetsListener(dramaDetailView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Bottom Sheet
        val bottomSheet = findViewById<FrameLayout>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            peekHeight = 1500 // 초기 높이를 설정
        }

        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(mutableListOf()) // 빈 목록으로 초기화
        reviewRecyclerView.adapter = reviewAdapter

        // 드라마 정보를 가져옵니다.
        val dramaImage = intent.getStringExtra("image")
        val dramaTitle = intent.getStringExtra("title")
        val dramaLocation = intent.getStringExtra("location")

        // 이미지뷰, 텍스트뷰를 초기화하고 드라마 정보를 설정합니다.
        val imageView = findViewById<ImageView>(R.id.imageView)
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        val navigationButton = findViewById<Button>(R.id.navigationButton)
        val writeReviewButton = findViewById<Button>(R.id.writeReviewButton)

        Glide.with(this)
            .load(dramaImage)
            .into(imageView)

        titleTextView.text = dramaTitle
        locationTextView.text = dramaLocation

        // 지도 프래그먼트 설정
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        navigationButton.setOnClickListener {
            getDirections(dramaLocation!!)
        }

        writeReviewButton.setOnClickListener {
            showWriteReviewDialog()
        }
        getReviewsFromFirestore()
    }

    private fun showWriteReviewDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_write_review, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val contentEditText = dialogView.findViewById<EditText>(R.id.contentEditText)

        AlertDialog.Builder(this)
            .setTitle("Write a Review")
            .setView(dialogView)
            .setPositiveButton("Submit") { dialog, _ ->
                val title = titleEditText.text.toString()
                val content = contentEditText.text.toString()
                saveReviewToFirestore(title, content)
                dialog.dismiss()
                getReviewsFromFirestore()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun saveReviewToFirestore(title: String, content: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        // Get the current highest index value
        FirebaseFirestore.getInstance().collection("reviews")
            .orderBy("index", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                var newIndex = 0
                if (!documents.isEmpty) {
                    newIndex = documents.documents[0].getLong("index")?.toInt()?.plus(1) ?: 0
                }

                userRef.get().addOnSuccessListener { document ->
                    val userImgUrl = document.getString("img") ?: "@drawable/userimg"
                    val displayName = document.getString("displayname") ?: "anonymous"
                    val location = intent.getStringExtra("location") ?: ""

                    val review = hashMapOf(
                        "title" to title,
                        "content" to content,
                        "uid" to userId,
                        "displayName" to displayName,
                        "location" to location,
                        "img" to userImgUrl,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "index" to newIndex
                    )

                    FirebaseFirestore.getInstance().collection("reviews")
                        .add(review)
                        .addOnSuccessListener {
                            getReviewsFromFirestore() //리뷰 작성 성공 시 리뷰 바로 가져오기
                        }
                        .addOnFailureListener {
                            // 리뷰 저장 실패 시 처리
                        }
                }
            }
    }


    private fun getLatitudeLongitude(location: String) {
        val geocoder = Geocoder(this)
        var endPoint: LatLng? = null

        // 도착지 주소를 위도와 경도로 변환
        val destinationAddresses: List<Address> =
            geocoder.getFromLocationName(location, 1)?.toList() ?: emptyList()
        if (destinationAddresses.isNotEmpty()) {
            endPoint = LatLng(destinationAddresses[0].latitude, destinationAddresses[0].longitude)
        }

        if (endPoint != null) {
            // 마커 추가
            mMap.addMarker(MarkerOptions().position(endPoint).title("Drama Location"))

            // 촬영지로 이동
            mMap.moveCamera(CameraUpdateFactory.newLatLng(endPoint))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15f)) // 줌 레벨 조정
        }
    }

    private fun getDirections(location: String) {
        val geocoder = Geocoder(this)
        var endPoint: LatLng? = null

        // 도착지 주소를 위도와 경도로 변환
        val destinationAddresses: List<Address> =
            geocoder.getFromLocationName(location, 1)?.toList() ?: emptyList()
        if (destinationAddresses.isNotEmpty()) {
            endPoint = LatLng(destinationAddresses[0].latitude, destinationAddresses[0].longitude)
        }

        if (endPoint != null) {
            val url = "nmap://route/public?" +
                    "&dlat=${endPoint.latitude}&dlng=${endPoint.longitude}&dname=${
                        Uri.encode(
                            location
                        )
                    }" +
                    "&appname=com.example.k_content_app"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.nhn.android.nmap")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                val marketIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=com.nhn.android.nmap")
                }
                startActivity(marketIntent)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 드라마 촬영지의 위도와 경도를 얻어와서 지도에 마커를 추가하고 해당 위치로 이동
        val dramaLocation = intent.getStringExtra("location")
        getLatitudeLongitude(dramaLocation!!)
    }

    private fun getReviewsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val location = intent.getStringExtra("location") ?: ""
        db.collection("reviews")
            .whereEqualTo("location", location)
            .orderBy("index", Query.Direction.DESCENDING) //최신 리뷰가 먼저 나오도록 (수정원하면 카톡하세요)
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING) // __name__ 필드를 내림차순으로 정렬
            .get()
            .addOnSuccessListener { result ->
                val reviews = mutableListOf<Review>()
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val displayName = document.getString("displayName") ?: ""
                    val img = document.getString("img") ?: "@drawable/userimg"
                    val location = document.getString("location") ?: ""

                    reviews.add(Review(displayName, title, content, img, location))
                }
                reviewAdapter.updateReviews(reviews)
            }
            .addOnFailureListener { exception ->
                // 오류 처리
            }
    }



    data class Review(
        val username: String,
        val title: String,
        val comment: String,
        val img: String,
        val location: String
    )

    class ReviewAdapter(private var reviews: MutableList<Review>) :
        RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

        fun updateReviews(newReviews: List<Review>) {
            reviews.clear()
            reviews.addAll(newReviews)
            notifyDataSetChanged()
        }

        class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
            val commentTextView: TextView = view.findViewById(R.id.commentTextView)
            val reviewTitle: TextView = view.findViewById(R.id.ReviewTitle)
            val userImageView: ImageView = view.findViewById(R.id.userprofileImage)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.review_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val review = reviews[position]
            holder.usernameTextView.text = review.username
            holder.reviewTitle.text = review.title
            holder.commentTextView.text = review.comment

            if (review.img != "@drawable/userimg") {
                Glide.with(holder.itemView.context)
                    .load(review.img)
                    .circleCrop() // 이미지 동그랗게 자르기
                    .into(holder.userImageView)
            } else {
                holder.userImageView.setImageResource(R.drawable.userimg)
            }
        }

        override fun getItemCount() = reviews.size
    }
}