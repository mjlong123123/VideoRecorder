package com.dragon.renderlib.node

import com.dragon.renderlib.utils.OpenGlUtils

open abstract class Node(
    var x: Float,
    var y: Float,
    var w: Float,
    var h: Float
) {
    val positionBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
    val textureCoordinateBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(8)

    fun contain(x: Float, y: Float) = x > this.x && x < this.x + w && y > this.y && y < this.y + h

    abstract fun recreate()

    abstract fun render(render: NodesRender)

    abstract fun release()

    open fun onTouch(action: Int, x:Float, y:Float) = false
}