package com.dragon.videorecorder.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * IP 地址和端口验证工具类测试
 */
class IpPortValidatorTest {

    @Test
    fun `test valid IP address`() {
        assertTrue(IpPortValidator.isValidIp("192.168.0.1"))
        assertTrue(IpPortValidator.isValidIp("10.0.0.1"))
        assertTrue(IpPortValidator.isValidIp("127.0.0.1"))
        assertTrue(IpPortValidator.isValidIp("255.255.255.255"))
    }

    @Test
    fun `test invalid IP address`() {
        assertFalse(IpPortValidator.isValidIp("256.168.0.1"))
        assertFalse(IpPortValidator.isValidIp("192.168.0"))
        assertFalse(IpPortValidator.isValidIp("192.168.0.1.1"))
        assertFalse(IpPortValidator.isValidIp("192.168.0.a"))
        assertFalse(IpPortValidator.isValidIp(""))
    }

    @Test
    fun `test valid IP port format`() {
        assertTrue(IpPortValidator.isValidIpPort("192.168.0.1:3000"))
        assertTrue(IpPortValidator.isValidIpPort("192.168.0.1:80"))
        assertTrue(IpPortValidator.isValidIpPort("192.168.0.1:65535"))
        assertTrue(IpPortValidator.isValidIpPort("10.0.0.1:1"))
    }

    @Test
    fun `test invalid IP port format`() {
        assertFalse(IpPortValidator.isValidIpPort("192.168.0.1"))
        assertFalse(IpPortValidator.isValidIpPort("192.168.0.1:"))
        assertFalse(IpPortValidator.isValidIpPort("192.168.0.1:0"))
        assertFalse(IpPortValidator.isValidIpPort("192.168.0.1:65536"))
        assertFalse(IpPortValidator.isValidIpPort("192.168.0.1:99999"))
        assertFalse(IpPortValidator.isValidIpPort("192.168.0.1:abc"))
    }

    @Test
    fun `test parse IP only`() {
        val result = IpPortValidator.parseIpPort("192.168.0.1")
        assertNotNull(result)
        assertEquals("192.168.0.1", result?.first)
        assertNull(result?.second)
    }

    @Test
    fun `test parse IP with port`() {
        val result = IpPortValidator.parseIpPort("192.168.0.1:3000")
        assertNotNull(result)
        assertEquals("192.168.0.1", result?.first)
        assertEquals(3000, result?.second)
    }

    @Test
    fun `test parse invalid input`() {
        assertNull(IpPortValidator.parseIpPort("192.168.0"))
        assertNull(IpPortValidator.parseIpPort("192.168.0.1:"))
        assertNull(IpPortValidator.parseIpPort(""))
    }

    @Test
    fun `test validate and parse valid IP`() {
        val result = IpPortValidator.validateAndParse("192.168.0.1")
        assertTrue(result.isValid)
        assertEquals("192.168.0.1", result.ip)
        assertNull(result.port)
        assertNull(result.errorMessage)
    }

    @Test
    fun `test validate and parse valid IP with port`() {
        val result = IpPortValidator.validateAndParse("192.168.0.1:3000")
        assertTrue(result.isValid)
        assertEquals("192.168.0.1", result.ip)
        assertEquals(3000, result.port)
        assertNull(result.errorMessage)
    }

    @Test
    fun `test validate and parse invalid input`() {
        val result = IpPortValidator.validateAndParse("192.168.0")
        assertFalse(result.isValid)
        assertNull(result.ip)
        assertNull(result.port)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("格式不正确"))
    }
}
