package com.dragon.videorecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dragon.videorecorder.ui.scan.ScanScreen
import com.dragon.videorecorder.ui.theme.VideoRecorderTheme
import androidx.core.content.ContextCompat

/**
 * 扫码 Activity - 使用 Jetpack Compose
 */
class ScanActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予
        } else {
            // 权限被拒绝
            setResult(RESULT_CANCELED)
            finish()
        }
    }
    
    companion object {
        private const val TAG = "ScanActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            VideoRecorderTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    ScanScreen(
                        onBarcodeScanned = { ip ->
                            onBarcodeScanned(ip)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // 检查权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    private fun onBarcodeScanned(ip: String) {
        // 返回扫码结果给 MainActivity
        val resultIntent = Intent().apply {
            putExtra("ip", ip)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
