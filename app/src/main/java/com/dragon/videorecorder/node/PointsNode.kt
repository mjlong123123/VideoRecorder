package com.dragon.renderlib.node

import com.dragon.renderlib.program.PrimitiveProgram

/**
 * @author dragon
 */
class PointsNode(x: Float, y: Float, w: Float, h: Float) : Node(x, y, w, h) {

    var pointX = 0f
    var pointY = 0f
    override fun recreate() {

    }

    override fun render(render: NodesRender) {
        render.program<PrimitiveProgram>(PrimitiveProgram::class.java).let { program ->
            positionBuffer.put(0, pointX)
            positionBuffer.put(1, pointY)
            program.draw(0, positionBuffer, textureCoordinateBuffer, render.openGlMatrix.mvpMatrix)
        }
    }

    override fun onTouch(action: Int, x: Float, y: Float): Boolean {
        pointX = x
        pointY = y
        return true
    }

    override fun release() {

    }
}