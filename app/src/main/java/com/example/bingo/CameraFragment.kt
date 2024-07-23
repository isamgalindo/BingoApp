package com.example.bingo

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.concurrent.Executors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.io.File
import kotlin.math.abs

class CameraFragment : Fragment() {
    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
        private const val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1002
        private const val REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 1003

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        viewFinder = view.findViewById(R.id.viewFinder)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissionsAndStartCamera()
        view.findViewById<ImageButton>(R.id.switch_camera_button).setOnClickListener {
            toggleCamera()
        }

    }

    private fun checkPermissionsAndStartCamera() {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
        val writePermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)

        val permissionsToRequest = mutableListOf<String>()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), REQUEST_CAMERA_PERMISSION)
        } else {
            startCamera()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    checkPermissionsAndStartCamera()
                } else {
                }
            }
        }
    }



    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }



    private fun isPoseMatchingSilhouette(pose: Pose): Boolean {
        val viewFinderWidth = viewFinder.width
        val viewFinderHeight = viewFinder.height

        val expectedNosePosition = Pair(viewFinderWidth * 0.50, viewFinderHeight * 0.30) // 50% width, 30% height
        val expectedLeftHandPosition = Pair(viewFinderWidth * 0.30, viewFinderHeight * 0.40) // 30% width, 40% height
        val expectedRightHandPosition = Pair(viewFinderWidth * 0.70, viewFinderHeight * 0.40) // 70% width, 40% height

        val tolerance = 0.05 // 5% tolerance

        return pose.allPoseLandmarks.all { landmark ->
            when (landmark.landmarkType) {
                PoseLandmark.NOSE -> isWithinTolerance(landmark, expectedNosePosition, tolerance)
                PoseLandmark.LEFT_WRIST -> isWithinTolerance(landmark, expectedLeftHandPosition, tolerance)
                PoseLandmark.RIGHT_WRIST -> isWithinTolerance(landmark, expectedRightHandPosition, tolerance)
                else -> true
            }
        }
    }

    private fun isWithinTolerance(landmark: PoseLandmark, expectedPosition: Pair<Double, Double>, tolerance: Double): Boolean {
        val posX = landmark.position.x
        val posY = landmark.position.y
        val expectedX = expectedPosition.first
        val expectedY = expectedPosition.second

        return abs(posX / viewFinder.width - expectedX) < tolerance && abs(posY / viewFinder.height - expectedY) < tolerance
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            requireContext().externalMediaDirs.first(),
            "photo-${System.currentTimeMillis()}.jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                override fun onError(exc: ImageCaptureException) {
                    val msg = "Photo capture failed: ${exc.message}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    @OptIn(ExperimentalGetImage::class) private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            // Setup pose detector
            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
            val poseDetector = PoseDetection.getClient(options)

            val analysisUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, rotationDegrees)
                        poseDetector.process(inputImage)
                            .addOnSuccessListener { pose ->
                                // Check if the pose matches the silhouette
                                if (isPoseMatchingSilhouette(pose)) {
                                    takePhoto()
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysisUseCase)
            } catch (exc: Exception) {
                Toast.makeText(context, "Failed to start camera: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))}}


