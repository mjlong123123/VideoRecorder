package com.dragon.videorecorder.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import com.dragon.renderlib.background.RenderScope
import com.dragon.videorecorder.config.PortConfig
import com.dragon.videorecorder.utils.IpPortValidator
import com.dragon.videorecorder.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 主界面 ViewModel
 */
class MainViewModel : ViewModel() {
    
    // UI 状态
    private val _ipAddress = MutableStateFlow("")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _deviceIps = MutableStateFlow(emptyList<String>())
    val deviceIps: StateFlow<List<String>> = _deviceIps.asStateFlow()
    
    // 菜单状态
    private val _showDeviceMenu = MutableStateFlow(false)
    val showDeviceMenu: StateFlow<Boolean> = _showDeviceMenu.asStateFlow()
    
    // IP 对话框状态
    private val _showIpDialog = MutableStateFlow(false)
    val showIpDialog: StateFlow<Boolean> = _showIpDialog.asStateFlow()
    
    // RTP 端口状态
    private val _rtpPort = MutableStateFlow(PortConfig.DEFAULT_RTP_PORT)
    val rtpPort: StateFlow<Int> = _rtpPort.asStateFlow()
    
    // 端口对话框状态
    private val _showPortDialog = MutableStateFlow(false)
    val showPortDialog: StateFlow<Boolean> = _showPortDialog.asStateFlow()
    
    // SDP 生成对话框状态
    private val _showSdpDialog = MutableStateFlow(false)
    val showSdpDialog: StateFlow<Boolean> = _showSdpDialog.asStateFlow()
    
    // 关于对话框状态
    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()
    
    // SharedPreferences 名称
    companion object {
        private const val PREFS_IP = "ip"
        private const val KEY_IP = "ip"
        private const val PREFS_DEVICE_IPS = "device_ips"
        private const val KEY_DEVICE_IPS = "ips"
        private const val PREFS_RTP_PORT = "rtp_port"
        private const val KEY_RTP_PORT = "port"
    }
    
    init {
        loadIpAddress()
        loadDevices()
        // RTP 端口在 Activity 中加载
        _rtpPort.value = PortConfig.DEFAULT_RTP_PORT
    }
    
