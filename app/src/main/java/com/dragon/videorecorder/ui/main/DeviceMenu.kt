package com.dragon.videorecorder.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt

/**
 * 设备下拉菜单
 */
@Composable
@Preview
fun DeviceMenu(
    deviceIps: List<String> = emptyList(),
    currentPort: Int = 40018,
    onScanDevice: () -> Unit={},
    onAddDevice: () -> Unit={},
    onSetPort: () -> Unit={},
    onGenerateSdp: () -> Unit = {},
    onDeviceClick: (String) -> Unit={},
    onDeleteDevice: (String) -> Unit={},
    onAboutClick: () -> Unit = {},
    onDismiss: () -> Unit={},
    enabled: Boolean = true
) {
    /**
     * 获取设备的显示文本，格式为 IP:Port
     */
    fun getDeviceDisplayText(ip: String): String {
        return "$ip:$currentPort"
    }
    if (!enabled) {
        return
    }
    // 1. 获取当前设备的密度（核心：用于 dp 转 px）
    val density = LocalDensity.current
    val offsetX = with(density) { -50.dp.toPx().roundToInt() }
    val offsetY = with(density) { 140.dp.toPx().roundToInt() }
    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(offsetX, offsetY),
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(Color.Black.copy(alpha = 0.7f)),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                // 关于（放在第一项）
                MenuItem(
                    text = "关于",
                    onClick = {
                        onDismiss()
                        onAboutClick()
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )
                
                // 扫码添加设备
                MenuItem(
                    text = "扫码添加设备",
                    onClick = {
                        onDismiss()
                        onScanDevice()
                    }
                )
                
                // 添加目标设备
                MenuItem(
                    text = "添加目标设备",
                    onClick = {
                        onDismiss()
                        onAddDevice()
                    }
                )
                
                // 设置 RTP 端口
                MenuItem(
                    text = "设置端口 (当前：$currentPort)",
                    onClick = {
                        onDismiss()
                        onSetPort()
                    }
                )
                
                // 生成 SDP 文件
                MenuItem(
                    text = "生成 sdp 文件",
                    onClick = {
                        onDismiss()
                        onGenerateSdp()
                    }
                )
                
                // 分隔线（如果有设备列表）
                if (deviceIps.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    
                    // 已添加的设备列表
                    deviceIps.forEach { ip ->
                        DeviceItem(
                            ip = getDeviceDisplayText(ip),
                            onClick = {
                                onDismiss()
                                onDeviceClick(ip)
                            },
                            onDelete = {
                                onDeleteDevice(ip)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun DeviceItem(
    ip: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ip,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )
        
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
