package com.example.k_content_app.card_benner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.k_content_app.R

class CardFragment1 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 카드 1 레이아웃을 인플레이트
        return inflater.inflate(R.layout.card_1, container, false)
    }
}
