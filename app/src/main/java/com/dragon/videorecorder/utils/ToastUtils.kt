package com.dragon.videorecorder.utils

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

/**
 * 深色主题 Toast 工具类
 */
object ToastUtils {
    
    /**
     * 显示成功提示（绿色）
     */
    fun showSuccess(context: Context, message: String) {
        showToast(context, message, Color.parseColor("#4CAF50"))
    }
    
    /**
     * 显示错误提示（红色）
     */
    fun showError(context: Context, message: String) {
        showToast(context, message, Color.parseColor("#F44336"))
    }
    
    /**
     * 显示信息提示（蓝色）
     */
    fun showInfo(context: Context, message: String) {
        showToast(context, message, Color.parseColor("#2196F3"))
    }
    
    /**
     * 显示警告提示（橙色）
     */
    fun showWarning(context: Context, message: String) {
        showToast(context, message, Color.parseColor("#FF9800"))
    }
    
    /**
     * 自定义 Toast，使用深色背景和白色文字
     */
    private fun showToast(context: Context, message: String, backgroundColor: Int) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        
        // 获取 Toast 的 View
        val toastView = toast.view
        
        // 设置深色背景
        toastView?.setBackgroundColor(Color.parseColor("#333333"))
        
        // 设置内边距
        val horizontalPadding = toastView?.paddingLeft ?: 0
        val verticalPadding = toastView?.paddingTop ?: 0
        toastView?.setPadding(horizontalPadding + 20, verticalPadding + 20, horizontalPadding + 20, verticalPadding + 20)
        
        // 设置位置（底部居中）
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 200)
        
        toast.show()
    }
}
