package com.dragon.renderlib.extension

import java.nio.ByteBuffer

/**
 * @author dragon
 */

fun ByteBuffer.print(size: Int = 4): String {
    position(0)
    val sb = StringBuilder("[")
    for (i in 0 until size) {
        sb.append(String.format("%02x ", get()))
    }
    sb.append("]")
    return sb.toString()
}

fun ByteBuffer.packageAndSend(block:()->Unit){

}

fun ByteArray.print(startIndex: Int = 0, size: Int = 4): String {
    val sb = StringBuilder("[")
    for (i in startIndex until startIndex + size) {
        sb.append(String.format("%02x ", get(i)))
    }
    sb.append("]")
    return sb.toString()
}

fun ByteArray.printStart(startIndex: Int = 0, size: Int = 4): String {
    val sb = StringBuilder("[")
    for (i in startIndex until startIndex + size) {
        sb.append(String.format("%02x ", get(i)))
    }
    sb.append("]")
    return sb.toString()
}
fun ByteArray.printSt(startIndex: Int = 0, size: Int = 4): String {
    val sb = StringBuilder("[")
    for (i in startIndex until startIndex + size) {
        sb.append(String.format("%02x ", get(i)))
    }
    sb.append("]")
    return sb.toString()
}
