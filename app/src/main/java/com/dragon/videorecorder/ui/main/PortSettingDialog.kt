package com.dragon.videorecorder.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dragon.videorecorder.config.PortConfig

/**
 * RTP 端口设置对话框 - 深色主题
 */
@Composable
fun PortSettingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    initialValue: Int = PortConfig.DEFAULT_RTP_PORT
) {
    var portText by remember { mutableStateOf(initialValue.toString()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 实时验证输入
    fun validateInput(text: String): Boolean {
        if (text.isBlank()) {
            return true // 空输入不显示错误，让用户继续编辑
        }
        
        val port = text.toIntOrNull()
        if (port == null) {
            errorMessage = "请输入有效的数字"
            return false
        }
        
        if (!PortConfig.isValidPort(port)) {
            errorMessage = "端口必须在 ${PortConfig.MIN_PORT} 到 ${PortConfig.MAX_PORT} 之间"
            return false
        }
        
        showError = false
        return true
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "设置 RTP 端口",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 端口规则说明
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Blue.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "提示",
                            tint = Color.Blue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "端口范围：${PortConfig.MIN_PORT} - ${PortConfig.MAX_PORT}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "推荐使用 ${PortConfig.RECOMMENDED_MIN_PORT} 以上的偶数端口",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "RTP 使用端口 n，RTCP 使用端口 n+1",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = portText,
                    onValueChange = { 
                        portText = it
                        validateInput(it)
                    },
                    label = { Text("RTP 端口") },
                    placeholder = { Text(PortConfig.DEFAULT_RTP_PORT.toString()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = !validateInput(portText) && portText.isNotBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        errorTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = if (!validateInput(portText) && portText.isNotBlank()) 
                            Color(0xFFFF5252) 
                        else 
                            Color.White.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = Color.White.copy(alpha = 0.7f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                
                // 错误提示
                if (!validateInput(portText) && portText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFF5252).copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "错误",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF8A80),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val port = portText.toIntOrNull()
                            if (port != null && PortConfig.isValidPort(port)) {
                                onConfirm(port)
                            }
                        },
                        enabled = portText.toIntOrNull()?.let { PortConfig.isValidPort(it) } == true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (portText.toIntOrNull()?.let { PortConfig.isValidPort(it) } == true) 
                                Color.White.copy(alpha = 0.9f) 
                            else 
                                Color.Gray.copy(alpha = 0.5f),
                            contentColor = Color.Black,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}
