package com.example.k_content_app

import RVAdapter
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UserInfoFragment : Fragment() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var bookmarkAdapter: RVAdapter
    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_info, container, false)

        // Initialize Firebase Authentication
        auth = Firebase.auth

        // Initialize RecyclerView and Adapters
        userRecyclerView = view.findViewById(R.id.userReviewList)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        reviewAdapter = ReviewAdapter(mutableListOf())
        bookmarkAdapter = RVAdapter(requireContext(), mutableListOf())

        // Set up click listeners
        view.findViewById<LinearLayout>(R.id.manage_bookmark)?.setOnClickListener {
            displayBookmarks()
        }

        view.findViewById<LinearLayout>(R.id.manage_review)?.setOnClickListener {
            displayReviews()
        }

        view.findViewById<LinearLayout>(R.id.enrollInfo)?.setOnClickListener {
            showUserInfoDialog()
        }

        view.findViewById<Button>(R.id.uploadBtn)?.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }

        view.findViewById<Button>(R.id.btn1)?.setOnClickListener {
            it.findNavController().navigate(R.id.action_userInfoFragment_to_searchingFragment)
        }

        // Display default view
        displayBookmarks()

        // Check if user is authenticated
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Set user information
            view.findViewById<TextView>(R.id.userid)?.text = "@" + currentUser.uid
            view.findViewById<TextView>(R.id.username)?.text = currentUser.displayName

            // Load user profile image
            setUserProfileImage(view.findViewById(R.id.img_user))

            // Load user cash
            setUserCash(view.findViewById(R.id.user_cash))
        } else {
            Log.e("UserInfoFragment", "User is not authenticated.")
        }

        return view
    }

    private fun displayBookmarks() {
        userRecyclerView.adapter = bookmarkAdapter
        getBookmarksForCurrentUser()

        bookmarkAdapter.setOnItemClickListener { selectedItem ->
            val intent = Intent(requireContext(), DramaDetailActivity::class.java).apply {
                putExtra("image", selectedItem.imageUrl)
                putExtra("title", selectedItem.dramaTitle)
                putExtra("location", selectedItem.location)
            }
            startActivity(intent)
        }
    }

    private fun displayReviews() {
        userRecyclerView.adapter = reviewAdapter
        getReviewsForCurrentUser()
    }

    private fun showUserInfoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.customdialog, null)
        builder.setView(dialogView)

        val userIdTextView = dialogView.findViewById<TextView>(R.id.userinfoArea)
        val userEmailTextView = dialogView.findViewById<TextView>(R.id.useremailArea)
        val userDisplayTextView = dialogView.findViewById<TextView>(R.id.userDisplayArea)

        val user = Firebase.auth.currentUser
        userIdTextView.text = "사용자 UID: ${user?.uid}"
        userEmailTextView.text = "사용자 이메일: ${user?.email}"
        userDisplayTextView.text = "사용자 디스플레이: ${user?.displayName}"

        val dialog = builder.create()
        dialog.show()
    }

    private fun getReviewsForCurrentUser() {
        val currentUserUid = Firebase.auth.currentUser?.uid
        if (currentUserUid != null) {
            db.collection("reviews")
                .whereEqualTo("uid", currentUserUid)
                .get()
                .addOnSuccessListener { documents ->
                    val reviews = mutableListOf<Review>()
                    for (document in documents) {
                        val title = document.getString("title") ?: ""
                        val content = document.getString("content") ?: ""
                        val displayName = document.getString("displayName") ?: ""
                        val img = document.getString("img") ?: "@drawable/userimg"
                        reviews.add(Review(displayName, title, content, img))
                    }
                    reviewAdapter.updateReviews(reviews)
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
    }

    private fun getBookmarksForCurrentUser() {
        val currentUserUid = Firebase.auth.currentUser?.uid
        if (currentUserUid != null) {
            db.collection("bookmark")
                .whereEqualTo("userId", currentUserUid)
                .get()
                .addOnSuccessListener { documents ->
                    val bookmarks = mutableListOf<SearchModel>()
                    for (document in documents) {
                        val dramaTitle = document.getString("dramaTitle") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""
                        val location = document.getString("location") ?: ""
                        bookmarks.add(SearchModel(imageUrl, dramaTitle, location))
                    }
                    bookmarkAdapter.updateBookmarks(bookmarks)
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
    }

    private fun setUserProfileImage(imageView: ImageView?) {
        val currentUserUid = Firebase.auth.currentUser?.uid
        if (currentUserUid != null) {
            db.collection("users").document(currentUserUid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val imageUrl = document.getString("img")
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            imageView?.let {
                                Glide.with(this)
                                    .load(imageUrl)
                                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                    .into(it)
                            }
                        } else {
                            imageView?.setImageResource(R.drawable.userimg)
                        }
                    } else {
                        imageView?.setImageResource(R.drawable.userimg)
                    }
                }
                .addOnFailureListener { exception ->
                    imageView?.setImageResource(R.drawable.userimg)
                }
        } else {
            imageView?.setImageResource(R.drawable.userimg)
        }
    }

    private fun setUserCash(textView: TextView?) {
        val currentUserUid = Firebase.auth.currentUser?.uid
        if (currentUserUid != null) {
            db.collection("users").document(currentUserUid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val cash = document.getLong("cash") ?: -1
                        textView?.text = cash.toString()
                    }
                }
                .addOnFailureListener { exception ->
                    textView?.text = "error"
                }
        } else {
            textView?.text = "error"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            AlertDialog.Builder(requireContext())
                .setTitle("프로필 사진 설정")
                .setMessage("선택하신 사진으로 프로필로 설정 하시겠습니까?")
                .setPositiveButton("Yes") { dialog, which ->
                    uploadImage()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref =
                storage.reference.child("images/${auth.currentUser!!.uid}/${System.currentTimeMillis()}")
            val uploadTask = ref.putFile(filePath!!)

            uploadTask.addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    db.collection("users").document(auth.currentUser!!.uid)
                        .update("img", imageUrl)
                        .addOnSuccessListener {
                            setUserProfileImage(requireView().findViewById(R.id.img_user))
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to save image URL
                        }
                }
            }.addOnFailureListener { e ->
                // Handle upload failure
            }
        }
    }

    data class Review(
        val displayName: String,
        val title: String,
        val content: String,
        val img: String
    )

    class ReviewAdapter(private val reviews: MutableList<Review>) :
        RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

        class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
            val reviewTitleTextView: TextView = itemView.findViewById(R.id.ReviewTitle)
            val commentTextView: TextView = itemView.findViewById(R.id.commentTextView)
            val userImageView: ImageView = itemView.findViewById(R.id.userprofileImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
            return ReviewViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            val review = reviews[position]
            holder.usernameTextView.text = review.displayName
            holder.reviewTitleTextView.text = review.title
            holder.commentTextView.text = review.content
            if (review.img != "@drawable/userimg") {
                Glide.with(holder.itemView.context)
                    .load(review.img)
                    .circleCrop()
                    .into(holder.userImageView)
            } else {
                holder.userImageView.setImageResource(R.drawable.userimg)
            }
        }

        override fun getItemCount(): Int {
            return reviews.size
        }

        fun updateReviews(newReviews: List<Review>) {
            reviews.clear()
            reviews.addAll(newReviews)
            notifyDataSetChanged()
        }
    }
}
