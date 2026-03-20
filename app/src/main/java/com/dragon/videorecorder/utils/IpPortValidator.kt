package com.dragon.videorecorder.utils

import java.util.regex.Pattern

/**
 * IP 地址和端口验证工具类
 */
object IpPortValidator {

    // IPv4 地址正则表达式
    private val IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )

    // IP:Port 格式正则表达式
    private val IP_PORT_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):(\\d{1,5})$"
    )

    /**
     * 验证纯 IP 地址格式
     * @param ip IP 地址字符串
     * @return 是否为有效的 IPv4 地址
     */
    fun isValidIp(ip: String): Boolean {
        return IPV4_PATTERN.matcher(ip.trim()).matches()
    }

    /**
     * 验证 IP:Port 格式
     * @param input 输入字符串，格式为 "IP:Port"
     * @return 是否为有效的 IP:Port 格式
     */
    fun isValidIpPort(input: String): Boolean {
        return IP_PORT_PATTERN.matcher(input.trim()).matches()
    }

    /**
     * 解析输入字符串，提取 IP 和端口
     * 支持两种格式：
     * 1. "192.168.0.1" (纯 IP)
     * 2. "192.168.0.1:3000" (IP:Port)
     *
     * @param input 输入字符串
     * @return Pair<IP, Port?>，如果只提供 IP，Port 为 null
     */
    fun parseIpPort(input: String): Pair<String, Int?>? {
        val trimmed = input.trim()

        // 尝试解析 IP:Port 格式
        val ipPortMatch = IP_PORT_PATTERN.matcher(trimmed)
        if (ipPortMatch.matches()) {
            val ip = trimmed.substringBeforeLast(":")
            val portStr = trimmed.substringAfterLast(":")
            val port = portStr.toIntOrNull()
            
            // 验证端口范围
            if (port != null && port in 1..65535) {
                return Pair(ip, port)
            }
        }

        // 尝试解析纯 IP 格式
        val ipMatch = IPV4_PATTERN.matcher(trimmed)
        if (ipMatch.matches()) {
            return Pair(trimmed, null)
        }

        return null
    }

    /**
     * 验证并解析输入字符串
     * @param input 输入字符串
     * @return 验证结果，包含错误信息或解析后的 IP 和端口
     */
    fun validateAndParse(input: String): ValidationResult {
        val result = parseIpPort(input)
        return if (result != null) {
            ValidationResult(
                isValid = true,
                ip = result.first,
                port = result.second,
                errorMessage = null
            )
        } else {
            ValidationResult(
                isValid = false,
                ip = null,
                port = null,
                errorMessage = "格式不正确。支持的格式：\n" +
                        "• 纯 IP：192.168.0.1\n" +
                        "• IP:Port：192.168.0.1:3000\n" +
                        "端口范围：1-65535"
            )
        }
    }

    /**
     * 验证结果数据类
     */
    data class ValidationResult(
        val isValid: Boolean,
        val ip: String?,
        val port: Int?,
        val errorMessage: String?
    )
}
