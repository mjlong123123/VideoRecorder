package com.dragon.videorecorder.ui.main

import android.view.SurfaceHolder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    surfaceHolderCallback: SurfaceHolder.Callback? = null
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            // SurfaceView 引用
            val (surfaceView, settingsButton, recordButton) = createRefs()

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


            // 设置按钮 - 右上角
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .constrainAs(settingsButton) {
                        top.linkTo(surfaceView.top, margin = 32.dp)
                        end.linkTo(surfaceView.end, margin = 32.dp)
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 录制按钮 - 水平居中
            Box(
                modifier = Modifier
                    .constrainAs(recordButton) {
                        bottom.linkTo(surfaceView.bottom, margin = 32.dp)
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
                        onClick = onRecordClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 录制状态指示器（内部小圆圈）
                Box(
                    modifier = Modifier
                        .size(if (isRecording) 44.dp else 52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}
