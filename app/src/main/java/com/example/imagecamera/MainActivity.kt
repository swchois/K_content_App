package com.example.imagecamera
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.imagecamera.R
// import app.ij.mlwithtensorflowlite.ml.Model

import com.example.imagecamera.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var camera: Button
    private lateinit var gallery: Button
    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private val imageSize = 32

    lateinit var labels_list: List<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Load labels list from labels_mobilenet_dataset file in assets folder
        try{
            val labels_reader= BufferedReader(InputStreamReader(application.assets.open(("dataset.txt"))))
            labels_list= labels_reader.readLines()

            Log.d(TAG, "labels list="+labels_list.get(0))
        }catch(e: Exception){
            Log.d(TAG, "exception in reading labels from file: "+e)
        }

        camera = findViewById(R.id.button)
        gallery = findViewById(R.id.button2)
        result = findViewById(R.id.result)
        imageView = findViewById(R.id.imageView)

        camera.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 3)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            }
        }
        gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }
    }

    private fun classifyImage(image: Bitmap) {
        //paste the tflite model code here
        try{
            val model = Model.newInstance(this)

            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.UINT8)

            //create bytebuffer image from the resized bitmap
            var byteBuffer= TensorImage.fromBitmap(image).buffer
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            //show output in textView of layout file
            //outputFeature0 array has probabilities of 1000 values
            var maxProbabilityIndex:Int= getMaxProbabilityIndex(outputFeature0.floatArray)
            Log.d(TAG, "returned maxProbabilityIndex="+maxProbabilityIndex)

            var resulttxt= "item name: "+labels_list[maxProbabilityIndex]+", probability: "+outputFeature0.floatArray[maxProbabilityIndex]
            result.text= resulttxt



            // Releases model resources if no longer used.
            model.close()
        }catch(e: Exception){
            Log.d(TAG, "exception in using model for predictions: "+e)
        }
    }

    fun getMaxProbabilityIndex(arr: FloatArray): Int {
        var max_val: Float= 0.0F
        var max_val_index:Int= -1
        var items_count_max_probalities:Int=0

        for(i in 0..4) {
            //find max probability item index
            if(arr[i]>max_val) {
                max_val_index=i
                max_val=arr[i]
            }
            //find no. of items with and above 100 probability
            if(arr[i]>=100){
                items_count_max_probalities++
            }
        }
        Log.d(TAG, "no. of items with probability >=100 are "+items_count_max_probalities)
        return max_val_index
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                3 -> {
                    val image = data?.extras?.get("data") as? Bitmap ?: return
                    val dimension = Math.min(image.width, image.height)
                    val thumbImage = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
                    imageView.setImageBitmap(thumbImage)

                    val scaledImage = Bitmap.createScaledBitmap(thumbImage, imageSize, imageSize, false)
                    classifyImage(scaledImage)
                }
                1 -> {
                    val uri = data?.data ?: return
                    val image = try {
                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return
                    }
                    imageView.setImageBitmap(image)

                    val scaledImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
                    classifyImage(scaledImage)
                }
            }
        }
    }
}