    fun updateIpAddress(ip: String) {
        _ipAddress.value = ip
    }
    
    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
        // 录制时隐藏菜单
        if (recording) {
            _showDeviceMenu.value = false
        }
    }
    
    fun toggleDeviceMenu(show: Boolean) {
        // 录制时不允许显示菜单
        if (_isRecording.value && show) {
            return
        }
        _showDeviceMenu.value = show
    }
    
    fun showIpDialog(show: Boolean) {
        _showIpDialog.value = show
    }
    
    fun showPortDialog(show: Boolean) {
        _showPortDialog.value = show
    }
    
    fun showSdpDialog(show: Boolean) {
        _showSdpDialog.value = show
    }
    
    fun showAboutDialog(show: Boolean) {
        _showAboutDialog.value = show
    }
    
    /**
     * 获取所有已添加的 IP 地址列表
     */
    fun getSelectedIps(): List<String> {
        return _deviceIps.value
    }
    
    /**
     * 验证并添加设备
     * 支持两种格式：
     * 1. 纯 IP：192.168.0.1
     * 2. IP:Port：192.168.0.1:3000
     *
     * @param input 输入字符串（可以是 IP 或 IP:Port）
     * @param context 上下文
     */
    fun addDevice(input: String, context: Context) {
        val validation = IpPortValidator.validateAndParse(input)

        if (!validation.isValid) {
            ToastUtils.showError(context, validation.errorMessage ?: "格式错误")
            return
        }

        val ip = validation.ip!!
        val port = validation.port

        // 如果提供了端口，更新 RTP 端口
        if (port != null) {
            if (PortConfig.isValidPort(port)) {
                _rtpPort.value = port
                saveRtpPort(context)
                val warnings = PortConfig.getPortValidationErrors(port)
                if (warnings.isNotEmpty()) {
                    ToastUtils.showInfo(context, warnings.joinToString("\n"))
                }
            } else {
                ToastUtils.showError(context, "端口必须在 ${PortConfig.MIN_PORT} 到 ${PortConfig.MAX_PORT} 之间")
                return
            }
        }

        // 添加设备（使用纯 IP 格式，port 信息通过 _rtpPort 共享）
        val currentList = _deviceIps.value.toMutableList()
        if (!currentList.contains(ip)) {
            currentList.add(ip)
            _deviceIps.value = currentList
            saveDevices(context)

            val message = if (port != null) {
                "已添加设备：$ip:$port"
            } else {
                "已添加设备：$ip:${_rtpPort.value}"
            }
            ToastUtils.showSuccess(context, message)
        } else {
            ToastUtils.showWarning(context, "该设备已存在")
        }
    }

    /**
     * 获取设备的显示文本，格式为 IP:Port
     * @param ip IP 地址
     * @return 格式化后的字符串
     */
    fun getDeviceDisplayText(ip: String): String {
        return "$ip:${_rtpPort.value}"
    }
    
    fun removeDevice(ip: String, context: Context) {
        val currentList = _deviceIps.value.toMutableList()
        currentList.remove(ip)
        _deviceIps.value = currentList
        saveDevices(context)
        ToastUtils.showInfo(context, "已删除设备：$ip")
    }
    
    fun selectDevice(ip: String, context: Context) {
        _ipAddress.value = ip
        ToastUtils.showInfo(context, "已选择设备：$ip")
    }
    
    private fun loadIpAddress() {
        // 从 SharedPreferences 加载 IP 地址
        // 注意：实际应用中需要通过 Application Context 访问
        // 这里简化处理，在 Activity 中加载
    }
    
    fun loadIpAddress(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_IP, Context.MODE_PRIVATE)
        val savedIp = prefs.getString(KEY_IP, "") ?: ""
        if (savedIp.isNotEmpty()) {
            _ipAddress.value = savedIp
        }
    }
    
    private fun loadDevices() {
        // 从 SharedPreferences 加载设备列表
        // 需要在 Activity 中初始化
    }
    
    fun loadDevices(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_DEVICE_IPS, Context.MODE_PRIVATE)
        val ips = prefs.getStringSet(KEY_DEVICE_IPS, emptySet()) ?: emptySet()
        _deviceIps.value = ips.toList()
    }
    
    fun saveIpAddress(context: Context) {
        context.getSharedPreferences(PREFS_IP, Context.MODE_PRIVATE).edit {
            putString(KEY_IP, _ipAddress.value)
        }
    }
    
    private fun saveDevices() {
        // 需要在 Activity 中调用
    }
    
    fun saveDevices(context: Context) {
        context.getSharedPreferences(PREFS_DEVICE_IPS, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_DEVICE_IPS, _deviceIps.value.toSet())
        }
    }
    
    /**
     * 加载 RTP 端口配置
     */
    fun loadRtpPort(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_RTP_PORT, Context.MODE_PRIVATE)
        val savedPort = prefs.getInt(KEY_RTP_PORT, PortConfig.DEFAULT_RTP_PORT)
        _rtpPort.value = savedPort
    }
    
    /**
     * 保存 RTP 端口配置
     */
    fun saveRtpPort(context: Context) {
        context.getSharedPreferences(PREFS_RTP_PORT, Context.MODE_PRIVATE).edit {
            putInt(KEY_RTP_PORT, _rtpPort.value)
        }
    }
    
    /**
     * 更新 RTP 端口并验证
     */
    fun updateRtpPort(port: Int, context: Context): Boolean {
        if (!PortConfig.isValidPort(port)) {
            ToastUtils.showError(context, "端口必须在 ${PortConfig.MIN_PORT} 到 ${PortConfig.MAX_PORT} 之间")
            return false
        }
        
        _rtpPort.value = port
        saveRtpPort(context)
        
        val warnings = PortConfig.getPortValidationErrors(port)
        if (warnings.isNotEmpty()) {
            ToastUtils.showInfo(context, warnings.joinToString("\n"))
        } else {
            ToastUtils.showSuccess(context, "端口设置成功：$port")
        }
        
        return true
    }
    
    /**
     * 验证端口有效性
     */
    fun validatePort(port: Int): List<String> {
        return PortConfig.getPortValidationErrors(port)
    }
}
