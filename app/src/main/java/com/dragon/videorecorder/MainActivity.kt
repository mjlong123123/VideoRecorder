package com.dragon.videorecorder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dragon.renderlib.background.RenderScope
import com.dragon.renderlib.camera.CameraHolder
import com.dragon.renderlib.egl.EGLCore
import com.dragon.renderlib.extension.MirrorType
import com.dragon.renderlib.node.NodesRender
import com.dragon.renderlib.node.OesTextureNode
import com.dragon.renderlib.texture.CombineSurfaceTexture
import com.dragon.videorecorder.ui.main.AboutDialog
import com.dragon.videorecorder.ui.main.DeviceMenu
import com.dragon.videorecorder.ui.main.IpAddressDialog
import com.dragon.videorecorder.ui.main.MainScreen
import com.dragon.videorecorder.ui.main.PortSettingDialog
import com.dragon.videorecorder.ui.main.SdpGenerateDialog
import com.dragon.videorecorder.ui.theme.VideoRecorderTheme
import com.dragon.videorecorder.utils.ToastUtils
import com.dragon.videorecorder.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    // 使用 720p 16:9 分辨率
    private val targetWidth = 720
    private val targetHeight = 1280
    
    private val nodesRender = NodesRender(targetWidth, targetHeight)
    private val eglRender = EGLRender(nodesRender)
    private var renderScope: RenderScope = RenderScope(eglRender)
    private var eglSurfaceHolder: EGLCore.EGLSurfaceHolder? = null

    private lateinit var cameraHolder: CameraHolder
    
    private val viewModel: MainViewModel by viewModels()

    private val recorder = VideoRecorder(targetWidth, targetHeight, { surface ->
        renderScope.addSurfaceHolder(EGLCore.EGLSurfaceHolder(surface, targetWidth.toFloat(), targetHeight.toFloat()))
    },
        { surface ->
            renderScope.removeSurfaceHolder(surface)
            surface.release()
        })

    // SDP 文件保存启动器
    private val saveSdpLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/sdp")
    ) { uri ->
        uri?.let {
            try {
                // 写入 SDP 文件内容
                val port = viewModel.rtpPort.value
                val sdpContent = """m=video $port RTP/AVP 96
a=rtpmap:96 H264
a=framerate:30"""
                
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(sdpContent.toByteArray())
                }
                ToastUtils.showSuccess(this, "SDP 文件已保存：mobile_${port}.sdp")
            } catch (e: Exception) {
                ToastUtils.showError(this, "保存 SDP 文件失败：${e.message}")
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            
        // 启用全面屏，设置为透明状态栏
        enableEdgeToEdge()
            
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
        // 不适配系统窗口边距，让内容延伸到全屏
        WindowCompat.setDecorFitsSystemWindows(window, false)
            
        // 隐藏状态栏和导航栏，并允许用户滑动拉出
        val windowInsetsController = WindowInsetsControllerCompat(
            window,          
            window.decorView 
        )
        windowInsetsController.hide(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
        )
        // 设置为临时显示模式（用户滑动后会自动消失）
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // 加载已保存的设备 IP 和当前 IP
        viewModel.loadIpAddress(this)
        viewModel.loadDevices(this)
        viewModel.loadRtpPort(this)
        cameraHolder = CameraHolder(this)
        nodesRender.runInRender {
            // 选择最接近 720p 16:9 的分辨率
            val size = select720pSize()
            Log.d(TAG, "Selected camera size: ${size.width}x${size.height}")
            val windowRotation = when ((this@MainActivity).windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }
            val cameraRotation = cameraHolder.sensorOrientation
            val rotation = cameraRotation - windowRotation
            val texture = CombineSurfaceTexture(
                size.width, size.height,
                rotation.toFloat(),
                if (cameraHolder.cameraId == CameraHolder.CAMERA_FRONT) MirrorType.VERTICAL_AND_HORIZONTAL else MirrorType.VERTICAL,
                { surface ->
                    cameraHolder.setSurface(surface).invalidate()
                }) {
                renderScope.requestRender()
            }
            val previewNode = OesTextureNode(
                0f,
                0f,
                nodesRender.width.toFloat(),
                nodesRender.height.toFloat(),
                texture
            )
            addNode(0, previewNode)
        }

        setContent {
            VideoRecorderTheme {
                val context = LocalContext.current
                val ipAddress by viewModel.ipAddress.collectAsState()
                val isRecording by viewModel.isRecording.collectAsState()
                val deviceIps by viewModel.deviceIps.collectAsState()
                val showDeviceMenu by viewModel.showDeviceMenu.collectAsState()
                val showIpDialog by viewModel.showIpDialog.collectAsState()
                val showPortDialog by viewModel.showPortDialog.collectAsState()
                val showSdpDialog by viewModel.showSdpDialog.collectAsState()
                val showAboutDialog by viewModel.showAboutDialog.collectAsState()
                val rtpPort by viewModel.rtpPort.collectAsState()
                
                // 创建 SurfaceHolder 回调
                val surfaceCallback = remember { createSurfaceCallback() }
                
                MainScreen(
                    onSettingsClick = {
                        viewModel.toggleDeviceMenu(true)
                    },
                    onRecordClick = {
                        handleRecordClick()
                    },
                    onSetPort = {
                        viewModel.showPortDialog(true)
                    },
                    onIpChanged = { newIp ->
                        viewModel.updateIpAddress(newIp)
                    },
                    isRecording = isRecording,
                    currentIp = ipAddress,
                    deviceIps = deviceIps,
                    surfaceHolderCallback = surfaceCallback,
                    currentPort = rtpPort
                )
                
                // 显示设备菜单
                if (showDeviceMenu) {
                    DeviceMenu(
                        deviceIps = deviceIps,
                        currentPort = rtpPort,
                        onScanDevice = { startScan() },
                        onAddDevice = { viewModel.showIpDialog(true) },
                        onSetPort = { viewModel.showPortDialog(true) },
                        onGenerateSdp = { viewModel.showSdpDialog(true) },
                        onDeviceClick = { ip ->
                            viewModel.selectDevice(ip, this@MainActivity)
                            viewModel.toggleDeviceMenu(false)
                        },
                        onDeleteDevice = { ip ->
                            viewModel.removeDevice(ip, this@MainActivity)
                        },
                        onAboutClick = {
                            viewModel.toggleDeviceMenu(false)
                            viewModel.showAboutDialog(true)
                        },
                        onDismiss = { viewModel.toggleDeviceMenu(false) },
                        enabled = !isRecording // 录制时禁用菜单
                    )
                }
                
                // 显示 IP 输入对话框
                if (showIpDialog) {
                    IpAddressDialog(
                        onDismiss = { viewModel.showIpDialog(false) },
                        onConfirm = { ip ->
                            viewModel.addDevice(ip, this)
                            viewModel.showIpDialog(false)
                        }
                    )
                }
                
                // 显示端口设置对话框
                if (showPortDialog) {
                    val port by viewModel.rtpPort.collectAsState()
                    PortSettingDialog(
                        onDismiss = { viewModel.showPortDialog(false) },
                        onConfirm = { port ->
                            if (viewModel.updateRtpPort(port, this)) {
                                viewModel.showPortDialog(false)
                            }
                        },
                        port
                    )
                }
                
                // 显示 SDP 生成确认对话框
                if (showSdpDialog) {
                    SdpGenerateDialog(
                        currentPort = rtpPort,
                        onDismiss = { viewModel.showSdpDialog(false) },
                        onConfirm = {
                            viewModel.showSdpDialog(false)
                            // 触发系统文件选择器，文件名：mobile_端口号.sdp
                            saveSdpLauncher.launch("mobile_${rtpPort}.sdp")
                        }
                    )
                }
                
                // 显示关于对话框
                if (showAboutDialog) {
                    AboutDialog(
                        onDismiss = { viewModel.showAboutDialog(false) }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        cameraHolder.startPreview().invalidate()
    }

    override fun onStop() {
        super.onStop()
        cameraHolder.stopPreview().invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHolder.release().invalidate()
        recorder.stopVideoEncoder()
    }
    
    /**
     * 创建 SurfaceHolder 回调
     */
    private fun createSurfaceCallback(): android.view.SurfaceHolder.Callback {
        return object : android.view.SurfaceHolder.Callback {
            override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                Log.d(TAG, "surfaceCreated")
            }

            override fun surfaceChanged(
                holder: android.view.SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d(TAG, "surfaceChanged")
                if (eglSurfaceHolder == null) {
                    eglSurfaceHolder = EGLCore.EGLSurfaceHolder(holder.surface, width.toFloat(), height.toFloat())
                    renderScope.addSurfaceHolder(eglSurfaceHolder)
                    renderScope.requestRender()
                }
            }

            override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                Log.d(TAG, "surfaceDestroyed")
                renderScope.removeSurfaceHolder(holder.surface)
                eglSurfaceHolder = null
            }
        }
    }
    
    /**
     * 处理录制按钮点击
     */
    private fun handleRecordClick() {
        if (recorder.isStarted) {
            recorder.stopVideoEncoder()
            viewModel.setRecording(false)
        } else {
            // 检查是否有已添加的 IP 地址
            val deviceIps = viewModel.getSelectedIps()
            if (deviceIps.isEmpty()) {
                ToastUtils.showError(this, "请先添加目的设备 IP 地址")
                return
            }
            
            // 获取当前设置的 RTP 端口
            val rtpPort = viewModel.rtpPort.value
            
            // 使用所有已添加的 IP 地址和配置的端口启动录制
            recorder.startVideoEncoder(deviceIps, rtpPort)
            viewModel.setRecording(true)
            ToastUtils.showSuccess(this, "开始录制，已发送到 ${deviceIps.size} 个设备，端口：$rtpPort")
        }
    }
    
    /**
     * 启动扫码
     */
    private fun startScan() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivityForResult(intent, REQUEST_SCAN)
    }

    /**
     * 选择最接近 720p (1280x720) 16:9 的分辨率
     * 优先选择精确匹配的 1280x720，如果没有则选择最接近的
     */
    private fun select720pSize(): android.util.Size {
        val sizes = cameraHolder.previewSizes
        if (sizes.isEmpty()) {
            Log.e(TAG, "No preview sizes available, using default 1280x720")
            return android.util.Size(1280, 720)
        }

        // 首先查找是否有精确的 1280x720
        val exact720p = sizes.find { it.width == 1280 && it.height == 720 }
        if (exact720p != null) {
            return exact720p
        }

        // 如果没有精确匹配，找宽高比最接近 16:9 (1.778) 且宽度>=1280 的
        val targetRatio = 16.0 / 9.0 // 1.778
        return sizes
            .filter { it.width >= 1280 } // 至少 720p 宽度
            .minByOrNull {
                val ratio = it.width.toDouble() / it.height.toDouble()
                val ratioDiff = kotlin.math.abs(ratio - targetRatio)
                // 综合考虑：分辨率不能太低，宽高比要接近 16:9
                ratioDiff * 1000 + (it.width * it.height).toDouble() / 10000
            } ?: sizes.first()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_SCAN = 1001
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCAN && resultCode == RESULT_OK) {
            val ip = data?.getStringExtra("ip")
            if (!ip.isNullOrEmpty()) {
                viewModel.addDevice(ip, this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 将权限请求结果传递给CameraHolder处理
        if (cameraHolder.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            // CameraHolder已经处理了这个权限请求
            return
        }
        // 可以在这里处理其他权限请求
    }
}