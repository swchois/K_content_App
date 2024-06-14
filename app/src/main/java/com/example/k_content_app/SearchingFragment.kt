package com.example.k_content_app

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import java.io.File

class SearchingFragment : Fragment(), ImageModel.ImageSearchCallback {

    private lateinit var imageModel: ImageModel

    private var resView: TextView? = null
    private var imageBtn: ImageButton? = null
    private var imageView : ImageView? = null
    private var uploadChooser: UploadChooser? = null

    private val FILE_NAME = "picture.jpg"
    private lateinit var photoFile: File

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                // Handle gallery image
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    imageView?.setImageBitmap(bitmap)
                    imageModel.modelActivity(bitmap)
                    uploadChooser?.dismiss()
                    Log.d("ImageSearch", "Image loaded and processed successfully")
                } catch (e: Exception) {
                    Log.e("ImageSearch", "Error loading image", e)
                }
            } else {
                // Handle camera image
                try {
                    val photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, photoUri)
                    imageView?.setImageBitmap(bitmap)
                    imageModel.modelActivity(bitmap)
                    uploadChooser?.dismiss()
                    Log.d("ImageSearch", "Image captured and processed successfully")
                } catch (e: Exception) {
                    Log.e("ImageSearch", "Error loading image", e)
                }
            }
        } else {
            Log.e("ImageSearch", "Result not OK")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 레이아웃을 인플레이트 합니다.
        val view = inflater.inflate(R.layout.fragment_searching, container, false)
        imageView = view.findViewById(R.id.imageView6)
        resView = view.findViewById(R.id.searchcontent)
        imageBtn = view.findViewById(R.id.imageSearchBtn) // 여기 잠시


        // ImageModel 초기화
        imageModel = ImageModel(requireContext())
        imageModel.callback = this  // Set the callback

        // btn2가 imagebutton임을 확인하고 참조를 수정
        val btn2 = view.findViewById<ImageButton>(R.id.btn2)
        if (btn2 == null) {
            Log.e("SearchingFragment", "btn2 is null")
        } else {
            btn2.setOnClickListener {
                Log.d("SearchingFragment", "btn2 clicked")
                it.findNavController().navigate(R.id.action_searchingFragment_to_userInfoFragment)
            }
        // 이미지 검색 버튼
        val imageSearchButton = view.findViewById<Button>(R.id.imageSearchBtn)
        imageSearchButton.setOnClickListener {
            // 로그 추가
            Log.d("ImageSearch", "Click ImageSearch Button")
            // UploadChooser().show(parentFragmentManager, "UploadChooser")
            uploadChooser = UploadChooser().apply {
                addInterface(object : UploadChooser.UploadChooserInterface {
                    override fun cameraOnClick() {
                        Log.d("upload", "cameraOnClick")
                        openCamera()
                    }

                    override fun galleryOnClick() {
                        Log.d("upload", "galleryOnClick")
                        callImageSearch()
                    }
                })
            }
            uploadChooser!!.show(parentFragmentManager, "UploadChooser")
        }

        // UserInfoFragment로 이동하는 버튼
        view.findViewById<Button>(R.id.btn2).setOnClickListener {
            Log.d("Navigation", "Navigating to UserInfoFragment")
            it.findNavController().navigate(R.id.action_searchingFragment_to_userInfoFragment)
        }

        val searchButton = view.findViewById<ImageButton>(R.id.searchbtn)
        searchButton.setOnClickListener {
            val searchText = view.findViewById<EditText>(R.id.searchcontent).text.toString()
            Log.d("Search", "Search button clicked with text: $searchText")
            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra("searchText", searchText)
            startActivity(intent)
        }

        // 퀴즈설명으로 넘어가는 이미지 버튼
        val quizButton = view.findViewById<ImageButton>(R.id.quizButton)
        quizButton.setOnClickListener {
            val intent = Intent(activity, GameDescriptionActivity::class.java)
            startActivity(intent)
        }

        // 응모 or 당첨자 발표 버튼
        val applyOrAnnounceButton = view.findViewById<Button>(R.id.applyOrAnnounceButton)
        applyOrAnnounceButton.setOnClickListener {
            val intent = Intent(activity, ApplyActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onImageSearchResult(result: String) {
        Log.d("ImageSearch", "onImageSearchResult called with result: $result")
        resView?.text = result

        // 이미지 선택 후 검색 결과를 확인 하지 않고, 바로 다음 Step으로 넘기고 싶을 때
        // val searchText = result
        // val intent = Intent(activity, SearchActivity::class.java)
        // intent.putExtra("searchText", searchText)
        // startActivity(intent)
    }

    private fun openCamera() {
        photoFile = createCameraFile()
        val photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
        // 이후 카메라 인텐트를 사용한 코드 추가
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        imagePickerLauncher.launch(intent)
    }

    private fun createCameraFile(): File {
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    fun callImageSearch() {
        Log.d("ImageSearch", "callImageSearch called")
        imageSelect()
    }

    fun imageSelect() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        Log.d("ImageSearch", "Launching image selector")
        imagePickerLauncher.launch(intent)
    }
}
