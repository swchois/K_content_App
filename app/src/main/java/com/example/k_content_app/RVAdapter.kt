import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.k_content_app.R
import com.example.k_content_app.SearchModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RVAdapter(
    val context: Context,
    val originalList: MutableList<SearchModel>,
    searchText: String? = ""
) : RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    private var filteredList: MutableList<SearchModel> = ArrayList()
    private var itemClickListener: ((SearchModel) -> Unit)? = null


    init {
        filteredList.addAll(originalList)
        if (searchText!!.isNotBlank()) {
            filter(searchText.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RVAdapter.ViewHolder, position: Int) {
        holder.bindItems(filteredList[position])
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: SearchModel) {
            val rv_img = itemView.findViewById<ImageView>(R.id.rvimgArea)
            val rv_title = itemView.findViewById<TextView>(R.id.rvtextArea)
            val rv_location = itemView.findViewById<TextView>(R.id.rvlocationArea)
            val rv_bookmark = itemView.findViewById<ImageButton>(R.id.bookmark_img)

            Glide.with(context)
                .load(item.imageUrl)
                .into(rv_img)

            rv_title.text = item.dramaTitle
            rv_location.text = item.location

            // 북마크 상태 확인하여 아이콘 설정
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("bookmark")
                    .whereEqualTo("location", item.location)
                    .whereEqualTo("userId", user.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            rv_bookmark.setImageResource(R.drawable.bookmarkon)  // 북마크된 아이콘
                        } else {
                            rv_bookmark.setImageResource(R.drawable.bookmark)  // 기본 아이콘
                        }
                    }
                    .addOnFailureListener {
                        rv_bookmark.setImageResource(R.drawable.bookmark)  // 기본 아이콘
                    }
            }

            rv_bookmark.setOnClickListener {
                addBookmark(item, rv_bookmark)
            }
        }


        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.invoke(filteredList[position])
                }
            }
        }
    }
    fun filter(text: String) {
        filteredList.clear()
        val searchText = text.toLowerCase().trim()
        if (text.isEmpty()) {
            filteredList.addAll(originalList)
        } else {
            originalList.forEach { item ->
                val titleContains = item.dramaTitle.toLowerCase().contains(searchText)
                val locationContains = item.location.toLowerCase().contains(searchText)
                if (titleContains || locationContains) {
                    filteredList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (SearchModel) -> Unit) {
        itemClickListener = listener
    }

    private fun addBookmark(item: SearchModel, bookmarkButton: ImageButton) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val bookmarkRef = db.collection("bookmark")
                .whereEqualTo("location", item.location)
                .whereEqualTo("userId", user.uid)

            bookmarkRef.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // 북마크가 존재하면 삭제
                        for (document in documents) {
                            db.collection("bookmark").document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "북마크가 해제되었습니다", Toast.LENGTH_SHORT).show()
                                    bookmarkButton.setImageResource(R.drawable.bookmark)  // 기본 아이콘으로 변경
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "북마크 해제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // 북마크가 존재하지 않으면 추가
                        val bookmark = hashMapOf(
                            "dramaTitle" to item.dramaTitle,
                            "imageUrl" to item.imageUrl,
                            "location" to item.location,
                            "userId" to user.uid
                        )
                        db.collection("bookmark")
                            .add(bookmark)
                            .addOnSuccessListener {
                                Toast.makeText(context, "북마크에 저장됨", Toast.LENGTH_SHORT).show()
                                bookmarkButton.setImageResource(R.drawable.bookmarkon)  // 북마크된 아이콘으로 변경
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "북마크 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "북마크 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }


    fun updateBookmarks(newBookmarks: List<SearchModel>) {
        originalList.clear()
        originalList.addAll(newBookmarks)
        filteredList.clear()
        filteredList.addAll(newBookmarks)
        notifyDataSetChanged()
    }
}
