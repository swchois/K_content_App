package com.example.k_content_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UploadChooser : BottomSheetDialogFragment()  {

    interface UploadChooserInterface{
        fun cameraOnClick()
        fun galleryOnClick()
    }
    var uploadChooserInterface : UploadChooserInterface? = null
    var cameraUploadBtn : TextView? = null
    var galleryUploadBtn : TextView? = null

    fun addInterface(listener: UploadChooserInterface){
        uploadChooserInterface = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.upload_chooser, container, false)
        cameraUploadBtn = view.findViewById(R.id.upload_camera)
        galleryUploadBtn = view.findViewById(R.id.upload_gallery)
       return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListener()
    }

    private fun setupListener()
    {
        cameraUploadBtn?.setOnClickListener {
            uploadChooserInterface?.cameraOnClick()
        }

        galleryUploadBtn?.setOnClickListener {
            uploadChooserInterface?.galleryOnClick()
        }
    }
}