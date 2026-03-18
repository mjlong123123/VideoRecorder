package com.dragon.videorecorder.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 扫码界面
 */
@Composable
fun ScanScreen(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    // 请求权限
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            // 需要 Activity 来请求权限，这里简化处理
            // 实际应用中应该使用 Accompanist Permissions
        }
    }
    
    if (!hasPermission) {
        // 显示权限请求提示
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "需要相机权限才能扫码",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // 初始化相机提供者
    LaunchedEffect(Unit) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // 初始化相机
    LaunchedEffect(cameraProvider) {
        cameraProvider?.let { provider ->
            previewView?.let { view ->
                // 设置预览
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }
                
                // 设置图像分析（用于条码扫描）
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(imageProxy, barcodeScanner, onBarcodeScanned)
                        }
                    }
                
                // 选择后置摄像头
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // 绑定生命周期
                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    // 预览视图
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = this
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            // 可以在这里更新预览视图
        }
    )
}

private fun processImageProxy(
    imageProxy: androidx.camera.core.ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onBarcodeScanned: (String) -> Unit
) {
    try {
        val mediaImage = imageProxy.image ?: return
        
        // 将 YUV 格式转换为 Bitmap
        val bitmap = yuvToBitmap(mediaImage)
        
        // 使用 ML Kit 进行条码识别
        val image = InputImage.fromBitmap(bitmap, 0)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val value = barcode.rawValue
                    if (!value.isNullOrBlank()) {
                        // 找到条码，返回结果
                        onBarcodeScanned(value.trim())
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnFailureListener { e ->
                // 扫码失败
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } catch (e: Exception) {
        imageProxy.close()
    }
}

private fun yuvToBitmap(mediaImage: android.media.Image): android.graphics.Bitmap {
    val width = mediaImage.width
    val height = mediaImage.height
    val planes = mediaImage.planes
    
    // 获取 Y、U、V 数据
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V
    
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    
    val nv21 = ByteArray(ySize + uSize + vSize)
    
    // 复制 Y 数据
    yBuffer.get(nv21, 0, ySize)
    // 复制 V 数据（NV21 中是 VU 顺序）
    vBuffer.get(nv21, ySize, vSize)
    // 复制 U 数据
    uBuffer.get(nv21, ySize + vSize, uSize)
    
    // 创建 YUV 图像
    val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
    val outputStream = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 80, outputStream)
    
    // 解码为 Bitmap
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
    
    // 旋转图片以匹配实际显示方向
    return rotateBitmap(bitmap, 90f)
}

private fun rotateBitmap(bitmap: android.graphics.Bitmap, degrees: Float): android.graphics.Bitmap {
    val matrix = android.graphics.Matrix()
    matrix.postRotate(degrees)
    return android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
