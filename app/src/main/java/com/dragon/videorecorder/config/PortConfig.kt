package com.dragon.videorecorder.config

/**
 * RTP 端口配置
 */
object PortConfig {
    // 最小有效端口（避开系统保留端口 0-1023）
    const val MIN_PORT = 1024
    
    // 最大有效端口
    const val MAX_PORT = 65535
    
    // 推荐最小端口（避免常见服务冲突）
    const val RECOMMENDED_MIN_PORT = 10000
    
    // 默认 RTP 端口
    const val DEFAULT_RTP_PORT = 40018
    
    /**
     * 验证端口是否有效
     * @param port 要验证的端口号
     * @return true 如果端口有效，否则 false
     */
    fun isValidPort(port: Int): Boolean {
        return port in MIN_PORT..MAX_PORT
    }
    
    /**
     * 检查端口是否在推荐范围内
     * @param port 要检查的端口号
     * @return true 如果在推荐范围内，否则 false
     */
    fun isRecommendedPort(port: Int): Boolean {
        return port in RECOMMENDED_MIN_PORT..MAX_PORT
    }
    
    /**
     * 检查端口是否为偶数（RTP/RTCP 成对使用）
     * @param port 要检查的端口号
     * @return true 如果是偶数，否则 false
     */
    fun isEvenPort(port: Int): Boolean {
        return port % 2 == 0
    }
    
    /**
     * 获取端口验证错误信息
     * @param port 要验证的端口号
     * @return 错误信息列表，如果为空则表示端口有效
     */
    fun getPortValidationErrors(port: Int): List<String> {
        val errors = mutableListOf<String>()
        
        if (port < MIN_PORT || port > MAX_PORT) {
            errors.add("端口必须在 $MIN_PORT 到 $MAX_PORT 之间")
        } else {
            if (!isRecommendedPort(port)) {
                errors.add("建议使用 $RECOMMENDED_MIN_PORT 以上的端口")
            }
            if (!isEvenPort(port)) {
                errors.add("建议使用偶数端口（RTP 使用端口 n，RTCP 使用端口 n+1）")
            }
        }
        
        return errors
    }
}
