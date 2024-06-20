package com.example.k_content_app.card_benner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.k_content_app.R
import com.example.k_content_app.card_benner.card_benner.CardData

class CardAdapter(private val context: Context, private val cardDataList: List<CardData>) :
    RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = cardDataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardData = cardDataList[position]
        holder.bind(cardData)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.card_image)
        private val titleView: TextView = itemView.findViewById(R.id.card_title)

        fun bind(cardData: CardData) {
            imageView.setImageResource(cardData.imageResId)
            titleView.text = cardData.title
        }
    }
}
