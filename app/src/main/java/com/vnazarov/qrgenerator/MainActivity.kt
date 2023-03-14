package com.vnazarov.qrgenerator

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import coil.load
import com.vnazarov.qrgenerator.databinding.ActivityMainBinding
import io.github.g0dkar.qrcode.QRCode
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.generateButton.setOnClickListener {
            if (mBinding.enterText.text.isNotEmpty()) {
                generateQR(mBinding.enterText.text.toString())
            } else Toast.makeText(this, "I can't generate QR from nothing :(", Toast.LENGTH_SHORT)
                .show()
        }

        mBinding.downloadButton.setOnClickListener {
            val bitmap = getImageFromView(mBinding.generatedQr)
            if (bitmap != null){
                saveToStorage(bitmap)
            }
        }
    }

    private fun generateQR(string: String) {

        mBinding.generatedQr.load(QRCode(string).render().nativeImage() as Bitmap?)
        mBinding.generatedQr.setPadding(5, 5, 5, 5)
        mBinding.generatedQr.scaleType = ImageView.ScaleType.CENTER_INSIDE
        mBinding.generatedQr.setBackgroundColor(Color.WHITE)

        if (mBinding.generatedQr.isEnabled) {
            mBinding.downloadButton.visibility = View.VISIBLE
        }

    }

    private fun getImageFromView(generatedQr: ImageView): Bitmap? {
        var image: Bitmap? = null

        try {
            image = Bitmap.createBitmap(generatedQr.measuredWidth, generatedQr.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)

            generatedQr.draw(canvas)
        } catch (e: Exception){
            Log.d("Exception", e.message.toString())
        }

        return image
    }

    private fun saveToStorage(bitmap: Bitmap) {

        val imageName = "qr_generator_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            this.contentResolver?.also {resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        }
        else {
            val imagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDirectory, imageName)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Successfully downloaded", Toast.LENGTH_SHORT).show()
        }
    }
}