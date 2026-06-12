package com.example.a220960_sirnelson_lab01

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack  // ← fixed deprecated
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner  // ← fixed deprecated
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCamPermission) {
            CameraContent(navController)
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission required", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CameraContent(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uriHandler = LocalUriHandler.current

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    var scannedText by remember { mutableStateOf("") }
    var isEditingText by remember { mutableStateOf(false) }
    var lastScannedQr by remember { mutableStateOf("") }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    LaunchedEffect(previewView) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            @OptIn(ExperimentalGetImage::class)
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            val rawValue = barcode.rawValue ?: ""
                            if (rawValue.isNotEmpty() && rawValue != lastScannedQr) {
                                lastScannedQr = rawValue
                                Toast.makeText(context, "QR Detected: $rawValue", Toast.LENGTH_SHORT).show()
                                if (rawValue.startsWith("http://") || rawValue.startsWith("https://")) {
                                    uriHandler.openUri(rawValue)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { Log.e("CameraScreen", "QR Scan error", it) }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("CameraScreen", "Binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(Color.Black.copy(0.5f), RoundedCornerShape(50))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "SCANNER (QR/TEXT)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.Center)
                .background(Color.Transparent)
        ) {
            Text(
                text = "Align QR Code Here",
                color = Color.Yellow.copy(0.8f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Tap button to capture and convert text",
                color = Color.White.copy(0.7f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                @OptIn(ExperimentalGetImage::class)
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    textRecognizer.process(image)
                                        .addOnSuccessListener { visionText ->
                                            if (visionText.text.isNotEmpty()) {
                                                scannedText = visionText.text
                                                isEditingText = true
                                            } else {
                                                Toast.makeText(context, "No text found!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to read text", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B72C)),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.size(70.dp)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Capture Text",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        if (isEditingText) {
            AlertDialog(
                onDismissRequest = { isEditingText = false },
                title = { Text("Extracted Text Editor") },
                text = {
                    OutlinedTextField(
                        value = scannedText,
                        onValueChange = { scannedText = it },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.Black)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isEditingText = false
                            Toast.makeText(context, "Text Saved/Updated!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B72C))
                    ) {
                        Text("Copy / Done", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditingText = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}