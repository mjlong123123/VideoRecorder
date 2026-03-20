package com.dragon.videorecorder.ui.main

import android.view.SurfaceHolder
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.dragon.videorecorder.ui.theme.RecorderButtonIdle
import com.dragon.videorecorder.ui.theme.RecorderButtonRecording

/**
 * 主屏幕界面
 */
@Composable
@Preview
fun MainScreen(
    onSettingsClick: () -> Unit,
    onRecordClick: () -> Unit,
    onSetPort: () -> Unit,
    onIpChanged: (String) -> Unit,
    isRecording: Boolean = false,
    currentIp: String = "",
    deviceIps: List<String> = emptyList(),
    surfaceHolderCallback: SurfaceHolder.Callback? = null,
    currentPort: Int = 40018
) {
    val context = LocalContext.current

    // 录制按钮的脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "recording pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            // SurfaceView 引用
            val (surfaceView, settingsButton, recordButton, statusIndicator) = createRefs()

            // SurfaceView - 使用 AndroidView 包装
            AndroidView(
                factory = { ctx ->
                    android.view.SurfaceView(ctx).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) {
                                surfaceHolderCallback?.surfaceCreated(holder)
                            }

                            override fun surfaceChanged(
                                holder: SurfaceHolder,
                                format: Int,
                                width: Int,
                                height: Int
                            ) {
                                surfaceHolderCallback?.surfaceChanged(holder, format, width, height)
                            }

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                surfaceHolderCallback?.surfaceDestroyed(holder)
                            }
                        })
                    }
                },
                modifier = Modifier
                    .constrainAs(surfaceView) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                    .aspectRatio(9f / 16f)
            )


            // 设置按钮 - 右上角，优化视觉效果
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .constrainAs(settingsButton) {
                        top.linkTo(surfaceView.top, margin = 32.dp)
                        end.linkTo(surfaceView.end, margin = 32.dp)
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                    .size(44.dp) // 增大到 44dp，更符合触摸友好原则
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 状态指示器 - 显示已连接的设备 IP
            if (deviceIps.isNotEmpty()) {
                StatusIndicator(
                    deviceIps = deviceIps,
                    isRecording = isRecording,
                    currentPort = currentPort,
                    modifier = Modifier.constrainAs(statusIndicator) {
                        bottom.linkTo(recordButton.top, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
            }

            // 录制按钮 - 水平居中，优化样式
            Box(
                modifier = Modifier
                    .constrainAs(recordButton) {
                        bottom.linkTo(surfaceView.bottom, margin = 48.dp) // 增加底部间距
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) RecorderButtonRecording else RecorderButtonIdle
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRecordClick,
                        role = Role.Button
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 录制状态指示器（内部圆形或方形）
                Box(
                    modifier = Modifier
                        .size(if (isRecording) 44.dp else 52.dp)
                        .clip(
                            if (isRecording) {
                                RoundedCornerShape(8.dp) // 录制中显示圆角方形
                            } else {
                                CircleShape // 待机时显示圆形
                            }
                        )
                        .background(
                            Color.White.copy(
                                alpha = if (isRecording) pulseAlpha else 0.3f // 录制中脉冲效果
                            )
                        )
                )
            }
        }
    }
}

/**
 * 录制状态指示器 - 显示已连接的设备 IP
 */
@Composable
private fun StatusIndicator(
    deviceIps: List<String>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    currentPort: Int = 40018
) {
    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 录制状态图标和文字
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 信号图标（使用圆点代替）
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) Color(0xFF34C759) else Color(0xFF0A84FF)
                        )
                )

                Text(
                    text = if (isRecording) "录制中" else "已连接",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 显示 IP:Port 列表（最多显示 3 个）
            Spacer(modifier = Modifier.height(8.dp))
            deviceIps.take(3).forEachIndexed { index, ip ->
                Text(
                    text = "• $ip:$currentPort",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // 如果超过 3 个，显示省略号
            if (deviceIps.size > 3) {
                Text(
                    text = "+ ${deviceIps.size - 3} 更多",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
