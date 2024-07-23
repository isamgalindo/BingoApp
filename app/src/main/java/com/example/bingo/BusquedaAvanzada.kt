package com.example.bingo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.lang.Error

class BusquedaAvanzada : AppCompatActivity() {
    private lateinit var buttonSubirFoto: Button
    private lateinit var buttonTomarFoto: Button
    private lateinit var imageView: ImageView
    private lateinit var responseTextView: TextView
    private lateinit var choice: String
    private var imageBitmap: Bitmap? = null
    companion object {
        val IMAGE_REQUEST_CODE = 1000;
        const val SERVER_URL = " http://10.0.2.2:8000/predict/"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_busqueda_avanzada)

        responseTextView = findViewById(R.id.responseTextView)

        buttonSubirFoto = findViewById(R.id.subirFoto)
        imageView = findViewById(R.id.foto)

        buttonSubirFoto.setOnClickListener {
            choice = "subir"
            pickImageGallery()
        }
        buttonTomarFoto = findViewById(R.id.tomarFoto)
        buttonTomarFoto.setOnClickListener {
            choice = "tomar"
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, IMAGE_REQUEST_CODE)
            } catch (e: ActivityNotFoundException){
                Toast.makeText(this, "Error: " + e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonSendToServer: Button = findViewById(R.id.mandarFoto)
        buttonSendToServer.setOnClickListener {
            imageBitmap?.let { bitmap ->
                uploadImage(bitmap)
            } ?: Toast.makeText(this, "No image to send!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (choice == "tomar") {
                imageBitmap = data?.extras?.get("data") as Bitmap
            } else if (choice == "subir") {
                val imageUri: Uri? = data?.data
                if (imageUri == null){
                    Toast.makeText(this, "Null", Toast.LENGTH_LONG).show()
                } else {
                    try {
                        imageUri?.let { uri ->
                            contentResolver.openInputStream(uri).use { inputStream ->
                                imageBitmap = BitmapFactory.decodeStream(inputStream)
                                imageView.setImageBitmap(imageBitmap)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Error getting selected file: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            imageView.setImageBitmap(imageBitmap)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }


    }
    private fun uploadImage(imageBitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        // Create a request body with the image and content type
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)

        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = "image.jpg",
            body = requestBody
        )
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM) // This is important to note the type of the request
            .addPart(filePart) // Add the file part to the body
            .build()

        // Build the request
        val request = Request.Builder()
            .url(SERVER_URL)
            .post(multipartBody)
            .build()

        // Instantiate the OkHttpClient
        val client = OkHttpClient()

        // Asynchronously execute the HTTP request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        val gson = Gson()
                        val result = gson.fromJson(responseBody, ApiResponse::class.java)

                        if (result.predictions.isEmpty()) {
                            responseTextView.text = "Vuelve a intentar. Asegúrate de que la foto esté iluminada y que no sea borrosa."
                        } else {
                            val label = result.predictions[0].label
                            responseTextView.text = label
                        }
                    }
                } else {
                    runOnUiThread {
                        responseTextView.text = "Failed to get the response"
                    }
                }
            }
        })
    }
}